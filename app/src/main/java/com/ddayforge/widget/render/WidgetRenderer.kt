package com.ddayforge.widget.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.ddayforge.widget.data.DDay
import com.ddayforge.widget.data.DDayCalc
import com.ddayforge.widget.style.BackgroundFill
import com.ddayforge.widget.style.BorderStyle
import com.ddayforge.widget.style.Decoration
import com.ddayforge.widget.style.FontChoice
import com.ddayforge.widget.style.LayoutStyle
import com.ddayforge.widget.style.TextAlign
import com.ddayforge.widget.style.TextEffect
import com.ddayforge.widget.style.Texture
import com.ddayforge.widget.style.WidgetShape
import com.ddayforge.widget.style.WidgetStyle
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** 위젯에 표시될 글자들 */
data class WidgetContent(
    val title: String,
    val count: String,
    val date: String,
) {
    companion object {
        fun of(dday: DDay, style: WidgetStyle, today: LocalDate = LocalDate.now()): WidgetContent {
            val v = DDayCalc.compute(dday, today)
            return WidgetContent(
                title = dday.title,
                count = style.countFormat.format(v),
                date = DDayCalc.formatDate(dday.date),
            )
        }

        fun placeholder(message: String = "탭해서 디데이 선택") =
            WidgetContent(title = message, count = "D-?", date = "")
    }
}

/**
 * 위젯 전체를 비트맵 한 장으로 그린다.
 * RemoteViews로는 커스텀 폰트/텍스처/글로우를 쓸 수 없기 때문에, 앱 안 미리보기와 홈 화면 위젯이
 * 정확히 같은 이 렌더러를 사용한다. (= 미리보기 그대로 위젯에 나옴)
 *
 * 그리기 순서는 부품 단위로 나뉘어 있다:
 *   형태(path) → [오프셋 그림자] → 배경 채우기 → 텍스처 → 테두리 → 장식 → 텍스트(레이아웃 + 효과)
 */
class WidgetRenderer(context: Context) {
    private val appContext = context.applicationContext

    /**
     * @param systemClipDp 런처가 위젯을 잘라내는 모서리 반경(dp). 0이면 잘림 없음.
     *   style.clipGuard가 켜져 있으면 디자인이 이 둥근 영역 안에 들어오도록 안쪽으로 줄여 그린다.
     */
    fun render(
        style: WidgetStyle,
        content: WidgetContent,
        widthDp: Float,
        heightDp: Float,
        density: Float,
        systemClipDp: Float = 0f,
        maxPixels: Float = 1_600_000f,
    ): Bitmap {
        var d = density
        // 비트맵 메모리 제한 (RemoteViews 한도 대비 여유)
        if (widthDp * heightDp * d * d > maxPixels) d = sqrt(maxPixels / (widthDp * heightDp))
        val w = max(1, (widthDp * d).toInt())
        val h = max(1, (heightDp * d).toInt())
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Frame(Canvas(bmp), style, content, w.toFloat(), h.toFloat(), d, if (style.clipGuard) systemClipDp else 0f).draw()
        return bmp
    }

