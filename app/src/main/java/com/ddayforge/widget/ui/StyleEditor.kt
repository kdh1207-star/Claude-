@file:OptIn(ExperimentalMaterial3Api::class)

package com.ddayforge.widget.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ddayforge.widget.render.WidgetContent
import com.ddayforge.widget.style.BackgroundFill
import com.ddayforge.widget.style.BorderStyle
import com.ddayforge.widget.style.CountFormat
import com.ddayforge.widget.style.Decoration
import com.ddayforge.widget.style.FontChoice
import com.ddayforge.widget.style.LayoutStyle
import com.ddayforge.widget.style.TextAlign
import com.ddayforge.widget.style.TextEffect
import com.ddayforge.widget.style.Texture
import com.ddayforge.widget.style.Theme
import com.ddayforge.widget.style.WidgetShape
import com.ddayforge.widget.style.WidgetStyle

/**
 * 편집 섹션. 각 섹션은 스타일의 일부 필드만 담당하며, [take]로 다른 테마에서
 * 해당 부분만 가져올 수 있다. (예: 사이버펑크의 텍스처 + 레트로 터미널의 글꼴)
 */
private class Section(
    val title: String,
    val take: (target: WidgetStyle, src: WidgetStyle) -> WidgetStyle,
    val body: @Composable (WidgetStyle, (WidgetStyle) -> Unit) -> Unit,
)

