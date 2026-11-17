@file:OptIn(ExperimentalMaterial3Api::class)

package com.ddayforge.widget.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ddayforge.widget.data.DDay
import com.ddayforge.widget.data.Repository
import com.ddayforge.widget.data.WidgetConfig
import com.ddayforge.widget.render.WidgetContent
import com.ddayforge.widget.style.Theme
import com.ddayforge.widget.style.ThemePresets
import com.ddayforge.widget.widget.DDayWidgetProvider
import java.util.UUID

class WidgetConfigActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RECONFIGURE = "reconfigure"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        // 설정을 끝내지 않고 나가면 위젯 추가 취소
        setResult(Activity.RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        Repository.init(this)
        val manager = AppWidgetManager.getInstance(this)
        val size = DDayWidgetProvider.widgetSizeDp(manager, widgetId)
        val hasRealSize = manager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) > 0

        setContent {
            AppTheme {
                ConfigScreen(
                    widgetId = widgetId,
                    actualSize = if (hasRealSize) size else null,
                    onCancel = { finish() },
                    onSaved = {
                        DDayWidgetProvider.updateWidget(this, manager, widgetId)
                        DDayWidgetProvider.scheduleMidnight(this)
                        setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfigScreen(
    widgetId: Int,
    actualSize: Pair<Float, Float>?,
    onCancel: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val data by Repository.state.collectAsState()
    val existing = remember { data.widgets[widgetId] }

    var step by remember { mutableStateOf(if (existing != null && data.ddays.any { it.id == existing.ddayId }) 1 else 0) }
    var ddayId by remember { mutableStateOf(existing?.ddayId ?: data.ddays.firstOrNull()?.id) }
    var style by remember { mutableStateOf(existing?.style ?: ThemePresets.DEFAULT.style) }
    var themeId by remember { mutableStateOf(existing?.baseThemeId ?: ThemePresets.DEFAULT.id) }
    var creating by remember { mutableStateOf(false) }
    var savingTheme by remember { mutableStateOf(false) }

    val themes: List<Theme> = ThemePresets.ALL + data.customThemes
    val dday: DDay? = data.ddays.firstOrNull { it.id == ddayId }

    BackHandler(enabled = step == 1) { step = 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (step == 0) "디데이 선택" else "위젯 디자인") },
                navigationIcon = {
                    if (step == 0) IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, "닫기") }
                    else IconButton(onClick = { step = 0 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로") }
                },
                actions = {
                    if (step == 0) {
                        TextButton(enabled = dday != null, onClick = { step = 1 }) { Text("다음") }
                    } else {
                        TextButton(onClick = { savingTheme = true }) { Text("내 테마로 저장") }
                        Button(
                            enabled = dday != null,
                            onClick = {
                                Repository.saveWidget(context, WidgetConfig(widgetId, ddayId!!, style, themeId))
                                onSaved()
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        ) { Text("완료") }
                    }
                },
            )
        },
        floatingActionButton = {
            if (step == 0) {
                ExtendedFloatingActionButton(
                    onClick = { creating = true },
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("새 디데이") },
                )
            }
        },
    ) { pad ->
        if (step == 0) {
            if (data.ddays.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(pad).padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "아직 만든 디데이가 없어요.\n오른쪽 아래 '새 디데이'로 만들어 주세요.",
                        textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                    items(data.ddays, key = { it.id }) { d ->
                        DDayRow(d, selected = d.id == ddayId, onClick = { ddayId = d.id })
                    }
                }
            }
        } else if (dday != null) {
            val content = remember(dday, style.countFormat) { WidgetContent.of(dday, style) }
            StyleEditor(
                style = style,
                onStyleChange = { style = it },
                content = content,
                themes = themes,
                selectedThemeId = themeId,
                onThemeSelected = { style = it.style; themeId = it.id },
                actualSize = actualSize,
                modifier = Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()),
            )
        }
    }

    if (creating) {
        DDayEditorDialog(
            initial = null,
            onDismiss = { creating = false },
            onSave = {
                Repository.upsertDDay(context, it)
                ddayId = it.id
                creating = false
            },
        )
    }

    if (savingTheme) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { savingTheme = false },
            title = { Text("내 테마로 저장") },
            text = {
                Column {
                    Text("지금 디자인을 테마로 저장하면 다른 위젯에서도 고를 수 있어요.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(name, { name = it }, label = { Text("테마 이름") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    val t = Theme(id = "custom_" + UUID.randomUUID(), name = name.trim(), description = "내 테마", style = style)
                    Repository.saveCustomTheme(context, t)
                    themeId = t.id
                    savingTheme = false
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { savingTheme = false }) { Text("취소") } },
        )
    }
}