    // ---------------------------------------------------------------------------------------
    private inner class Frame(
        val c: Canvas,
        val s: WidgetStyle,
        val content: WidgetContent,
        val w: Float,
        val h: Float,
        val d: Float,
        val systemClipDp: Float,
    ) {
        val bw = if (s.border == BorderStyle.NONE) 0f else s.borderWidth * d
        val offsetShadow = if (s.border == BorderStyle.OFFSET) bw * 1.5f + 2f * d else 0f
        val margin = when {
            s.border == BorderStyle.NEON -> bw + 8f * d
            s.decoration == Decoration.TAPE -> 7f * d
            s.textEffect == TextEffect.GLOW && s.border == BorderStyle.NONE -> 2f * d
            else -> 1f * d
        } + clipGuardInset()

        /**
         * 런처의 둥근 잘림(반경 R) 안에 이 위젯의 모서리(반경 r)가 다 들어오려면
         * 가장자리에서 (R - r)(1 - 1/√2) 만큼 떨어져 있어야 한다.
         */
        fun clipGuardInset(): Float {
            if (systemClipDp <= 0f) return 0f
            val wDp = w / d
            val hDp = h / d
            val ownR = when (s.shape) {
                WidgetShape.ROUNDED -> s.cornerRadius
                WidgetShape.PILL -> min(wDp, hDp) / 2f
                WidgetShape.CIRCLE -> return if (wDp == hDp) systemClipDp * 0.05f * d else 0f
                WidgetShape.TICKET -> s.cornerRadius * 0.6f
                WidgetShape.CHAMFER -> s.cornerRadius * 0.6f
                else -> 0f
            }
            val clipR = min(systemClipDp, min(wDp, hDp) / 2f)
            return max(0f, clipR - ownR) * (1f - 1f / sqrt(2f)) * d
        }

        val bounds: RectF = run {
            val r = RectF(margin, margin, w - margin - offsetShadow, h - margin - offsetShadow)
            if (s.shape == WidgetShape.CIRCLE) {
                val size = min(r.width(), r.height())
                RectF(r.centerX() - size / 2, r.centerY() - size / 2, r.centerX() + size / 2, r.centerY() + size / 2)
            } else r
        }
        val radius = min(s.cornerRadius * d, min(bounds.width(), bounds.height()) / 2f)

        /**
         * 작은 위젯(예: 2x1)에서는 여백·장식이 고정 dp면 글자 들어갈 자리가 없어진다.
         * 짧은 변 110dp를 기준으로 여백과 장식 크기를 함께 줄인다.
         */
        val compact = (min(bounds.width(), bounds.height()) / d / 110f).coerceIn(0.45f, 1f)
        val dk = d * compact
        val shapePath = shapePath(bounds, radius)

        fun draw() {
            if (s.border == BorderStyle.OFFSET) {
                val p = Path(shapePath).apply { offset(offsetShadow, offsetShadow) }
                c.drawPath(p, fillPaint(s.borderColor))
            }
            // 배경 레이어: 채우기 + 텍스처, 배경 투명도 적용
            val layerAlpha = (s.bgOpacity.coerceIn(0f, 1f) * 255).toInt()
            val save = c.saveLayerAlpha(RectF(0f, 0f, w, h), layerAlpha)
            c.clipPath(shapePath)
            drawFill()
            drawTexture()
            c.restoreToCount(save)

            drawBorder()
            val contentRect = drawDecorationAndGetContentRect()
            drawText(contentRect)
        }

        // ---------------- 형태 ----------------
        fun shapePath(r: RectF, rad: Float): Path {
            val p = Path()
            when (s.shape) {
                WidgetShape.RECTANGLE -> p.addRect(r, Path.Direction.CW)
                WidgetShape.ROUNDED -> p.addRoundRect(r, rad, rad, Path.Direction.CW)
                WidgetShape.PILL -> {
                    val pr = min(r.width(), r.height()) / 2f
                    p.addRoundRect(r, pr, pr, Path.Direction.CW)
                }
                WidgetShape.CIRCLE -> p.addOval(r, Path.Direction.CW)
                WidgetShape.CHAMFER -> cutCorners(p, r, rad, rad, rad, rad)
                WidgetShape.ASYM_CUT -> cutCorners(p, r, rad * 1.4f, rad * 0.3f, rad * 1.4f, rad * 0.3f)
                WidgetShape.TICKET -> {
                    val base = Path().apply { addRoundRect(r, rad * 0.6f, rad * 0.6f, Path.Direction.CW) }
                    val nr = min(r.height() * 0.13f, max(rad * 0.7f, 6f * d))
                    val notches = Path().apply {
                        addCircle(r.left, r.centerY(), nr, Path.Direction.CW)
                        addCircle(r.right, r.centerY(), nr, Path.Direction.CW)
                    }
                    base.op(notches, Path.Op.DIFFERENCE)
                    p.addPath(base)
                }
                WidgetShape.PIXEL -> {
                    val step = max(2f * d, rad / 3f)
                    // 각 모서리를 3단 계단으로
                    p.moveTo(r.left + 3 * step, r.top)
                    p.lineTo(r.right - 3 * step, r.top)
                    p.lineTo(r.right - 3 * step, r.top + step); p.lineTo(r.right - 2 * step, r.top + step)
                    p.lineTo(r.right - 2 * step, r.top + 2 * step); p.lineTo(r.right - step, r.top + 2 * step)
                    p.lineTo(r.right - step, r.top + 3 * step); p.lineTo(r.right, r.top + 3 * step)
                    p.lineTo(r.right, r.bottom - 3 * step)
                    p.lineTo(r.right - step, r.bottom - 3 * step); p.lineTo(r.right - step, r.bottom - 2 * step)
                    p.lineTo(r.right - 2 * step, r.bottom - 2 * step); p.lineTo(r.right - 2 * step, r.bottom - step)
                    p.lineTo(r.right - 3 * step, r.bottom - step); p.lineTo(r.right - 3 * step, r.bottom)
                    p.lineTo(r.left + 3 * step, r.bottom)
                    p.lineTo(r.left + 3 * step, r.bottom - step); p.lineTo(r.left + 2 * step, r.bottom - step)
                    p.lineTo(r.left + 2 * step, r.bottom - 2 * step); p.lineTo(r.left + step, r.bottom - 2 * step)
                    p.lineTo(r.left + step, r.bottom - 3 * step); p.lineTo(r.left, r.bottom - 3 * step)
                    p.lineTo(r.left, r.top + 3 * step)
                    p.lineTo(r.left + step, r.top + 3 * step); p.lineTo(r.left + step, r.top + 2 * step)
                    p.lineTo(r.left + 2 * step, r.top + 2 * step); p.lineTo(r.left + 2 * step, r.top + step)
                    p.lineTo(r.left + 3 * step, r.top + step)
                    p.close()
                }
            }
            return p
        }

        fun cutCorners(p: Path, r: RectF, tl: Float, tr: Float, br: Float, bl: Float) {
            p.moveTo(r.left + tl, r.top)
            p.lineTo(r.right - tr, r.top)
            p.lineTo(r.right, r.top + tr)
            p.lineTo(r.right, r.bottom - br)
            p.lineTo(r.right - br, r.bottom)
            p.lineTo(r.left + bl, r.bottom)
            p.lineTo(r.left, r.bottom - bl)
            p.lineTo(r.left, r.top + tl)
            p.close()
        }

        // ---------------- 배경 ----------------
        fun drawFill() {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            when (s.fill) {
                BackgroundFill.SOLID -> p.color = s.bgColor
                BackgroundFill.LINEAR_GRADIENT -> {
                    val a = Math.toRadians(s.gradientAngle.toDouble())
                    val dx = cos(a).toFloat()
                    val dy = sin(a).toFloat()
                    val half = (abs(bounds.width() * dx) + abs(bounds.height() * dy)) / 2f
                    val cx = bounds.centerX()
                    val cy = bounds.centerY()
                    p.shader = LinearGradient(
                        cx - dx * half, cy - dy * half, cx + dx * half, cy + dy * half,
                        s.bgColor, s.bgColor2, Shader.TileMode.CLAMP,
                    )
                }
                BackgroundFill.RADIAL_GRADIENT -> {
                    val r = max(bounds.width(), bounds.height()) * 0.75f
                    p.shader = RadialGradient(
                        bounds.centerX(), bounds.centerY(), max(r, 1f),
                        s.bgColor, s.bgColor2, Shader.TileMode.CLAMP,
                    )
                }
            }
            c.drawRect(bounds, p)
        }

        // ---------------- 텍스처 ----------------
        fun texPaint(alphaMul: Float = 1f): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mulAlpha(s.textureColor, s.textureOpacity.coerceIn(0f, 1f) * alphaMul)
        }

