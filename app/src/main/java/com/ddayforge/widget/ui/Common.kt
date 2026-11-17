@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.ddayforge.widget.ui

import android.graphics.Color as AColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ddayforge.widget.render.WidgetContent
import com.ddayforge.widget.render.WidgetRenderer
import com.ddayforge.widget.style.FontChoice
import com.ddayforge.widget.style.StyleElement
import com.ddayforge.widget.style.ThemePresets
import com.ddayforge.widget.style.WidgetStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val Amber = Color(0xFFFFD23F)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Amber,
            onPrimary = Color(0xFF1A1A1A),
            secondary = Color(0xFF05D9E8),
            background = Color(0xFF121214),
            surface = Color(0xFF18181B),
            surfaceVariant = Color(0xFF26262B),
            surfaceContainer = Color(0xFF1E1E22),
            surfaceContainerHigh = Color(0xFF26262B),
        ),
        content = content,
    )
}

/** 위젯 렌더러로 그린 미리보기. 렌더링은 백그라운드 스레드에서 한다. */
@Composable
fun WidgetPreview(
    style: WidgetStyle,
    content: WidgetContent,
    widthDp: Float,
    heightDp: Float,
    modifier: Modifier = Modifier,
    displayWidth: Dp = widthDp.dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val renderer = remember { WidgetRenderer(context) }
    val bitmap by produceState<ImageBitmap?>(null, style, content, widthDp, heightDp) {
        value = withContext(Dispatchers.Default) {
            renderer.render(style, content, widthDp, heightDp, density).asImageBitmap()
        }
    }
    val displayHeight = displayWidth * (heightDp / widthDp)
    Box(modifier.size(displayWidth, displayHeight), contentAlignment = Alignment.Center) {
        bitmap?.let {
            Image(it, contentDescription = content.count, contentScale = ContentScale.Fit, modifier = Modifier.size(displayWidth, displayHeight))
        }
    }
}

/** 편집기용 체크무늬 배경 (투명도 확인용) */
fun Modifier.checkerBackground(): Modifier = this.background(Color(0xFF3A3A40))

fun FontChoice.composeFamily(): FontFamily = when {
    resId != null -> FontFamily(Font(resId))
    systemFamily == "monospace" -> FontFamily.Monospace
    systemFamily == "serif" -> FontFamily.Serif
    else -> FontFamily.SansSerif
}

@Composable
fun <T : StyleElement> ElementChips(
    values: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    fontOf: ((T) -> FontFamily)? = null,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        values.forEach { v ->
            FilterChip(
                selected = v == selected,
                onClick = { onSelect(v) },
                label = {
                    Text(v.label, fontFamily = fontOf?.invoke(v), fontSize = 13.sp)
                },
            )
        }
    }
}

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    format: (Float) -> String = { "%.1f".format(it) },
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(format(value), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value.coerceIn(range), onValueChange = onChange, valueRange = range)
    }
}

@Composable
fun LabeledSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** 모든 기본 테마에서 쓰인 색들 = 공유 팔레트 */
val SharedPalette: List<Int> by lazy {
    ThemePresets.ALL.flatMap {
        val s = it.style
        listOf(s.bgColor, s.bgColor2, s.numberColor, s.labelColor, s.subColor, s.borderColor, s.textureColor, s.decorationColor, s.effectColor)
    }.map { it or (0xFF shl 24) }.distinct() + listOf(0xFFFFFFFF.toInt(), 0xFF000000.toInt())
}

@Composable
fun ColorRow(label: String, color: Int, onChange: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable { open = true }.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(hex(color), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).checkerBackground()
                .background(Color(color)).border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(8.dp)),
        )
    }
    if (open) ColorPickerDialog(color, onDismiss = { open = false }, onPick = { onChange(it); open = false })
}

fun hex(c: Int): String = "#%08X".format(c)

@Composable
fun ColorPickerDialog(initial: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val hsv = remember { FloatArray(3).also { AColor.colorToHSV(initial, it) } }
    var h by remember { mutableFloatStateOf(hsv[0]) }
    var s by remember { mutableFloatStateOf(hsv[1]) }
    var v by remember { mutableFloatStateOf(hsv[2]) }
    var a by remember { mutableFloatStateOf(AColor.alpha(initial) / 255f) }
    val current = AColor.HSVToColor((a * 255).toInt(), floatArrayOf(h, s, v))
    var hexText by remember(current) { mutableStateOf(hex(current)) }

    fun setFrom(c: Int) {
        val t = FloatArray(3); AColor.colorToHSV(c, t)
        h = t[0]; s = t[1]; v = t[2]; a = AColor.alpha(c) / 255f
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onPick(current) }) { Text("적용") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        title = { Text("색상 선택") },
        text = {
            Column {
                Box(
                    Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).checkerBackground()
                        .background(Color(current)),
                )
                Spacer(Modifier.height(8.dp))
                Text("테마 팔레트", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SharedPalette.forEach { pc ->
                        Box(
                            Modifier.size(22.dp).clip(CircleShape).background(Color(pc))
                                .border(1.dp, Color(0x44FFFFFF), CircleShape)
                                .clickable { setFrom((pc and 0x00FFFFFF) or ((a * 255).toInt() shl 24)) },
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                LabeledSlider("색상", h, 0f..360f, { h = it }) { "${it.toInt()}°" }
                LabeledSlider("채도", s, 0f..1f, { s = it }) { "${(it * 100).toInt()}%" }
                LabeledSlider("밝기", v, 0f..1f, { v = it }) { "${(it * 100).toInt()}%" }
                LabeledSlider("불투명도", a, 0f..1f, { a = it }) { "${(it * 100).toInt()}%" }
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { t ->
                        hexText = t
                        parseHex(t)?.let { setFrom(it) }
                    },
                    label = { Text("HEX (#AARRGGBB 또는 #RRGGBB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}

fun parseHex(t: String): Int? {
    val x = t.trim().removePrefix("#")
    return when (x.length) {
        6 -> x.toLongOrNull(16)?.let { (0xFF000000 or it).toInt() }
        8 -> x.toLongOrNull(16)?.toInt()
        else -> null
    }
}
