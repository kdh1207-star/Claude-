package com.ddayforge.widget.data

import android.content.Context
import com.ddayforge.widget.style.Theme
import com.ddayforge.widget.style.WidgetStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class WidgetConfig(
    val appWidgetId: Int,
    val ddayId: String,
    val style: WidgetStyle,
    /** 마지막으로 고른 테마 이름 (표시용) */
    val baseThemeId: String? = null,
)

@Serializable
data class AppData(
    val ddays: List<DDay> = emptyList(),
    val widgets: Map<Int, WidgetConfig> = emptyMap(),
    val customThemes: List<Theme> = emptyList(),
)

/**
 * 아주 작은 JSON 파일 저장소. 위젯 프로바이더(브로드캐스트)와 UI 양쪽에서 동기적으로 읽을 수 있게
 * 단순하게 유지한다.
 */
object Repository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val lock = Any()
    private var file: File? = null
    private val _state = MutableStateFlow(AppData())
    val state: StateFlow<AppData> = _state.asStateFlow()
    private var loaded = false

    fun init(context: Context) {
        synchronized(lock) {
            if (loaded) return
            val f = File(context.applicationContext.filesDir, "dday_data.json")
            file = f
            _state.value = runCatching { json.decodeFromString<AppData>(f.readText()) }.getOrElse { AppData() }
            loaded = true
        }
    }

    fun get(context: Context): AppData {
        init(context)
        return _state.value
    }

    private fun update(context: Context, transform: (AppData) -> AppData) {
        init(context)
        synchronized(lock) {
            val next = transform(_state.value)
            _state.value = next
            val f = file!!
            val tmp = File(f.parentFile, f.name + ".tmp")
            tmp.writeText(json.encodeToString(AppData.serializer(), next))
            tmp.renameTo(f)
        }
    }

    // --- D-Day ---
    fun upsertDDay(context: Context, dday: DDay) = update(context) { d ->
        val exists = d.ddays.any { it.id == dday.id }
        d.copy(ddays = if (exists) d.ddays.map { if (it.id == dday.id) dday else it } else d.ddays + dday)
    }

    fun deleteDDay(context: Context, id: String) = update(context) { d ->
        d.copy(ddays = d.ddays.filterNot { it.id == id })
    }

    fun findDDay(context: Context, id: String): DDay? = get(context).ddays.firstOrNull { it.id == id }

    // --- 위젯 ---
    fun saveWidget(context: Context, config: WidgetConfig) = update(context) { d ->
        d.copy(widgets = d.widgets + (config.appWidgetId to config))
    }

    fun removeWidgets(context: Context, ids: IntArray) = update(context) { d ->
        d.copy(widgets = d.widgets - ids.toSet())
    }

    fun widget(context: Context, id: Int): WidgetConfig? = get(context).widgets[id]

    // --- 내 테마 ---
    fun saveCustomTheme(context: Context, theme: Theme) = update(context) { d ->
        d.copy(customThemes = d.customThemes.filterNot { it.id == theme.id } + theme)
    }

    fun deleteCustomTheme(context: Context, id: String) = update(context) { d ->
        d.copy(customThemes = d.customThemes.filterNot { it.id == id })
    }
}
