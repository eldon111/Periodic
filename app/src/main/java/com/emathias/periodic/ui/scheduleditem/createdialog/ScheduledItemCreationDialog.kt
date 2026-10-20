package com.emathias.periodic.ui.scheduleditem.createdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.MonthlyMode
import com.emathias.periodic.model.RepeatUnit
import com.emathias.periodic.model.Repetition
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideEditDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.CreateItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideAddDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.UpdateItem
import com.emathias.periodic.util.RecurrenceFormatter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduledItemCreationDialog(
    onEvent: (ScheduledItemEvent) -> Unit,
    scheduledItem: ScheduledItem? = null,
) {
    val isEditing = scheduledItem != null
    val itemZone = scheduledItem?.schedule?.start?.zone ?: ZoneId.systemDefault()
    val initialStartDate = scheduledItem?.schedule?.start?.toLocalDate() ?: LocalDate.now()
    val initialEndDate = scheduledItem?.expiration
        ?.atZone(itemZone)
        ?.toLocalDate()
    var title by remember(scheduledItem?.id) {
        mutableStateOf(scheduledItem?.title.orEmpty())
    }
    var description by remember(scheduledItem?.id) {
        mutableStateOf(scheduledItem?.description.orEmpty())
    }
    var startDate by remember(scheduledItem?.id) {
        mutableStateOf(initialStartDate)
    }
    var unit by remember { mutableStateOf(RepeatUnit.WEEK) }
    var intervalText by remember { mutableStateOf("1") }
    val interval = intervalText.toIntOrNull() ?: 1
    val intervalValid = intervalText.toIntOrNull()?.let { it in 1..99 } == true
    var weekdays by remember { mutableStateOf(emptySet<DayOfWeek>()) }
    var monthlyMode by remember { mutableStateOf(MonthlyMode.DAY_OF_MONTH) }
    var endDate by remember(scheduledItem?.id) {
        mutableStateOf(initialEndDate)
    }

    val endDateChanged = endDate != initialEndDate
    val endValid = endDate?.isBefore(startDate) != true &&
        (!endDateChanged || endDate == null || endDate.isAfter(LocalDate.now()))
    val canSubmit = title.isNotBlank() && (isEditing || intervalValid) && endValid
    val dismissEvent = if (isEditing) HideEditDialog else HideAddDialog

    Dialog(onDismissRequest = { onEvent(dismissEvent) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(if (isEditing) "Edit scheduled item" else "New scheduled item")
                TextField(
                    value = title,
                    singleLine = true,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                DateField(
                    "Start Date",
                    startDate,
                    enabled = !isEditing,
                    onPick = { startDate = it },
                )

                if (scheduledItem != null) {
                    Text("Repetition")
                    Text(
                        text = RecurrenceFormatter.describe(scheduledItem.schedule),
                        style = LocalTextStyle.current,
                    )
                    if (scheduledItem.repeats) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = endDate != null,
                                    onValueChange = {
                                        endDate = if (it) startDate.plusMonths(1) else null
                                    },
                                    role = Role.Checkbox,
                                ),
                        ) {
                            Checkbox(checked = endDate != null, onCheckedChange = null)
                            Text("Ends", modifier = Modifier.padding(start = 8.dp))
                        }
                        endDate?.let {
                            DateField(
                                "End date",
                                it,
                                isError = !endValid,
                                errorText = when {
                                    it.isBefore(startDate) -> " (before start)"
                                    endDateChanged && !it.isAfter(LocalDate.now()) ->
                                        " (must be in the future)"
                                    else -> null
                                },
                                onPick = { d -> endDate = d },
                            )
                        }
                    }
                } else {
                    run {
                        var unitMenuOpen by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Every")
                            OutlinedTextField(
                                value = intervalText,
                                onValueChange = { intervalText = it.filter(Char::isDigit).take(2) },
                                singleLine = true,
                                isError = !intervalValid,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(64.dp),
                            )
                            ExposedDropdownMenuBox(
                                expanded = unitMenuOpen,
                                onExpandedChange = { unitMenuOpen = it },
                                modifier = Modifier.weight(1f),
                            ) {
                                OutlinedTextField(
                                    value = unit.label + "s",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = unitMenuOpen
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .widthIn(max = 160.dp),
                                )
                                ExposedDropdownMenu(
                                    expanded = unitMenuOpen,
                                    onDismissRequest = { unitMenuOpen = false },
                                ) {
                                    RepeatUnit.values().forEach {
                                        DropdownMenuItem(
                                            text = { Text(it.label + "s") },
                                            onClick = {
                                                unit = it
                                                unitMenuOpen = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        when (unit) {
                            RepeatUnit.WEEK -> {
                                Text("On")
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    DayOfWeek.values().forEach { day ->
                                        FilterChip(
                                            selected = day in weekdays,
                                            onClick = {
                                                weekdays = if (day in weekdays) weekdays - day
                                                else weekdays + day
                                            },
                                            label = {
                                                Text(
                                                    day.getDisplayName(
                                                        TextStyle.NARROW,
                                                        Locale.US
                                                    )
                                                )
                                            },
                                        )
                                    }
                                }
                                if (weekdays.isEmpty()) {
                                    Text(
                                        "Repeats on ${
                                            startDate.dayOfWeek.getDisplayName(
                                                TextStyle.FULL,
                                                Locale.US
                                            )
                                        } unless days are selected."
                                    )
                                }
                            }

                            RepeatUnit.MONTH -> Column {
                                MonthlyOption(
                                    "On day ${startDate.dayOfMonth}",
                                    monthlyMode == MonthlyMode.DAY_OF_MONTH
                                ) {
                                    monthlyMode = MonthlyMode.DAY_OF_MONTH
                                }
                                MonthlyOption(
                                    "On the ${Repetition.nthWeekdayLabel(startDate)}",
                                    monthlyMode == MonthlyMode.NTH_WEEKDAY
                                ) {
                                    monthlyMode = MonthlyMode.NTH_WEEKDAY
                                }
                                MonthlyOption(
                                    "On the last day of the month",
                                    monthlyMode == MonthlyMode.LAST_DAY
                                ) {
                                    monthlyMode = MonthlyMode.LAST_DAY
                                }
                            }

                            else -> Unit
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = endDate != null,
                                    onValueChange = {
                                        endDate = if (it) startDate.plusMonths(1) else null
                                    },
                                    role = Role.Checkbox,
                                ),
                        ) {
                            Checkbox(checked = endDate != null, onCheckedChange = null)
                            Text("Ends", modifier = Modifier.padding(start = 8.dp))
                        }
                        endDate?.let {
                            DateField(
                                "End date",
                                it,
                                isError = !endValid,
                                errorText = when {
                                    it.isBefore(startDate) -> " (before start)"
                                    endDateChanged && !it.isAfter(LocalDate.now()) ->
                                        " (must be in the future)"
                                    else -> null
                                },
                                onPick = { d -> endDate = d },
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (scheduledItem != null) {
                            onEvent(UpdateItem(scheduledItem, title, description, endDate))
                        } else {
                            val repetition = Repetition(
                                unit = unit,
                                interval = interval,
                                weekdays = weekdays,
                                monthlyMode = monthlyMode,
                                endDate = endDate,
                            )
                            onEvent(CreateItem(title, description, startDate, repetition))
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(if (isEditing) "Save" else "Create")
                }
            }
        }
    }
}

@Composable
private fun MonthlyOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    onPick: (LocalDate) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    Column {
        Text(label)
        OutlinedButton(
            enabled = enabled,
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(date.format(DATE_FORMAT) + (errorText ?: if (isError) " (before start)" else ""))
        }
    }
    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduledItemCreationDialogPreview() {
    ScheduledItemCreationDialog { }
}
