package com.ddayforge.widget.style

import kotlinx.serialization.Serializable

/**
 * 위젯 하나의 완전한 스타일. 모든 값은 서로 독립적인 부품(StyleElements)과 수치로 이루어진다.
 * 색상은 0xAARRGGBB 형식의 Int.
 */
@Serializable
data class WidgetStyle(
    // 형태
    val shape: WidgetShape = WidgetShape.ROUNDED,
    val cornerRadius: Float = 20f, // dp
    val padding: Float = 14f, // dp
    /** 홈 화면 런처의 둥근 모서리 잘림을 피하도록 안쪽으로 맞춤 */
    val clipGuard: Boolean = true,

    // 배경
    val fill: BackgroundFill = BackgroundFill.SOLID,
    val bgColor: Int = 0xFF1E1E24.toInt(),
    val bgColor2: Int = 0xFF3A3A48.toInt(),
    val gradientAngle: Float = 90f, // 도. 0 = 왼→오, 90 = 위→아래
    val bgOpacity: Float = 1f, // 0..1

    // 텍스처
    val texture: Texture = Texture.NONE,
    val textureColor: Int = 0xFFFFFFFF.toInt(),
    val textureOpacity: Float = 0.15f,
    val textureScale: Float = 1f,

    // 테두리
    val border: BorderStyle = BorderStyle.NONE,
    val borderColor: Int = 0xFFFFFFFF.toInt(),
    val borderWidth: Float = 2f, // dp

    // 장식
    val decoration: Decoration = Decoration.NONE,
    val decorationColor: Int = 0xFFFFFFFF.toInt(),

    // 글꼴/텍스트
    val numberFont: FontChoice = FontChoice.SYSTEM_BOLD,
    val labelFont: FontChoice = FontChoice.SYSTEM,
    val numberColor: Int = 0xFFFFFFFF.toInt(),
    val labelColor: Int = 0xFFE0E0E0.toInt(),
    val subColor: Int = 0xFF9E9E9E.toInt(),
    val numberSize: Float = 1f, // 배율
    val labelSize: Float = 1f, // 배율
    val letterSpacing: Float = 0f, // em
    val textEffect: TextEffect = TextEffect.NONE,
    val effectColor: Int = 0xFF000000.toInt(),
    val effectStrength: Float = 1f,
    val uppercase: Boolean = false,

    // 레이아웃/표시
    val layout: LayoutStyle = LayoutStyle.CLASSIC,
    val align: TextAlign = TextAlign.CENTER,
    val countFormat: CountFormat = CountFormat.DDAY,
    val showTitle: Boolean = true,
    val showDate: Boolean = true,
    val showCursor: Boolean = false,
    /** 터미널 레이아웃 첫 줄 앞에 붙는 프롬프트 */
    val promptPrefix: String = "> ",
    /** 터미널 레이아웃 마지막 줄(날짜) 앞에 붙는 프롬프트 */
    val subPrefix: String = "// ",
    /** 숫자만 크게, "D-", "일 남음" 같은 나머지 글자는 작게 */
    val emphasizeDigits: Boolean = false,
    val minorScale: Float = 0.6f,
)

@Serializable
data class Theme(
    val id: String,
    val name: String,
    val description: String = "",
    val style: WidgetStyle,
    val builtIn: Boolean = false,
)