        fun drawTexture() {
            val u = d * s.textureScale.coerceIn(0.3f, 4f)
            val b = bounds
            when (s.texture) {
                Texture.NONE -> Unit
                Texture.SCANLINES -> scanlines(u)
                Texture.INTERLACE -> {
                    // 촘촘한 주사선 + VHS처럼 가로로 번지는 줄무늬 노이즈
                    scanlines(u * 0.8f)
                    val rnd = Random(23)
                    val p = Paint(Paint.ANTI_ALIAS_FLAG)
                    val rows = (b.height() / (1.6f * u)).toInt().coerceIn(10, 800)
                    repeat(rows * 3) {
                        val y = b.top + rnd.nextFloat() * b.height()
                        val x = b.left + rnd.nextFloat() * b.width()
                        val ww = b.width() * (0.03f + rnd.nextFloat() * rnd.nextFloat() * 0.35f)
                        val hh = (0.6f + rnd.nextFloat() * 1.2f) * u
                        val bright = rnd.nextFloat() < 0.35f
                        p.color = if (bright) mulAlpha(Color.WHITE, s.textureOpacity * 0.18f * rnd.nextFloat())
                        else mulAlpha(s.textureColor, s.textureOpacity * (0.3f + 0.7f * rnd.nextFloat()))
                        c.drawRect(x, y, x + ww, y + hh, p)
                    }
                    // 가장자리 어둡게
                    val vp = Paint(Paint.ANTI_ALIAS_FLAG)
                    vp.shader = RadialGradient(
                        b.centerX(), b.centerY(), max(b.width(), b.height()) * 0.75f,
                        intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, mulAlpha(Color.BLACK, min(1f, s.textureOpacity))),
                        floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP,
                    )
                    c.drawRect(b, vp)
                }
                Texture.CRT -> {
                    scanlines(u)
                    // 비네팅
                    val vp = Paint(Paint.ANTI_ALIAS_FLAG)
                    vp.shader = RadialGradient(
                        b.centerX(), b.centerY(), max(b.width(), b.height()) * 0.7f,
                        intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, mulAlpha(s.textureColor, min(1f, s.textureOpacity * 1.6f))),
                        floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
                    )
                    c.drawRect(b, vp)
                    // 화면 반사광
                    val gp = Paint(Paint.ANTI_ALIAS_FLAG)
                    gp.shader = LinearGradient(
                        b.left, b.top, b.left, b.top + b.height() * 0.5f,
                        Color.argb(28, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP,
                    )
                    c.drawRect(b, gp)
                }
                Texture.GRID -> {
                    val p = texPaint().apply { isAntiAlias = false }
                    val lw = max(1f, Math.round(u * 0.8f).toFloat())
                    val step = max(4f, Math.round(16f * u).toFloat())
                    var x = Math.round(b.left + (b.width() % step) / 2).toFloat()
                    while (x < b.right) { c.drawRect(x, b.top, x + lw, b.bottom, p); x += step }
                    var y = Math.round(b.top + (b.height() % step) / 2).toFloat()
                    while (y < b.bottom) { c.drawRect(b.left, y, b.right, y + lw, p); y += step }
                }
                Texture.PERSPECTIVE_GRID -> {
                    val horizon = b.top + b.height() * 0.55f
                    val p = texPaint().apply { strokeWidth = max(1f, u * 1.1f) }
                    val save = c.save()
                    c.clipRect(b.left, horizon, b.right, b.bottom)
                    val n = 9
                    for (i in 1..n) {
                        val t = i.toFloat() / n
                        val y = horizon + (b.bottom - horizon) * t * t
                        p.alpha = (Color.alpha(texPaint().color) * (0.3f + 0.7f * t)).toInt()
                        c.drawLine(b.left, y, b.right, y, p)
                    }
                    p.color = texPaint().color
                    val vx = b.centerX()
                    val spacing = 22f * u
                    var k = -40
                    while (k <= 40) {
                        val bx = vx + k * spacing * 2.2f
                        c.drawLine(vx + k * spacing * 0.15f, horizon, bx, b.bottom + (b.bottom - horizon), p)
                        k++
                    }
                    c.restoreToCount(save)
                    // 지평선 빛
                    val hp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = mulAlpha(s.textureColor, min(1f, s.textureOpacity * 1.5f))
                        strokeWidth = max(1.5f, u * 1.5f)
                        maskFilter = BlurMaskFilter(3f * u, BlurMaskFilter.Blur.NORMAL)
                    }
                    c.drawLine(b.left, horizon, b.right, horizon, hp)
                    hp.maskFilter = null
                    c.drawLine(b.left, horizon, b.right, horizon, hp)
                }
                Texture.DOTS -> {
                    val p = texPaint()
                    val step = 8f * u
                    var y = b.top + step / 2
                    while (y < b.bottom) {
                        var x = b.left + step / 2
                        while (x < b.right) { c.drawCircle(x, y, 1.2f * u, p); x += step }
                        y += step
                    }
                }
                Texture.HALFTONE -> {
                    val p = texPaint()
                    val step = 7f * u
                    var row = 0
                    var y = b.top
                    while (y < b.bottom + step) {
                        var x = b.left + if (row % 2 == 0) 0f else step / 2
                        while (x < b.right + step) {
                            val t = ((x - b.left) / b.width() + (y - b.top) / b.height()) / 2f
                            val r = step * 0.5f * t.coerceIn(0.05f, 1f)
                            c.drawCircle(x, y, r, p)
                            x += step
                        }
                        y += step * 0.866f; row++
                    }
                }
                Texture.NOISE -> noise(u, Random(42), 1f)
                Texture.DIAGONAL -> {
                    val p = texPaint().apply { strokeWidth = max(1f, 1.5f * u) }
                    val step = 10f * u
                    var x = b.left - b.height()
                    while (x < b.right) { c.drawLine(x, b.bottom, x + b.height(), b.top, p); x += step }
                }
                Texture.CARBON -> {
                    val cell = 6f * u
                    val light = texPaint()
                    val dark = Paint().apply { color = mulAlpha(Color.BLACK, min(1f, s.textureOpacity * 2.5f)) }
                    var j = 0
                    var y = b.top
                    while (y < b.bottom) {
                        var i = 0
                        var x = b.left
                        while (x < b.right) {
                            if ((i + j) % 2 == 0) {
                                c.drawRect(x, y, x + cell, y + cell / 2, light)
                                c.drawRect(x, y + cell / 2, x + cell, y + cell, dark)
                            } else {
                                c.drawRect(x, y, x + cell / 2, y + cell, light)
                                c.drawRect(x + cell / 2, y, x + cell, y + cell, dark)
                            }
                            x += cell; i++
                        }
                        y += cell; j++
                    }
                }
                Texture.CIRCUIT -> {
                    val rnd = Random(7)
                    val p = texPaint().apply { style = Paint.Style.STROKE; strokeWidth = max(1f, 1.2f * u) }
                    val grid = 12f * u
                    val count = ((b.width() * b.height()) / (grid * grid * 6f)).toInt().coerceIn(6, 300)
                    repeat(count) {
                        var x = b.left + (rnd.nextInt(max(1, (b.width() / grid).toInt())) * grid)
                        var y = b.top + (rnd.nextInt(max(1, (b.height() / grid).toInt())) * grid)
                        val path = Path().apply { moveTo(x, y) }
                        c.drawCircle(x, y, 2.2f * u, p)
                        repeat(3) { seg ->
                            val len = grid * (1 + rnd.nextInt(4))
                            when ((seg + rnd.nextInt(2)) % 3) {
                                0 -> x += if (rnd.nextBoolean()) len else -len
                                1 -> y += if (rnd.nextBoolean()) len else -len
                                else -> { val dd = len * 0.6f; x += dd; y += if (rnd.nextBoolean()) dd else -dd }
                            }
                            path.lineTo(x, y)
                        }
                        c.drawPath(path, p)
                        c.drawCircle(x, y, 2.2f * u, p)
                    }
                }
                Texture.PAPER -> {
                    noise(u, Random(5), 0.6f)
                    val rnd = Random(9)
                    val p = texPaint(0.5f).apply { strokeWidth = max(1f, 0.6f * u) }
                    val n = ((b.width() * b.height()) / (400f * u * u)).toInt().coerceIn(10, 2000)
                    repeat(n) {
                        val x = b.left + rnd.nextFloat() * b.width()
                        val y = b.top + rnd.nextFloat() * b.height()
                        val a = rnd.nextFloat() * Math.PI.toFloat()
                        val l = (3f + rnd.nextFloat() * 8f) * u
                        c.drawLine(x, y, x + cos(a) * l, y + sin(a) * l, p)
                    }
                }
                Texture.STARS -> {
                    val rnd = Random(3)
                    val p = Paint(Paint.ANTI_ALIAS_FLAG)
                    val n = ((b.width() * b.height()) / (180f * u * u)).toInt().coerceIn(10, 1500)
                    repeat(n) {
                        val x = b.left + rnd.nextFloat() * b.width()
                        val y = b.top + rnd.nextFloat() * b.height()
                        val r = (0.3f + rnd.nextFloat() * rnd.nextFloat() * 1.4f) * u
                        p.color = mulAlpha(s.textureColor, s.textureOpacity * (0.25f + rnd.nextFloat() * 0.75f))
                        c.drawCircle(x, y, r, p)
                        if (r > 1.3f * u && rnd.nextFloat() < 0.5f) {
                            p.strokeWidth = max(1f, 0.4f * u)
                            c.drawLine(x - r * 3, y, x + r * 3, y, p)
                            c.drawLine(x, y - r * 3, x, y + r * 3, p)
                        }
                    }
                }
                Texture.GLITCH -> {
                    scanlines(u * 1.5f)
                    val rnd = Random(11)
                    val p = Paint()
                    repeat(14) {
                        val y = b.top + rnd.nextFloat() * b.height()
                        val hh = (1.5f + rnd.nextFloat() * 6f) * u
                        val x = b.left + rnd.nextFloat() * b.width() * 0.7f
                        val ww = b.width() * (0.1f + rnd.nextFloat() * 0.5f)
                        p.color = mulAlpha(s.textureColor, s.textureOpacity * (0.4f + rnd.nextFloat()))
                        c.drawRect(x, y, x + ww, y + hh, p)
                    }
                }
                Texture.HEX -> {
                    val p = texPaint().apply { style = Paint.Style.STROKE; strokeWidth = max(1f, u) }
                    val r = 10f * u
                    val hw = sqrt(3f) * r
                    var row = 0
                    var y = b.top
                    while (y < b.bottom + r) {
                        var x = b.left + if (row % 2 == 0) 0f else hw / 2
                        while (x < b.right + hw) {
                            val path = Path()
                            for (i in 0..5) {
                                val a = Math.toRadians((60.0 * i) - 30.0)
                                val px = x + r * cos(a).toFloat()
                                val py = y + r * sin(a).toFloat()
                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            path.close()
                            c.drawPath(path, p)
                            x += hw
                        }
                        y += r * 1.5f; row++
                    }
                }
                Texture.LCD -> {
                    val p = texPaint().apply { isAntiAlias = false }
                    val cell = max(2f, Math.round(3.5f * u).toFloat())
                    val gap = max(1f, Math.round(0.6f * u).toFloat())
                    var y = Math.floor(b.top.toDouble()).toFloat()
                    while (y < b.bottom) {
                        var x = Math.floor(b.left.toDouble()).toFloat()
                        while (x < b.right) { c.drawRect(x, y, x + cell - gap, y + cell - gap, p); x += cell }
                        y += cell
                    }
                }
            }
        }

        /** 픽셀 격자에 맞춘 주사선: 줄였을 때 물결무늬(모아레)가 덜 생긴다 */
        fun scanlines(u: Float) {
            val p = texPaint().apply { isAntiAlias = false }
            val step = max(2f, Math.round(3f * u).toFloat())
            val lh = max(1f, Math.round(step * 0.45f).toFloat())
            var y = Math.floor(bounds.top.toDouble()).toFloat()
            while (y < bounds.bottom) {
                c.drawRect(bounds.left, y, bounds.right, y + lh, p)
                y += step
            }
        }

        fun noise(u: Float, rnd: Random, alphaMul: Float) {
            val p = Paint()
            val size = max(1f, u)
            val n = ((bounds.width() * bounds.height()) / (size * size * 5f)).toInt().coerceIn(100, 40000)
            repeat(n) {
                val x = bounds.left + rnd.nextFloat() * bounds.width()
                val y = bounds.top + rnd.nextFloat() * bounds.height()
                p.color = mulAlpha(s.textureColor, s.textureOpacity * alphaMul * rnd.nextFloat())
                c.drawRect(x, y, x + size, y + size, p)
            }
        }

        // ---------------- 테두리 ----------------
        fun borderPath(inset: Float): Path {
            val r = RectF(bounds).apply { inset(inset, inset) }
            return shapePath(r, max(0f, radius - inset))
        }

        fun drawBorder() {
            if (s.border == BorderStyle.NONE || bw <= 0f) return
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = s.borderColor
                strokeWidth = bw
                strokeJoin = Paint.Join.MITER
            }
            when (s.border) {
                BorderStyle.NONE -> Unit
                BorderStyle.SOLID, BorderStyle.OFFSET -> c.drawPath(borderPath(bw / 2), p)
                BorderStyle.DOUBLE -> {
                    p.strokeWidth = bw
                    c.drawPath(borderPath(bw / 2), p)
                    p.strokeWidth = max(1f, bw * 0.6f)
                    c.drawPath(borderPath(bw * 2.6f), p)
                }
                BorderStyle.DASHED -> {
                    p.pathEffect = DashPathEffect(floatArrayOf(bw * 4f + 4f * d, bw * 2f + 3f * d), 0f)
                    c.drawPath(borderPath(bw / 2), p)
                }
                BorderStyle.DOTTED -> {
                    p.strokeCap = Paint.Cap.ROUND
                    p.pathEffect = DashPathEffect(floatArrayOf(0.1f, bw * 2.5f), 0f)
                    c.drawPath(borderPath(bw / 2), p)
                }
                BorderStyle.NEON -> {
                    val path = borderPath(bw / 2)
                    p.strokeWidth = bw * 2.5f
                    p.maskFilter = BlurMaskFilter(6f * d, BlurMaskFilter.Blur.NORMAL)
                    c.drawPath(path, p)
                    p.strokeWidth = bw * 1.2f
                    p.maskFilter = BlurMaskFilter(2f * d, BlurMaskFilter.Blur.NORMAL)
                    c.drawPath(path, p)
                    p.maskFilter = null
                    p.strokeWidth = max(1f, bw * 0.45f)
                    p.color = lighten(s.borderColor, 0.6f)
                    c.drawPath(path, p)
                }
            }
        }

        // ---------------- 장식 ----------------
        /** 장식을 그리고, 텍스트가 들어갈 영역을 돌려준다. */
        fun drawDecorationAndGetContentRect(): RectF {
            val pad = s.padding * dk + bw
            val area = if (s.shape == WidgetShape.CIRCLE) {
                val k = bounds.width() * (1f - 1f / sqrt(2f)) / 2f
                RectF(bounds).apply { inset(k, k) }
            } else RectF(bounds)
            if (s.shape == WidgetShape.TICKET) {
                val nr = min(bounds.height() * 0.13f, max(radius * 0.7f, 6f * dk))
                area.inset(nr * 0.6f, 0f)
            }
            val content = RectF(area).apply { inset(pad, pad) }
            val dc = s.decorationColor
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dc }
            when (s.decoration) {
                Decoration.NONE -> Unit
                Decoration.CORNER_BRACKETS -> {
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = 2f * dk
                    p.strokeCap = Paint.Cap.SQUARE
                    val cut = when (s.shape) {
                        WidgetShape.CHAMFER, WidgetShape.ASYM_CUT -> radius * 0.75f
                        WidgetShape.ROUNDED, WidgetShape.PILL -> radius * 0.35f
                        else -> 0f
                    }
                    val ins = bw + 5f * dk + cut
                    val l = min(bounds.width(), bounds.height()) * 0.14f
                    val r = RectF(bounds).apply { inset(ins, ins) }
                    c.drawLine(r.left, r.top, r.left + l, r.top, p); c.drawLine(r.left, r.top, r.left, r.top + l, p)
                    c.drawLine(r.right, r.top, r.right - l, r.top, p); c.drawLine(r.right, r.top, r.right, r.top + l, p)
                    c.drawLine(r.left, r.bottom, r.left + l, r.bottom, p); c.drawLine(r.left, r.bottom, r.left, r.bottom - l, p)
                    c.drawLine(r.right, r.bottom, r.right - l, r.bottom, p); c.drawLine(r.right, r.bottom, r.right, r.bottom - l, p)
                }
                Decoration.TERMINAL_BAR -> {
                    val barH = min(20f * dk, bounds.height() * 0.22f)
                    val save = c.save()
                    c.clipPath(shapePath)
                    p.color = mulAlpha(dc, 0.16f)
                    c.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + barH, p)
                    p.color = mulAlpha(dc, 0.5f)
                    c.drawRect(bounds.left, bounds.top + barH - max(1f, d), bounds.right, bounds.top + barH, p)
                    c.restoreToCount(save)
                    val cy = bounds.top + barH / 2
                    val r = barH * 0.18f
                    var x = bounds.left + bw + max(radius * 0.5f, 8f * dk) + r
                    for (a in floatArrayOf(1f, 0.6f, 0.3f)) {
                        p.color = mulAlpha(dc, a); c.drawCircle(x, cy, r, p); x += r * 3f
                    }
                    val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = Typeface.MONOSPACE; textSize = barH * 0.5f; color = mulAlpha(dc, 0.8f)
                        textAlign = Paint.Align.CENTER
                    }
                    c.drawText("dday.sh", bounds.centerX(), cy - (tp.ascent() + tp.descent()) / 2, tp)
                    content.top = max(content.top, bounds.top + barH + s.padding * dk * 0.6f)
                }
                Decoration.HUD -> {
                    p.strokeWidth = max(1f, d)
                    val ins = bw + 4f * dk + if (s.shape == WidgetShape.CHAMFER || s.shape == WidgetShape.ASYM_CUT) radius else radius * 0.4f
                    val step = 6f * dk
                    var x = bounds.left + ins
                    var i = 0
                    while (x < bounds.right - ins) {
                        val len = if (i % 5 == 0) 6f * dk else 3f * dk
                        p.color = mulAlpha(dc, if (i % 5 == 0) 0.9f else 0.45f)
                        c.drawLine(x, bounds.top + bw + 3f * dk, x, bounds.top + bw + 3f * dk + len, p)
                        c.drawLine(x, bounds.bottom - bw - 3f * dk, x, bounds.bottom - bw - 3f * dk - len, p)
                        x += step; i++
                    }
                    val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = Typeface.MONOSPACE; textSize = 7.5f * dk; color = mulAlpha(dc, 0.85f)
                    }
                    val ty = bounds.top + bw + 18f * dk
                    c.drawText("SYS//COUNTDOWN", bounds.left + ins, ty, tp)
                    tp.textAlign = Paint.Align.RIGHT
                    c.drawText("◢ LIVE", bounds.right - ins, ty, tp)
                    content.top = max(content.top, ty + 4f * dk)
                    content.bottom = min(content.bottom, bounds.bottom - bw - 12f * dk)
                }
                Decoration.TAPE -> {
                    val tw = bounds.width() * 0.32f
                    val th = 16f * dk
                    val save = c.save()
                    c.rotate(-4f, bounds.centerX(), bounds.top)
                    p.color = dc
                    c.drawRect(bounds.centerX() - tw / 2, bounds.top - th / 2 + 1f * dk, bounds.centerX() + tw / 2, bounds.top + th / 2 + 1f * dk, p)
                    p.color = Color.argb(30, 255, 255, 255)
                    var x = bounds.centerX() - tw / 2
                    while (x < bounds.centerX() + tw / 2) {
                        c.drawRect(x, bounds.top - th / 2 + 1f * dk, x + 2f * dk, bounds.top + th / 2 + 1f * dk, p); x += 6f * dk
                    }
                    c.restoreToCount(save)
                    content.top = max(content.top, bounds.top + th / 2 + 4f * dk)
                }
                Decoration.PIXEL_CORNERS -> {
                    val px = 3f * dk
                    val ins = bw + 4f * dk + if (s.shape == WidgetShape.PIXEL) radius * 0.4f else 0f
                    fun corner(x: Float, y: Float, sx: Int, sy: Int) {
                        val cells = listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 0 to 2, 1 to 1)
                        for ((cx, cy) in cells) {
                            val l = x + sx * cx * px - if (sx < 0) px else 0f
                            val t = y + sy * cy * px - if (sy < 0) px else 0f
                            c.drawRect(l, t, l + px, t + px, p)
                        }
                    }
                    corner(bounds.left + ins, bounds.top + ins, 1, 1)
                    corner(bounds.right - ins, bounds.top + ins, -1, 1)
                    corner(bounds.left + ins, bounds.bottom - ins, 1, -1)
                    corner(bounds.right - ins, bounds.bottom - ins, -1, -1)
                }
                Decoration.SIDE_STRIPE -> {
                    val sw = 7f * dk
                    val save = c.save()
                    c.clipPath(shapePath)
                    c.drawRect(bounds.left, bounds.top, bounds.left + sw, bounds.bottom, p)
                    c.restoreToCount(save)
                    content.left = max(content.left, bounds.left + sw + s.padding * dk * 0.8f)
                }
                Decoration.WARNING_STRIPES -> {
                    val bandH = min(9f * dk, bounds.height() * 0.12f)
                    val save = c.save()
                    c.clipPath(shapePath)
                    for (top in floatArrayOf(bounds.top, bounds.bottom - bandH)) {
                        val s2 = c.save()
                        c.clipRect(bounds.left, top, bounds.right, top + bandH)
                        p.color = Color.argb(200, 10, 10, 10)
                        c.drawRect(bounds.left, top, bounds.right, top + bandH, p)
                        p.color = dc
                        val step = bandH * 2f
                        var x = bounds.left - bandH
                        while (x < bounds.right + bandH) {
                            val path = Path().apply {
                                moveTo(x, top + bandH); lineTo(x + bandH, top)
                                lineTo(x + bandH * 2f, top); lineTo(x + bandH, top + bandH); close()
                            }
                            c.drawPath(path, p)
                            x += step * 1.0f
                        }
                        c.restoreToCount(s2)
                    }
                    c.restoreToCount(save)
                    content.top = max(content.top, bounds.top + bandH + s.padding * dk * 0.5f)
                    content.bottom = min(content.bottom, bounds.bottom - bandH - s.padding * dk * 0.5f)
                }
            }
            return content
        }

        // ---------------- 텍스트 ----------------
        inner class Line(
            val text: String,
            val font: FontChoice,
            val color: Int,
            /** 영역 높이 대비 기본 글자 크기 */
            val ratio: Float,
            val effect: Boolean,
            val divider: Boolean = false,
            emphasize: Boolean = false,
        ) {
            /** (글자, 크기 배율) 조각들. 숫자 강조가 켜지면 숫자가 아닌 부분은 작게 */
            val segments: List<Pair<String, Float>> =
                if (emphasize && text.any { it.isDigit() }) {
                    val out = ArrayList<Pair<String, Float>>()
                    val sb = StringBuilder()
                    var digit: Boolean? = null
                    for (ch in text) {
                        val isD = ch.isDigit() || (ch == ',' && digit == true)
                        if (digit != null && isD != digit) {
                            out += sb.toString() to (if (digit == true) 1f else s.minorScale.coerceIn(0.2f, 1f))
                            sb.clear()
                        }
                        sb.append(ch); digit = isD
                    }
                    if (sb.isNotEmpty()) out += sb.toString() to (if (digit == true) 1f else s.minorScale.coerceIn(0.2f, 1f))
                    out
                } else listOf(text to 1f)
            var size = 0f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = FontCache.get(appContext, font)
                this.color = this@Line.color
                letterSpacing = s.letterSpacing
            }

            fun resize(px: Float) { size = px; paint.textSize = px }
            fun width(): Float {
                if (divider) return 0f
                var total = 0f
                for ((t, k) in segments) { paint.textSize = size * k; total += paint.measureText(t) }
                paint.textSize = size
                return total
            }
            fun height() = if (divider) size else (paint.descent() - paint.ascent())
        }

        fun up(t: String) = if (s.uppercase) t.uppercase() else t

        fun drawText(area: RectF) {
            if (area.width() <= 4 || area.height() <= 4) return
            val title = if (s.showTitle) up(content.title) else ""
            val count = up(content.count)
            val date = if (s.showDate) content.date else ""
            val ls = s.labelSize
            val ns = s.numberSize
            val layout = if (s.layout == LayoutStyle.SPLIT && area.width() / area.height() < 1.25f) LayoutStyle.CLASSIC else s.layout

            when (layout) {
                LayoutStyle.CLASSIC -> stack(area, listOfNotNull(
                    title.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.labelColor, 0.21f * ls, true) },
                    Line(count, s.numberFont, s.numberColor, 0.58f * ns, true, emphasize = s.emphasizeDigits),
                    date.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.subColor, 0.15f * ls, false) },
                ), s.align)
                LayoutStyle.LABEL_TOP -> stack(area, listOfNotNull(
                    title.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.labelColor, 0.24f * ls, true) },
                    Line("", s.labelFont, s.numberColor, 0.06f, false, divider = true),
                    Line(count, s.numberFont, s.numberColor, 0.54f * ns, true, emphasize = s.emphasizeDigits),
                    date.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.subColor, 0.14f * ls, false) },
                ), s.align)
                LayoutStyle.MINIMAL -> stack(area, listOfNotNull(
                    Line(count, s.numberFont, s.numberColor, 0.78f * ns, true, emphasize = s.emphasizeDigits),
                    title.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.labelColor, 0.18f * ls, false) },
                ), s.align)
                LayoutStyle.TERMINAL -> stack(area, listOfNotNull(
                    title.takeIf { it.isNotEmpty() }?.let { Line(s.promptPrefix + it, s.labelFont, s.labelColor, 0.2f * ls, true) },
                    Line(count, s.numberFont, s.numberColor, 0.56f * ns, true, emphasize = s.emphasizeDigits),
                    (s.subPrefix + date + if (s.showCursor) "_" else "").trim().takeIf { it.isNotEmpty() }
                        ?.let { Line(s.subPrefix + date + if (s.showCursor) "_" else "", s.labelFont, s.subColor, 0.16f * ls, false) },
                ), s.align)
                LayoutStyle.SPLIT -> {
                    val gap = area.width() * 0.05f
                    val left = RectF(area.left, area.top, area.left + area.width() * 0.5f - gap / 2, area.bottom)
                    val right = RectF(area.left + area.width() * 0.5f + gap / 2, area.top, area.right, area.bottom)
                    stack(left, listOf(Line(count, s.numberFont, s.numberColor, 0.78f * ns, true, emphasize = s.emphasizeDigits)), TextAlign.CENTER)
                    val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mulAlpha(s.numberColor, 0.4f); strokeWidth = max(1f, d) }
                    val cx = area.centerX()
                    c.drawLine(cx, area.top + area.height() * 0.15f, cx, area.bottom - area.height() * 0.15f, dp)
                    stack(right, listOfNotNull(
                        title.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.labelColor, 0.27f * ls, true) },
                        date.takeIf { it.isNotEmpty() }?.let { Line(it, s.labelFont, s.subColor, 0.18f * ls, false) },
                    ), if (s.align == TextAlign.CENTER) TextAlign.START else s.align)
                }
            }
        }

        /** 줄들을 세로로 쌓고, 영역에 맞게 크기를 자동 조절해 그린다. */
        fun stack(area: RectF, lines: List<Line>, align: TextAlign) {
            if (lines.isEmpty()) return
            val gapRatio = 0.025f
            // 1) 기본 크기 + 너비에 맞추기
            for (l in lines) {
                l.resize(area.height() * l.ratio)
                if (!l.divider) {
                    val effectPad = effectPadding(l.size)
                    val wv = l.width() + effectPad * 2
                    if (wv > area.width()) l.resize(l.size * area.width() / wv)
                }
            }
            // 2) 높이에 맞추기
            val gap = area.height() * gapRatio
            var total = lines.sumOf { it.height().toDouble() }.toFloat() + gap * (lines.size - 1)
            if (total > area.height()) {
                val k = area.height() / total
                lines.forEach { it.resize(it.size * k) }
                total = area.height()
            }
            // 3) 그리기
            var y = area.top + (area.height() - total) / 2f
            for (l in lines) {
                if (l.divider) {
                    val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mulAlpha(l.color, 0.7f); strokeWidth = max(1f, l.size * 0.25f) }
                    val lw = area.width() * 0.35f
                    val x0 = when (align) {
                        TextAlign.START -> area.left
                        TextAlign.CENTER -> area.centerX() - lw / 2
                        TextAlign.END -> area.right - lw
                    }
                    c.drawLine(x0, y + l.size / 2, x0 + lw, y + l.size / 2, dp)
                    y += l.size + gap
                    continue
                }
                val baseline = y - l.paint.ascent()
                val pad = effectPadding(l.size)
                val x = when (align) {
                    TextAlign.START -> area.left + pad
                    TextAlign.CENTER -> area.centerX() - l.width() / 2
                    TextAlign.END -> area.right - l.width() - pad
                }
                var sx = x
                for ((t, k) in l.segments) {
                    l.paint.textSize = l.size * k
                    drawStyledText(l, t, sx, baseline, l.size * k)
                    sx += l.paint.measureText(t)
                }
                l.paint.textSize = l.size
                y += l.height() + gap
            }
        }

        fun effectPadding(size: Float): Float = when (s.textEffect) {
            TextEffect.CHROMATIC -> size * 0.05f * s.effectStrength
            TextEffect.LONG_SHADOW -> size * 0.08f * s.effectStrength
            TextEffect.OUTLINE -> size * 0.05f * s.effectStrength
            else -> 0f
        }

        fun drawStyledText(l: Line, text: String, x: Float, y: Float, k: Float) {
            val p = l.paint
            val st = s.effectStrength
            val effect = if (l.effect) s.textEffect else when (s.textEffect) {
                TextEffect.GLOW, TextEffect.SHADOW -> s.textEffect
                else -> TextEffect.NONE
            }
            when (effect) {
                TextEffect.NONE -> c.drawText(text, x, y, p)
                TextEffect.SHADOW -> {
                    p.setShadowLayer(max(0.5f, k * 0.06f * st), k * 0.04f * st, k * 0.05f * st, s.effectColor)
                    c.drawText(text, x, y, p)
                    p.clearShadowLayer()
                }
                TextEffect.GLOW -> {
                    p.setShadowLayer(max(1f, k * 0.28f * st), 0f, 0f, s.effectColor)
                    c.drawText(text, x, y, p)
                    p.setShadowLayer(max(0.5f, k * 0.09f * st), 0f, 0f, s.effectColor)
                    c.drawText(text, x, y, p)
                    p.clearShadowLayer()
                }
                TextEffect.OUTLINE -> {
                    val sp = Paint(p).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = k * 0.09f * st
                        strokeJoin = Paint.Join.ROUND
                        color = s.effectColor
                    }
                    c.drawText(text, x, y, sp)
                    c.drawText(text, x, y, p)
                }
                TextEffect.CHROMATIC -> {
                    val o = k * 0.035f * st
                    val p1 = Paint(p).apply { color = mulAlpha(s.effectColor, 0.85f) }
                    val p2 = Paint(p).apply { color = mulAlpha(s.labelColor, 0.75f) }
                    c.drawText(text, x - o, y, p1)
                    c.drawText(text, x + o, y + o * 0.3f, p2)
                    c.drawText(text, x, y, p)
                }
                TextEffect.LONG_SHADOW -> {
                    val n = max(1, (k * 0.08f * st).toInt())
                    val sp = Paint(p).apply { color = s.effectColor }
                    for (i in n downTo 1) c.drawText(text, x + i, y + i, sp)
                    c.drawText(text, x, y, p)
                }
            }
        }

        fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    }

    companion object {
        fun mulAlpha(color: Int, f: Float): Int {
            val a = (Color.alpha(color) * f.coerceIn(0f, 1f)).toInt()
            return (color and 0x00FFFFFF) or (a shl 24)
        }

        fun lighten(color: Int, f: Float): Int {
            val r = Color.red(color) + ((255 - Color.red(color)) * f).toInt()
            val g = Color.green(color) + ((255 - Color.green(color)) * f).toInt()
            val b = Color.blue(color) + ((255 - Color.blue(color)) * f).toInt()
            return Color.argb(Color.alpha(color), r, g, b)
        }
    }
}

object FontCache {
    private val cache = HashMap<FontChoice, Typeface>()

    @Synchronized
    fun get(context: Context, font: FontChoice): Typeface = cache.getOrPut(font) {
        val res = font.resId
        if (res != null) {
            runCatching { ResourcesCompat.getFont(context, res) }.getOrNull() ?: Typeface.DEFAULT
        } else {
            Typeface.create(font.systemFamily ?: "sans-serif", Typeface.NORMAL)
        }
    }
}