private val sections: List<Section> = listOf(
    Section("형태", { t, s -> t.copy(shape = s.shape, cornerRadius = s.cornerRadius, padding = s.padding) }) { st, set ->
        ElementChips(WidgetShape.entries, st.shape, { set(st.copy(shape = it)) })
        LabeledSwitch("홈 화면 모서리 잘림 방지", st.clipGuard) { set(st.copy(clipGuard = it)) }
        LabeledSlider("모서리 크기", st.cornerRadius, 0f..60f, { set(st.copy(cornerRadius = it)) }) { "${it.toInt()}dp" }
        LabeledSlider("안쪽 여백", st.padding, 0f..40f, { set(st.copy(padding = it)) }) { "${it.toInt()}dp" }
    },
    Section("배경", { t, s ->
        t.copy(fill = s.fill, bgColor = s.bgColor, bgColor2 = s.bgColor2, gradientAngle = s.gradientAngle, bgOpacity = s.bgOpacity)
    }) { st, set ->
        ElementChips(BackgroundFill.entries, st.fill, { set(st.copy(fill = it)) })
        ColorRow(if (st.fill == BackgroundFill.SOLID) "배경 색" else "시작 색", st.bgColor) { set(st.copy(bgColor = it)) }
        if (st.fill != BackgroundFill.SOLID) ColorRow("끝 색", st.bgColor2) { set(st.copy(bgColor2 = it)) }
        if (st.fill == BackgroundFill.LINEAR_GRADIENT) {
            LabeledSlider("그라데이션 각도", st.gradientAngle, 0f..360f, { set(st.copy(gradientAngle = it)) }) { "${it.toInt()}°" }
        }
        LabeledSlider("배경 불투명도", st.bgOpacity, 0f..1f, { set(st.copy(bgOpacity = it)) }) { "${(it * 100).toInt()}%" }
    },
    Section("배경 텍스처", { t, s ->
        t.copy(texture = s.texture, textureColor = s.textureColor, textureOpacity = s.textureOpacity, textureScale = s.textureScale)
    }) { st, set ->
        ElementChips(Texture.entries, st.texture, { set(st.copy(texture = it)) })
        if (st.texture != Texture.NONE) {
            ColorRow("텍스처 색", st.textureColor) { set(st.copy(textureColor = it)) }
            LabeledSlider("텍스처 진하기", st.textureOpacity, 0f..1f, { set(st.copy(textureOpacity = it)) }) { "${(it * 100).toInt()}%" }
            LabeledSlider("텍스처 크기", st.textureScale, 0.4f..3f, { set(st.copy(textureScale = it)) }) { "x%.1f".format(it) }
        }
    },
    Section("테두리", { t, s -> t.copy(border = s.border, borderColor = s.borderColor, borderWidth = s.borderWidth) }) { st, set ->
        ElementChips(BorderStyle.entries, st.border, { set(st.copy(border = it)) })
        if (st.border != BorderStyle.NONE) {
            ColorRow("테두리 색", st.borderColor) { set(st.copy(borderColor = it)) }
            LabeledSlider("두께", st.borderWidth, 0.5f..8f, { set(st.copy(borderWidth = it)) }) { "%.1fdp".format(it) }
        }
    },
    Section("장식", { t, s -> t.copy(decoration = s.decoration, decorationColor = s.decorationColor) }) { st, set ->
        ElementChips(Decoration.entries, st.decoration, { set(st.copy(decoration = it)) })
        if (st.decoration != Decoration.NONE) ColorRow("장식 색", st.decorationColor) { set(st.copy(decorationColor = it)) }
    },
    Section("글꼴", { t, s ->
        t.copy(numberFont = s.numberFont, labelFont = s.labelFont, letterSpacing = s.letterSpacing, uppercase = s.uppercase)
    }) { st, set ->
        Text("숫자 글꼴", style = MaterialTheme.typography.labelLarge)
        ElementChips(FontChoice.entries, st.numberFont, { set(st.copy(numberFont = it)) }, fontOf = { it.composeFamily() })
        Spacer(Modifier.height(6.dp))
        Text("제목·날짜 글꼴", style = MaterialTheme.typography.labelLarge)
        ElementChips(FontChoice.entries, st.labelFont, { set(st.copy(labelFont = it)) }, fontOf = { it.composeFamily() })
        LabeledSlider("자간", st.letterSpacing, -0.1f..0.4f, { set(st.copy(letterSpacing = it)) }) { "%.2f".format(it) }
        LabeledSwitch("영문 대문자로", st.uppercase) { set(st.copy(uppercase = it)) }
    },
    Section("글자 색·크기", { t, s ->
        t.copy(
            numberColor = s.numberColor, labelColor = s.labelColor, subColor = s.subColor,
            numberSize = s.numberSize, labelSize = s.labelSize, emphasizeDigits = s.emphasizeDigits, minorScale = s.minorScale,
        )
    }) { st, set ->
        ColorRow("숫자 색", st.numberColor) { set(st.copy(numberColor = it)) }
        ColorRow("제목 색", st.labelColor) { set(st.copy(labelColor = it)) }
        ColorRow("날짜 색", st.subColor) { set(st.copy(subColor = it)) }
        LabeledSlider("숫자 크기", st.numberSize, 0.3f..2f, { set(st.copy(numberSize = it)) }) { "x%.2f".format(it) }
        LabeledSlider("제목·날짜 크기", st.labelSize, 0.3f..2f, { set(st.copy(labelSize = it)) }) { "x%.2f".format(it) }
        LabeledSwitch("숫자만 크게 (D-, 일 남음 등은 작게)", st.emphasizeDigits) { set(st.copy(emphasizeDigits = it)) }
        if (st.emphasizeDigits) {
            LabeledSlider("나머지 글자 비율", st.minorScale, 0.25f..1f, { set(st.copy(minorScale = it)) }) { "${(it * 100).toInt()}%" }
        }
    },
    Section("텍스트 효과", { t, s -> t.copy(textEffect = s.textEffect, effectColor = s.effectColor, effectStrength = s.effectStrength) }) { st, set ->
        ElementChips(TextEffect.entries, st.textEffect, { set(st.copy(textEffect = it)) })
        if (st.textEffect != TextEffect.NONE) {
            ColorRow("효과 색", st.effectColor) { set(st.copy(effectColor = it)) }
            LabeledSlider("효과 강도", st.effectStrength, 0f..2.5f, { set(st.copy(effectStrength = it)) }) { "x%.1f".format(it) }
        }
    },
    Section("배치·표시", { t, s ->
        t.copy(
            layout = s.layout, align = s.align, countFormat = s.countFormat, showTitle = s.showTitle, showDate = s.showDate,
            showCursor = s.showCursor, promptPrefix = s.promptPrefix, subPrefix = s.subPrefix,
        )
    }) { st, set ->
        Text("레이아웃", style = MaterialTheme.typography.labelLarge)
        ElementChips(LayoutStyle.entries, st.layout, { set(st.copy(layout = it)) })
        Text("정렬", style = MaterialTheme.typography.labelLarge)
        ElementChips(TextAlign.entries, st.align, { set(st.copy(align = it)) })
        Text("숫자 표시 형식", style = MaterialTheme.typography.labelLarge)
        ElementChips(CountFormat.entries, st.countFormat, { set(st.copy(countFormat = it)) })
        LabeledSwitch("제목 표시", st.showTitle) { set(st.copy(showTitle = it)) }
        LabeledSwitch("날짜 표시", st.showDate) { set(st.copy(showDate = it)) }
        if (st.layout == LayoutStyle.TERMINAL) {
            LabeledSwitch("커서(_) 표시", st.showCursor) { set(st.copy(showCursor = it)) }
            OutlinedTextField(
                st.promptPrefix, { set(st.copy(promptPrefix = it)) },
                label = { Text("첫 줄 프롬프트") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                st.subPrefix, { set(st.copy(subPrefix = it)) },
                label = { Text("마지막 줄 프롬프트") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
    },
)

private data class PreviewSize(val label: String, val w: Float, val h: Float)

@Composable
fun StyleEditor(
    style: WidgetStyle,
    onStyleChange: (WidgetStyle) -> Unit,
    content: WidgetContent,
    themes: List<Theme>,
    selectedThemeId: String?,
    onThemeSelected: (Theme) -> Unit,
    actualSize: Pair<Float, Float>?,
    modifier: Modifier = Modifier,
) {
    val sizes = remember(actualSize) {
        listOfNotNull(
            actualSize?.let { PreviewSize("실제 크기", it.first, it.second) },
            PreviewSize("4x2", 300f, 150f),
            PreviewSize("3x2", 230f, 150f),
            PreviewSize("2x2", 150f, 150f),
            PreviewSize("4x1", 300f, 70f),
        )
    }
    var size by remember(sizes) { mutableStateOf(sizes.first()) }

    Column(modifier) {
        // ---- 미리보기 ----
        Box(
            Modifier.fillMaxWidth().height(220.dp).checkerBackground().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maxW = 320f
            val scale = minOf(maxW / size.w, 196f / size.h, 1.4f)
            WidgetPreview(style, content, size.w, size.h, displayWidth = (size.w * scale).dp)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sizes) { s ->
                FilterChip(selected = s == size, onClick = { size = s }, label = { Text(s.label) })
            }
        }

        // ---- 테마 세트 ----
        Text(
            "테마", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 6.dp),
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(themes, key = { it.id }) { t ->
                val selected = t.id == selectedThemeId
                Card(
                    onClick = { onThemeSelected(t) },
                    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(6.dp).width(132.dp)) {
                        WidgetPreview(t.style, content, 200f, 110f, displayWidth = 132.dp)
                        Text(
                            t.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // ---- 세부 커스터마이즈 ----
        Text(
            "세부 커스터마이즈", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp),
        )
        Text(
            "각 항목의 '가져오기'로 다른 테마의 해당 요소만 골라 섞을 수 있어요.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
        )
        sections.forEach { sec ->
            SectionCard(sec, style, onStyleChange, themes)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionCard(sec: Section, style: WidgetStyle, onChange: (WidgetStyle) -> Unit, themes: List<Theme>) {
    var expanded by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(sec.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Box {
                TextButton(onClick = { menu = true }) { Text("가져오기") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    themes.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name) },
                            onClick = { onChange(sec.take(style, t.style)); menu = false },
                        )
                    }
                }
            }
            Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                sec.body(style, onChange)
            }
        }
    }
}
