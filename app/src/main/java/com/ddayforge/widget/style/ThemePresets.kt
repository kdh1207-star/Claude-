package com.ddayforge.widget.style

/**
 * 기본 테마(프리셋) 세트. 각 테마는 StyleElements의 부품 + 색/수치의 조합일 뿐이며,
 * 테마 전용 그리기 코드는 없다. (= 모든 요소가 커스터마이즈에서 공유됨)
 *
 * 테마는 하나씩 새로 만드는 중이라 지금은 비어 있다.
 */
object ThemePresets {

    @Suppress("unused")
    private fun c(hex: Long): Int = hex.toInt()

    val ALL: List<Theme> = emptyList()

    /** 테마를 고르지 않은 새 위젯이 쓰는 기본 스타일 (꾸밈 없는 다크) */
    val DEFAULT_STYLE = WidgetStyle()
}
