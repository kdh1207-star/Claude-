@file:OptIn(ExperimentalMaterial3Api::class)

package com.ddayforge.widget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ddayforge.widget.data.CountMode
import com.ddayforge.widget.data.DDay
import com.ddayforge.widget.data.DDayCalc
import com.ddayforge.widget.style.CountFormat
import java.time.LocalDate

private const val DAY_MS = 86_400_000L

@Composable
fun DDayEditorDialog(initial: DDay?, onDismiss: () -> Unit, onSave: (DDay) -> Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var epochDay by remember { mutableLongStateOf(initial?.epochDay ?: LocalDate.now().plusDays(100).toEpochDay()) }
    var mode by remember { mutableStateOf(initial?.mode ?: CountMode.COUNTDOWN) }
    var picking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "새 디데이" else "디데이 수정") },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val base = initial ?: DDay(title = title.trim(), epochDay = epochDay, mode = mode)
                    onSave(base.copy(title = title.trim(), epochDay = epochDay, mode = mode))
                },
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("이름 (예: 수능, 여행, 100일)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    CountMode.entries.forEachIndexed { i, m ->
                        SegmentedButton(
                            selected = mode == m,
                            onClick = { mode = m },
                            shape = SegmentedButtonDefaults.itemShape(i, CountMode.entries.size),
                        ) { Text(m.label) }
                    }
                }
                Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text((if (mode == CountMode.COUNTDOWN) "목표일: " else "시작일: ") + DDayCalc.formatDate(LocalDate.ofEpochDay(epochDay)))
                }
                val preview = DDay(title = title, epochDay = epochDay, mode = mode)
                Text(
                    "미리 보기: " + CountFormat.DDAY.format(DDayCalc.compute(preview)) + "  ·  " + CountFormat.KOREAN.format(DDayCalc.compute(preview)),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )

    if (picking) {
        val state = rememberDatePickerState(initialSelectedDateMillis = epochDay * DAY_MS)
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { epochDay = Math.floorDiv(it, DAY_MS) }
                    picking = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("취소") } },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
fun DDayRow(
    dday: DDay,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val v = DDayCalc.compute(dday)
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(dday.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${dday.mode.label} · ${DDayCalc.formatDate(dday.date)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                CountFormat.DDAY.format(v), fontSize = 24.sp, fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
            if (onEdit != null) IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "수정") }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "삭제") }
        }
    }
}
