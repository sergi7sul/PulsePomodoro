package com.cherrytime.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherrytime.R
import com.cherrytime.data.datastore.UserPreferences
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val geminiState by viewModel.geminiKeyState.collectAsStateWithLifecycle()
    SettingsContent(
        prefs = prefs,
        onWorkDuration = viewModel::setWorkDuration,
        onShortBreak = viewModel::setShortBreak,
        onLongBreak = viewModel::setLongBreak,
        onLongBreakInterval = viewModel::setLongBreakInterval,
        onSoundEnabled = viewModel::setSoundEnabled,
        onVibrationEnabled = viewModel::setVibrationEnabled,
        onWaterEnabled = viewModel::setWaterReminderEnabled,
        onWaterInterval = viewModel::setWaterReminderInterval,
        onPostureEnabled = viewModel::setPostureReminderEnabled,
        onPostureInterval = viewModel::setPostureReminderInterval,
        geminiState = geminiState,
        onSaveGeminiKey = viewModel::saveGeminiKey,
        onClearGeminiKey = viewModel::clearGeminiKey,
    )
}

@Composable
private fun SettingsContent(
    prefs: UserPreferences,
    onWorkDuration: (Int) -> Unit,
    onShortBreak: (Int) -> Unit,
    onLongBreak: (Int) -> Unit,
    onLongBreakInterval: (Int) -> Unit,
    onSoundEnabled: (Boolean) -> Unit,
    onVibrationEnabled: (Boolean) -> Unit,
    onWaterEnabled: (Boolean) -> Unit = {},
    onWaterInterval: (Int) -> Unit = {},
    onPostureEnabled: (Boolean) -> Unit = {},
    onPostureInterval: (Int) -> Unit = {},
    geminiState: GeminiKeyState = GeminiKeyState(),
    onSaveGeminiKey: (String) -> Unit = {},
    onClearGeminiKey: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        SectionTitle(stringResource(R.string.settings_title))

        Spacer(Modifier.height(8.dp))

        DurationSlider(
            label = stringResource(R.string.settings_work_duration),
            value = prefs.workDurationMin,
            range = 1f..60f,
            unit = "min",
            onValueChange = onWorkDuration,
        )

        DurationSlider(
            label = stringResource(R.string.settings_short_break_duration),
            value = prefs.shortBreakMin,
            range = 1f..30f,
            unit = "min",
            onValueChange = onShortBreak,
        )

        DurationSlider(
            label = stringResource(R.string.settings_long_break_duration),
            value = prefs.longBreakMin,
            range = 1f..30f,
            unit = "min",
            onValueChange = onLongBreak,
        )

        DurationSlider(
            label = stringResource(R.string.settings_pomodoros_before_long),
            value = prefs.longBreakInterval,
            range = 2f..8f,
            unit = "",
            onValueChange = onLongBreakInterval,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        SectionTitle("Alerts")

        Spacer(Modifier.height(4.dp))

        ToggleRow(
            label = "Sound",
            checked = prefs.soundEnabled,
            onCheckedChange = onSoundEnabled,
        )

        ToggleRow(
            label = "Vibration",
            checked = prefs.vibrationEnabled,
            onCheckedChange = onVibrationEnabled,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        SectionTitle(stringResource(R.string.settings_reminders))

        Spacer(Modifier.height(4.dp))

        ToggleRow(
            label = "Water reminder",
            checked = prefs.waterReminderEnabled,
            onCheckedChange = onWaterEnabled,
        )
        if (prefs.waterReminderEnabled) {
            DurationSlider(
                label = "Water interval",
                value = prefs.waterReminderIntervalMin,
                range = 15f..120f,
                unit = "min",
                onValueChange = onWaterInterval,
            )
        }

        ToggleRow(
            label = "Posture reminder",
            checked = prefs.postureReminderEnabled,
            onCheckedChange = onPostureEnabled,
        )
        if (prefs.postureReminderEnabled) {
            DurationSlider(
                label = "Posture interval",
                value = prefs.postureReminderIntervalMin,
                range = 15f..120f,
                unit = "min",
                onValueChange = onPostureInterval,
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        SectionTitle(stringResource(R.string.settings_gemini_key))

        GeminiKeySection(
            state = geminiState,
            onSave = onSaveGeminiKey,
            onClear = onClearGeminiKey,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GeminiKeySection(
    state: GeminiKeyState,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
) {
    var keyInput by remember { mutableStateOf("") }

    if (state.hasKey) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "API key saved",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            OutlinedButton(onClick = onClear) { Text("Clear") }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_gemini_key_hint)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (keyInput.isNotBlank()) onSave(keyInput) },
                enabled = keyInput.isNotBlank() && !state.isValidating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isValidating) {
                    CircularProgressIndicator()
                } else {
                    Text("Save & Validate")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun DurationSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (unit.isNotEmpty()) "$value $unit" else "$value",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range,
            steps = (range.endInclusive - range.start - 1).toInt(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
