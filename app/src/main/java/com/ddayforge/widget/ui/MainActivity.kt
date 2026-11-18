@file:OptIn(ExperimentalMaterial3Api::class)

package com.ddayforge.widget.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ddayforge.widget.data.DDay
import com.ddayforge.widget.data.Repository
import com.ddayforge.widget.render.WidgetContent
import com.ddayforge.widget.style.Theme
import com.ddayforge.widget.style.ThemePresets
import com.ddayforge.widget.widget.DDayWidgetProvider
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.init(this)
        setContent { AppTheme { MainScreen() } }
    }
}

@Composable
private fun MainScreen() {
    val context = LocalContext.current
    val data by Repository.state.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<DDay?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<DDay?>(null) }

    val sample = data.ddays.firstOrNull() ?: DDay(title = "수능", epochDay = LocalDate.now().plusDays(365).toEpochDay())

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("디데이 포지", fontWeight = FontWeight.Black) })
                TabRow(selectedTabIndex = tab) {
                    listOf("디데이", "테마", "위젯").forEachIndexed { i, t ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                    }
                }
            }
        },
        floatingActionButton = {
            if (tab == 0) FloatingActionButton(onClick = { creating = true }) { Icon(Icons.Filled.Add, "디데이 추가") }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (tab) {
                0 -> DDayListTab(data.ddays, onEdit = { editing = it }, onDelete = { deleting = it })
                1 -> ThemeGalleryTab(ThemePresets.ALL, data.customThemes, sample)
                2 -> WidgetsTab(data.widgets.values.toList(), data.ddays)
            }
        }
    }

    if (creating || editing != null) {
        DDayEditorDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = {
                Repository.upsertDDay(context, it)
                DDayWidgetProvider.updateAll(context)
                creating = false; editing = null
            },
        )
    }
    deleting?.let { d ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("'${d.title}' 삭제") },
            text = { Text("이 디데이를 쓰는 위젯은 '디데이가 삭제됨'으로 표시돼요. 위젯을 탭해서 다른 디데이로 바꿀 수 있어요.") },
            confirmButton = {
                TextButton(onClick = {
                    Repository.deleteDDay(context, d.id)
                    DDayWidgetProvider.updateAll(context)
                    deleting = null
                }) { Text("삭제") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("취소") } },
        )
    }
}

@Composable
private fun DDayListTab(ddays: List<DDay>, onEdit: (DDay) -> Unit, onDelete: (DDay) -> Unit) {
    if (ddays.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "+ 버튼으로 첫 디데이를 만들어 보세요.\n\n만든 뒤 홈 화면을 길게 눌러 '위젯' → '디데이 포지'를 추가하면\n어떤 디데이를 어떤 테마로 띄울지 고를 수 있어요.",
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(ddays.sortedBy { it.epochDay }, key = { it.id }) { d ->
            DDayRow(d, onEdit = { onEdit(d) }, onDelete = { onDelete(d) })
        }
    }
}

@Composable
private fun ThemeGalleryTab(presets: List<Theme>, custom: List<Theme>, sample: DDay) {
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf<Theme?>(null) }
    if (presets.isEmpty() && custom.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "테마가 없어요.\n위젯 편집 화면에서 꾸민 뒤 '내 테마로 저장'하면 여기에 모여요.",
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(custom + presets, key = { it.id }) { t ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(8.dp)) {
                    val content = remember(sample, t.style.countFormat) { WidgetContent.of(sample, t.style) }
                    WidgetPreview(t.style, content, 180f, 80f, displayWidth = 160.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(top = 6.dp)) {
                            Text(t.name, style = MaterialTheme.typography.titleSmall)
                            Text(t.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                        if (!t.builtIn) {
                            IconButton(onClick = { confirmDelete = t }) { Icon(Icons.Filled.Delete, "삭제") }
                        }
                    }
                }
            }
        }
    }
    confirmDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("'${t.name}' 테마 삭제") },
            text = { Text("이미 이 테마로 만든 위젯은 그대로 유지돼요.") },
            confirmButton = { TextButton(onClick = { Repository.deleteCustomTheme(context, t.id); confirmDelete = null }) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("취소") } },
        )
    }
}

@Composable
private fun WidgetsTab(widgets: List<com.ddayforge.widget.data.WidgetConfig>, ddays: List<DDay>) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("위젯 추가하는 법", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "1. 홈 화면 빈 곳을 길게 누르기\n2. '위젯' → '디데이 포지' 선택\n3. 열리는 화면에서 디데이와 테마 고르기\n\n이미 만든 위젯은 탭하면 다시 꾸밀 수 있어요.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        val m = AppWidgetManager.getInstance(context)
                        val ok = m.isRequestPinAppWidgetSupported &&
                            m.requestPinAppWidget(ComponentName(context, DDayWidgetProvider::class.java), null, null)
                        if (!ok) Toast.makeText(context, "이 런처는 바로 추가를 지원하지 않아요. 홈 화면에서 직접 추가해 주세요.", Toast.LENGTH_LONG).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("홈 화면에 위젯 바로 추가") }
                }
            }
        }
        if (widgets.isNotEmpty()) {
            item { Text("설치된 위젯", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        }
        items(widgets, key = { it.appWidgetId }) { w ->
            val d = ddays.firstOrNull { it.id == w.ddayId }
            val content = remember(d, w.style) { if (d != null) WidgetContent.of(d, w.style) else WidgetContent.placeholder("디데이가 삭제됨") }
            Card(
                onClick = {
                    context.startActivity(
                        Intent(context, WidgetConfigActivity::class.java)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, w.appWidgetId)
                            .putExtra(WidgetConfigActivity.EXTRA_RECONFIGURE, true),
                    )
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (ww, hh) = DDayWidgetProvider.widgetSizeDp(AppWidgetManager.getInstance(context), w.appWidgetId)
                    WidgetPreview(w.style, content, ww, hh, displayWidth = 150.dp)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(d?.title ?: "(삭제됨)", style = MaterialTheme.typography.titleSmall)
                        Text("탭해서 다시 꾸미기", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
