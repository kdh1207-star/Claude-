package com.ddayforge.widget.style

import androidx.annotation.FontRes
import com.ddayforge.widget.R
import com.ddayforge.widget.data.DDayValue
import kotlinx.serialization.Serializable

/*
 * 위젯 디자인을 이루는 "부품"들.
 *
 * 기본 테마(ThemePresets)는 오직 여기 정의된 부품들의 조합으로만 만들어진다.
 * 그래서 사이버펑크 테마에 쓰인 격자 텍스처, 레트로 터미널의 CRT 주사선, 타이틀바 장식 등은
 * 커스터마이즈 화면에서 다른 어떤 테마와도 자유롭게 섞어 쓸 수 있다.
 * 새 부품을 추가하면 모든 테마/커스텀 편집기에서 자동으로 선택 가능해진다.
 */

/** 공통 인터페이스: 편집기에서 칩 목록으로 보여주기 위한 이름 */
interface StyleElement {
    val label: String
}

@Serializable
enum class WidgetShape(override val label: String) : StyleElement {
    RECTANGLE("사각형"),
    ROUNDED("둥근 사각형"),
    PILL("알약"),
    CIRCLE("원"),
    CHAMFER("모서리 깎기"),
    ASYM_CUT("비대칭 컷"),
    TICKET("티켓"),
    PIXEL("픽셀 계단"),
}

@Serializable
enum class BackgroundFill(override val label: String) : StyleElement {
    SOLID("단색"),
    LINEAR_GRADIENT("선형 그라데이션"),
    RADIAL_GRADIENT("원형 그라데이션"),
}

@Serializable
enum class Texture(override val label: String) : StyleElement {
    NONE("없음"),
    SCANLINES("주사선"),
    INTERLACE("인터레이스 노이즈"),
    CRT("CRT 모니터"),
    GRID("격자"),
    PERSPECTIVE_GRID("신스웨이브 지평선"),
    DOTS("도트"),
    HALFTONE("하프톤"),
    NOISE("노이즈"),
    DIAGONAL("사선"),
    CARBON("카본"),
    CIRCUIT("회로"),
    PAPER("종이"),
    STARS("별"),
    GLITCH("글리치"),
    HEX("육각형"),
    LCD("LCD 픽셀"),
}

@Serializable
enum class BorderStyle(override val label: String) : StyleElement {
    NONE("없음"),
    SOLID("실선"),
    DOUBLE("이중선"),
    DASHED("파선"),
    DOTTED("점선"),
    NEON("네온 글로우"),
    OFFSET("오프셋 그림자"),
}

@Serializable
enum class Decoration(override val label: String) : StyleElement {
    NONE("없음"),
    CORNER_BRACKETS("코너 브래킷"),
    TERMINAL_BAR("터미널 타이틀바"),
    HUD("HUD 눈금"),
    TAPE("마스킹 테이프"),
    PIXEL_CORNERS("픽셀 코너"),
    SIDE_STRIPE("사이드 스트라이프"),
    WARNING_STRIPES("경고 줄무늬"),
}

@Serializable
enum class FontChoice(override val label: String, @FontRes val resId: Int?, val systemFamily: String? = null) : StyleElement {
    SYSTEM("기본", null, "sans-serif"),
    SYSTEM_BOLD("기본 굵게", null, "sans-serif-black"),
    SYSTEM_MONO("기본 고정폭", null, "monospace"),
    SYSTEM_SERIF("기본 명조", null, "serif"),
    ORBITRON("Orbitron", R.font.orbitron),
    RAJDHANI("Rajdhani", R.font.rajdhani_bold),
    AUDIOWIDE("Audiowide", R.font.audiowide),
    VT323("VT323", R.font.vt323),
    SHARE_TECH_MONO("Share Tech Mono", R.font.share_tech_mono),
    UBUNTU_MONO("Ubuntu Mono", R.font.ubuntu_mono_bold),
    PRESS_START("Press Start 2P", R.font.press_start_2p),
    SILKSCREEN("Silkscreen", R.font.silkscreen),
    MONOTON("Monoton", R.font.monoton),
    BUNGEE("Bungee", R.font.bungee),
    MAJOR_MONO("Major Mono", R.font.major_mono_display),
    PACIFICO("Pacifico", R.font.pacifico),
    SPECIAL_ELITE("Special Elite", R.font.special_elite),
    PLAYFAIR("Playfair", R.font.playfair_display),
    BLACK_HAN_SANS("검은고딕", R.font.black_han_sans),
    DO_HYEON("도현체", R.font.do_hyeon),
    NANUM_CODING("나눔고딕코딩", R.font.nanum_gothic_coding),
    GAEGU("개구체", R.font.gaegu_bold),
}

@Serializable
enum class TextEffect(override val label: String) : StyleElement {
    NONE("없음"),
    SHADOW("그림자"),
    GLOW("글로우"),
    OUTLINE("외곽선"),
    CHROMATIC("색수차(글리치)"),
    LONG_SHADOW("입체 그림자"),
}

@Serializable
enum class LayoutStyle(override val label: String) : StyleElement {
    CLASSIC("클래식"),
    MINIMAL("숫자만 크게"),
    TERMINAL("터미널"),
    SPLIT("좌우 분할"),
    LABEL_TOP("라벨 강조"),
}

@Serializable
enum class TextAlign(override val label: String) : StyleElement {
    START("왼쪽"),
    CENTER("가운데"),
    END("오른쪽"),
}

@Serializable
enum class CountFormat(override val label: String) : StyleElement {
    DDAY("D-12"),
    KOREAN("12일 남음"),
    DAYS("12 DAYS"),
    NUMBER("12"),
    ;

    fun format(v: DDayValue): String = when (this) {
        DDAY -> when (v.state) {
            DDayValue.State.BEFORE -> "D-${v.number}"
            DDayValue.State.TODAY -> "D-DAY"
            DDayValue.State.AFTER -> "D+${v.number}"
            DDayValue.State.SINCE -> "D+${v.number}"
        }
        KOREAN -> when (v.state) {
            DDayValue.State.BEFORE -> "${v.number}일 남음"
            DDayValue.State.TODAY -> "오늘!"
            DDayValue.State.AFTER -> "${v.number}일 지남"
            DDayValue.State.SINCE -> "${v.number}일째"
        }
        DAYS -> when (v.state) {
            DDayValue.State.BEFORE -> "${v.number} DAYS"
            DDayValue.State.TODAY -> "TODAY"
            DDayValue.State.AFTER -> "+${v.number} DAYS"
            DDayValue.State.SINCE -> "DAY ${v.number}"
        }
        NUMBER -> when (v.state) {
            DDayValue.State.TODAY -> "0"
            DDayValue.State.AFTER -> "+${v.number}"
            else -> "${v.number}"
        }
    }
}
