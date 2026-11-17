package com.ddayforge.widget.style

/**
 * 기본 테마 세트. 각 테마는 StyleElements의 부품 + 색/수치의 조합일 뿐이며,
 * 테마 전용 그리기 코드는 하나도 없다. (= 모든 요소가 커스터마이즈에서 공유됨)
 */
object ThemePresets {

    private fun c(hex: Long): Int = hex.toInt()

    val CYBERPUNK = Theme(
        id = "cyberpunk", name = "사이버펑크", description = "네온 시안·마젠타, 깎인 모서리, 색수차 글리치",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ASYM_CUT, cornerRadius = 18f, padding = 16f,
            fill = BackgroundFill.LINEAR_GRADIENT, bgColor = c(0xFF0B0221), bgColor2 = c(0xFF2A0845), gradientAngle = 120f,
            texture = Texture.GRID, textureColor = c(0xFFFF2A6D), textureOpacity = 0.18f, textureScale = 1f,
            border = BorderStyle.NEON, borderColor = c(0xFF05D9E8), borderWidth = 2f,
            decoration = Decoration.CORNER_BRACKETS, decorationColor = c(0xFFF9F002),
            numberFont = FontChoice.ORBITRON, labelFont = FontChoice.RAJDHANI,
            numberColor = c(0xFFF9F002), labelColor = c(0xFF05D9E8), subColor = c(0xFFFF2A6D),
            textEffect = TextEffect.CHROMATIC, effectColor = c(0xFFFF2A6D), effectStrength = 1f,
            uppercase = true, letterSpacing = 0.05f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.START, countFormat = CountFormat.DDAY,
        ),
    )

    val RETRO_TERMINAL = Theme(
        id = "retro_terminal", name = "레트로 터미널", description = "차콜 화면, 노란 인광 글로우, 인터레이스 노이즈",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 22f, padding = 18f,
            fill = BackgroundFill.RADIAL_GRADIENT, bgColor = c(0xFF34353B), bgColor2 = c(0xFF232428),
            texture = Texture.INTERLACE, textureColor = c(0xFF141417), textureOpacity = 0.55f, textureScale = 1f,
            border = BorderStyle.NONE, borderColor = c(0xFF000000),
            decoration = Decoration.NONE, decorationColor = c(0xFFFFD23F),
            numberFont = FontChoice.UBUNTU_MONO, labelFont = FontChoice.UBUNTU_MONO,
            numberColor = c(0xFFFFD23F), labelColor = c(0xFFF2C94C), subColor = c(0xFFE8B931),
            textEffect = TextEffect.GLOW, effectColor = c(0xFFFFB800), effectStrength = 0.85f,
            numberSize = 1.2f, labelSize = 1.0f,
            layout = LayoutStyle.TERMINAL, align = TextAlign.START, countFormat = CountFormat.DDAY,
            showCursor = false, promptPrefix = "user@dday:~$ ./", subPrefix = "> ",
            emphasizeDigits = true, minorScale = 0.55f,
        ),
    )

    val GREEN_PHOSPHOR = Theme(
        id = "green_phosphor", name = "그린 포스퍼", description = "녹색 인광 CRT, 타이틀바, 커서",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 10f, padding = 12f,
            fill = BackgroundFill.RADIAL_GRADIENT, bgColor = c(0xFF0F2A14), bgColor2 = c(0xFF020803),
            texture = Texture.CRT, textureColor = c(0xFF000000), textureOpacity = 0.45f,
            border = BorderStyle.SOLID, borderColor = c(0xFF1F5F2A), borderWidth = 2f,
            decoration = Decoration.TERMINAL_BAR, decorationColor = c(0xFF33FF66),
            numberFont = FontChoice.VT323, labelFont = FontChoice.NANUM_CODING,
            numberColor = c(0xFF33FF66), labelColor = c(0xFF33FF66), subColor = c(0xFF1FAF45),
            textEffect = TextEffect.GLOW, effectColor = c(0xFF33FF66), effectStrength = 0.9f,
            numberSize = 1.15f,
            layout = LayoutStyle.TERMINAL, align = TextAlign.START, countFormat = CountFormat.DDAY,
            showCursor = true, promptPrefix = "$ ", subPrefix = "// ",
        ),
    )

    val AMBER_MONITOR = Theme(
        id = "amber", name = "앰버 모니터", description = "호박색 단색 모니터, 주사선",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.RECTANGLE, cornerRadius = 4f, padding = 14f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF140C00),
            texture = Texture.SCANLINES, textureColor = c(0xFF000000), textureOpacity = 0.5f,
            border = BorderStyle.DOUBLE, borderColor = c(0xFFFFB000), borderWidth = 1.5f,
            decoration = Decoration.NONE, decorationColor = c(0xFFFFB000),
            numberFont = FontChoice.SHARE_TECH_MONO, labelFont = FontChoice.NANUM_CODING,
            numberColor = c(0xFFFFB000), labelColor = c(0xFFFFC94D), subColor = c(0xFFB37B00),
            textEffect = TextEffect.GLOW, effectColor = c(0xFFFF8C00), effectStrength = 0.7f,
            uppercase = true,
            layout = LayoutStyle.TERMINAL, align = TextAlign.START, countFormat = CountFormat.DAYS,
            showCursor = true, promptPrefix = "> ",
        ),
    )

    val SYNTHWAVE = Theme(
        id = "synthwave", name = "신스웨이브", description = "80년대 석양 그라데이션과 지평선 격자",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 24f, padding = 14f,
            fill = BackgroundFill.LINEAR_GRADIENT, bgColor = c(0xFF1A0B3D), bgColor2 = c(0xFFB0305C), gradientAngle = 90f,
            texture = Texture.PERSPECTIVE_GRID, textureColor = c(0xFFFF6AD5), textureOpacity = 0.55f,
            border = BorderStyle.NONE, borderColor = c(0xFFFF6AD5),
            decoration = Decoration.NONE, decorationColor = c(0xFFFFD319),
            numberFont = FontChoice.AUDIOWIDE, labelFont = FontChoice.AUDIOWIDE,
            numberColor = c(0xFFFFD319), labelColor = c(0xFFFFFFFF), subColor = c(0xFFFFB3E6),
            textEffect = TextEffect.GLOW, effectColor = c(0xFFFF2975), effectStrength = 1f,
            uppercase = true, letterSpacing = 0.08f,
            layout = LayoutStyle.LABEL_TOP, align = TextAlign.CENTER, countFormat = CountFormat.DDAY,
        ),
    )

    val ARCADE = Theme(
        id = "arcade", name = "8비트 아케이드", description = "픽셀 폰트, 계단 모서리, 굵은 테두리",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.PIXEL, cornerRadius = 12f, padding = 14f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF222034),
            texture = Texture.DOTS, textureColor = c(0xFFFFFFFF), textureOpacity = 0.06f,
            border = BorderStyle.SOLID, borderColor = c(0xFFFFFFFF), borderWidth = 3f,
            decoration = Decoration.PIXEL_CORNERS, decorationColor = c(0xFFFBF236),
            numberFont = FontChoice.PRESS_START, labelFont = FontChoice.DO_HYEON,
            numberColor = c(0xFFFBF236), labelColor = c(0xFFFFFFFF), subColor = c(0xFF99E550),
            textEffect = TextEffect.LONG_SHADOW, effectColor = c(0xFFD95763), effectStrength = 1f,
            numberSize = 0.9f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.CENTER, countFormat = CountFormat.DDAY,
        ),
    )

    val LCD_POCKET = Theme(
        id = "lcd", name = "LCD 포켓", description = "초록 액정 휴대용 게임기 감성",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 14f, padding = 14f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF9BBC0F),
            texture = Texture.LCD, textureColor = c(0xFF306230), textureOpacity = 0.18f,
            border = BorderStyle.SOLID, borderColor = c(0xFF0F380F), borderWidth = 3f,
            decoration = Decoration.NONE, decorationColor = c(0xFF0F380F),
            numberFont = FontChoice.SILKSCREEN, labelFont = FontChoice.DO_HYEON,
            numberColor = c(0xFF0F380F), labelColor = c(0xFF0F380F), subColor = c(0xFF306230),
            textEffect = TextEffect.SHADOW, effectColor = c(0x55306230), effectStrength = 0.6f,
            layout = LayoutStyle.SPLIT, align = TextAlign.START, countFormat = CountFormat.DDAY,
        ),
    )

    val VAPORWAVE = Theme(
        id = "vaporwave", name = "베이퍼웨이브", description = "파스텔 그라데이션, 하프톤",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.CHAMFER, cornerRadius = 14f, padding = 14f,
            fill = BackgroundFill.LINEAR_GRADIENT, bgColor = c(0xFFFF71CE), bgColor2 = c(0xFF01CDFE), gradientAngle = 45f,
            texture = Texture.HALFTONE, textureColor = c(0xFFFFFFFF), textureOpacity = 0.35f,
            border = BorderStyle.OFFSET, borderColor = c(0xFF05FFA1), borderWidth = 3f,
            decoration = Decoration.NONE, decorationColor = c(0xFFFFFB96),
            numberFont = FontChoice.MAJOR_MONO, labelFont = FontChoice.SYSTEM_SERIF,
            numberColor = c(0xFFFFFFFF), labelColor = c(0xFFFFFFFF), subColor = c(0xFFFFFB96),
            textEffect = TextEffect.CHROMATIC, effectColor = c(0xFFB967FF), effectStrength = 1f,
            letterSpacing = 0.1f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.CENTER, countFormat = CountFormat.DDAY,
        ),
    )

    val CARBON_HUD = Theme(
        id = "carbon_hud", name = "카본 HUD", description = "카본 파이버와 군용 HUD 눈금",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.CHAMFER, cornerRadius = 10f, padding = 16f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF16181B),
            texture = Texture.CARBON, textureColor = c(0xFFFFFFFF), textureOpacity = 0.08f,
            border = BorderStyle.SOLID, borderColor = c(0xFF3A3F45), borderWidth = 1.5f,
            decoration = Decoration.HUD, decorationColor = c(0xFFFF6B00),
            numberFont = FontChoice.RAJDHANI, labelFont = FontChoice.RAJDHANI,
            numberColor = c(0xFFFFFFFF), labelColor = c(0xFFFF6B00), subColor = c(0xFF8A9199),
            textEffect = TextEffect.NONE, effectColor = c(0xFFFF6B00),
            numberSize = 1.2f, uppercase = true, letterSpacing = 0.12f,
            layout = LayoutStyle.SPLIT, align = TextAlign.START, countFormat = CountFormat.DDAY,
        ),
    )

    val NIGHT_SKY = Theme(
        id = "night_sky", name = "밤하늘", description = "별이 뜬 남색 하늘, 우아한 세리프",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 28f, padding = 16f,
            fill = BackgroundFill.RADIAL_GRADIENT, bgColor = c(0xFF2B3A67), bgColor2 = c(0xFF0B1026),
            texture = Texture.STARS, textureColor = c(0xFFFFFFFF), textureOpacity = 0.8f,
            border = BorderStyle.NONE,
            decoration = Decoration.NONE,
            numberFont = FontChoice.PLAYFAIR, labelFont = FontChoice.SYSTEM_SERIF,
            numberColor = c(0xFFFFF4D6), labelColor = c(0xFFE8E6F0), subColor = c(0xFF9AA5C9),
            textEffect = TextEffect.GLOW, effectColor = c(0x88FFE9A8), effectStrength = 0.6f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.CENTER, countFormat = CountFormat.KOREAN,
        ),
    )

    val TYPEWRITER = Theme(
        id = "typewriter", name = "타자기 메모", description = "종이 질감, 마스킹 테이프",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.RECTANGLE, cornerRadius = 2f, padding = 16f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFFF3EAD3),
            texture = Texture.PAPER, textureColor = c(0xFF7A6A4F), textureOpacity = 0.25f,
            border = BorderStyle.NONE,
            decoration = Decoration.TAPE, decorationColor = c(0xCCE8C07D),
            numberFont = FontChoice.SPECIAL_ELITE, labelFont = FontChoice.NANUM_CODING,
            numberColor = c(0xFF2B2B2B), labelColor = c(0xFF3D3D3D), subColor = c(0xFF8A7F6A),
            textEffect = TextEffect.NONE,
            layout = LayoutStyle.CLASSIC, align = TextAlign.START, countFormat = CountFormat.DDAY,
        ),
    )

    val POP_COMIC = Theme(
        id = "pop", name = "팝 코믹", description = "노란 하프톤, 굵은 외곽선, 오프셋 그림자",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 12f, padding = 14f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFFFFD60A),
            texture = Texture.HALFTONE, textureColor = c(0xFFFF4D00), textureOpacity = 0.25f,
            border = BorderStyle.OFFSET, borderColor = c(0xFF111111), borderWidth = 3f,
            decoration = Decoration.NONE,
            numberFont = FontChoice.BUNGEE, labelFont = FontChoice.BLACK_HAN_SANS,
            numberColor = c(0xFFFFFFFF), labelColor = c(0xFF111111), subColor = c(0xFF333333),
            textEffect = TextEffect.OUTLINE, effectColor = c(0xFF111111), effectStrength = 1.2f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.CENTER, countFormat = CountFormat.DDAY,
        ),
    )

    val INDUSTRIAL = Theme(
        id = "industrial", name = "인더스트리얼", description = "경고 줄무늬와 스텐실 감성",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.RECTANGLE, cornerRadius = 6f, padding = 14f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF2A2A2A),
            texture = Texture.NOISE, textureColor = c(0xFFFFFFFF), textureOpacity = 0.08f,
            border = BorderStyle.NONE,
            decoration = Decoration.WARNING_STRIPES, decorationColor = c(0xFFFFC300),
            numberFont = FontChoice.BLACK_HAN_SANS, labelFont = FontChoice.BLACK_HAN_SANS,
            numberColor = c(0xFFFFC300), labelColor = c(0xFFEEEEEE), subColor = c(0xFF999999),
            textEffect = TextEffect.SHADOW, effectColor = c(0xFF000000), effectStrength = 0.8f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.CENTER, countFormat = CountFormat.DDAY,
        ),
    )

    val NEON_SIGN = Theme(
        id = "neon_sign", name = "네온사인", description = "벽돌 벽 위 핑크 네온",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.PILL, cornerRadius = 20f, padding = 18f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF120A14),
            texture = Texture.HEX, textureColor = c(0xFFFFFFFF), textureOpacity = 0.05f,
            border = BorderStyle.NEON, borderColor = c(0xFFFF3EA5), borderWidth = 2.5f,
            decoration = Decoration.NONE,
            numberFont = FontChoice.MONOTON, labelFont = FontChoice.PACIFICO,
            numberColor = c(0xFFFFE3F3), labelColor = c(0xFF7DF9FF), subColor = c(0xFFFF9BD2),
            textEffect = TextEffect.GLOW, effectColor = c(0xFFFF3EA5), effectStrength = 1.2f,
            layout = LayoutStyle.SPLIT, align = TextAlign.CENTER, countFormat = CountFormat.NUMBER,
        ),
    )

    val PASTEL_DIARY = Theme(
        id = "pastel", name = "파스텔 다이어리", description = "말랑한 손글씨와 사이드 스트라이프",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.TICKET, cornerRadius = 22f, padding = 16f,
            fill = BackgroundFill.LINEAR_GRADIENT, bgColor = c(0xFFFFE5EC), bgColor2 = c(0xFFE2ECFF), gradientAngle = 0f,
            texture = Texture.DOTS, textureColor = c(0xFFFF8FAB), textureOpacity = 0.18f,
            border = BorderStyle.DASHED, borderColor = c(0xFFFFB3C6), borderWidth = 2f,
            decoration = Decoration.SIDE_STRIPE, decorationColor = c(0xFFFF8FAB),
            numberFont = FontChoice.GAEGU, labelFont = FontChoice.GAEGU,
            numberColor = c(0xFFE05780), labelColor = c(0xFF6B4E71), subColor = c(0xFF9C89A1),
            textEffect = TextEffect.NONE,
            numberSize = 1.1f, labelSize = 1.15f,
            layout = LayoutStyle.CLASSIC, align = TextAlign.CENTER, countFormat = CountFormat.KOREAN,
        ),
    )

    val MINIMAL_DARK = Theme(
        id = "minimal_dark", name = "미니멀 다크", description = "깔끔한 반투명 다크",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 24f, padding = 16f,
            fill = BackgroundFill.SOLID, bgColor = c(0xFF121212), bgOpacity = 0.85f,
            texture = Texture.NONE,
            numberFont = FontChoice.SYSTEM_BOLD, labelFont = FontChoice.SYSTEM,
            numberColor = c(0xFFFFFFFF), labelColor = c(0xFFDDDDDD), subColor = c(0xFF888888),
            layout = LayoutStyle.CLASSIC, align = TextAlign.START, countFormat = CountFormat.DDAY,
        ),
    )

    val MINIMAL_GLASS = Theme(
        id = "glass", name = "글래스", description = "배경이 비치는 반투명 유리",
        builtIn = true,
        style = WidgetStyle(
            shape = WidgetShape.ROUNDED, cornerRadius = 26f, padding = 16f,
            fill = BackgroundFill.LINEAR_GRADIENT, bgColor = c(0xFFFFFFFF), bgColor2 = c(0xFFB8C6DB), gradientAngle = 135f,
            bgOpacity = 0.25f,
            texture = Texture.NOISE, textureColor = c(0xFFFFFFFF), textureOpacity = 0.12f,
            border = BorderStyle.SOLID, borderColor = c(0x66FFFFFF), borderWidth = 1f,
            numberFont = FontChoice.SYSTEM_BOLD, labelFont = FontChoice.SYSTEM,
            numberColor = c(0xFFFFFFFF), labelColor = c(0xFFFFFFFF), subColor = c(0xCCFFFFFF),
            textEffect = TextEffect.SHADOW, effectColor = c(0x66000000), effectStrength = 0.6f,
            layout = LayoutStyle.MINIMAL, align = TextAlign.CENTER, countFormat = CountFormat.DDAY,
        ),
    )

    val ALL: List<Theme> = listOf(
        CYBERPUNK, RETRO_TERMINAL, GREEN_PHOSPHOR, AMBER_MONITOR, SYNTHWAVE, ARCADE, LCD_POCKET, VAPORWAVE,
        CARBON_HUD, NEON_SIGN, NIGHT_SKY, TYPEWRITER, POP_COMIC, INDUSTRIAL, PASTEL_DIARY,
        MINIMAL_DARK, MINIMAL_GLASS,
    )

    val DEFAULT = CYBERPUNK
}
