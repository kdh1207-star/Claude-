package com.ddayforge.widget.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

@Serializable
enum class CountMode(val label: String, val description: String) {
    COUNTDOWN("디데이", "목표일까지 남은 날 (D-3, D-DAY, D+2)"),
    DAYS_SINCE("기념일", "시작일부터 며칠째인지 (시작일 = 1일)"),
}

@Serializable
data class DDay(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    /** LocalDate.toEpochDay() */
    val epochDay: Long,
    val mode: CountMode = CountMode.COUNTDOWN,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(epochDay)
}

/** 계산 결과. 표시 형식(CountFormat)과 분리되어 있어서 어떤 테마든 같은 값을 쓴다. */
data class DDayValue(
    /** 표시할 숫자(절댓값) */
    val number: Long,
    val state: State,
) {
    enum class State { BEFORE, TODAY, AFTER, SINCE }
}

object DDayCalc {
    fun compute(dday: DDay, today: LocalDate = LocalDate.now()): DDayValue {
        val diff = ChronoUnit.DAYS.between(today, dday.date) // 목표일 - 오늘
        return when (dday.mode) {
            CountMode.COUNTDOWN -> when {
                diff > 0 -> DDayValue(diff, DDayValue.State.BEFORE)
                diff == 0L -> DDayValue(0, DDayValue.State.TODAY)
                else -> DDayValue(-diff, DDayValue.State.AFTER)
            }
            CountMode.DAYS_SINCE -> if (diff > 0) {
                DDayValue(diff, DDayValue.State.BEFORE)
            } else {
                DDayValue(-diff + 1, DDayValue.State.SINCE)
            }
        }
    }

    fun formatDate(date: LocalDate): String {
        val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        return "%04d.%02d.%02d (%s)".format(date.year, date.monthValue, date.dayOfMonth, dow)
    }
}
