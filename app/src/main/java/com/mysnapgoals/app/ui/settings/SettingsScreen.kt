package com.mysnapgoals.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.R
import com.mysnapgoals.app.settings.AppTheme
import com.mysnapgoals.app.settings.PomodoroSettings
import com.mysnapgoals.app.settings.ProfileAvatar
import com.mysnapgoals.app.settings.SettingsRepository
import com.mysnapgoals.app.ui.components.Button3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val settings by settingsRepository.settingsFlow.collectAsState(initial = PomodoroSettings())
    val scope = rememberCoroutineScope()
    var showPomodoroSettings by remember { mutableStateOf(false) }
    var showProfileSettings by remember { mutableStateOf(false) }
    var showThemeSettings by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BackHandler(enabled = showPomodoroSettings || showProfileSettings || showThemeSettings || showPrivacyPolicy) {
            when {
                showPrivacyPolicy -> showPrivacyPolicy = false
                showProfileSettings -> showProfileSettings = false
                showPomodoroSettings -> showPomodoroSettings = false
                showThemeSettings -> showThemeSettings = false
            }
        }

        if (showPomodoroSettings) {
            PomodoroSettingsContent(
                settings = settings,
                onBack = { showPomodoroSettings = false },
                onClose = onClose,
                onUpdateAutoStartBreaks = { value ->
                    scope.launch { settingsRepository.setAutoStartBreaks(value) }
                },
                onUpdateAutoStartWork = { value ->
                    scope.launch { settingsRepository.setAutoStartWork(value) }
                },
                onUpdatePauseOnExit = { value ->
                    scope.launch { settingsRepository.setPauseOnExit(value) }
                },
                onUpdateAlarmEnabled = { value ->
                    scope.launch { settingsRepository.setAlarmEnabled(value) }
                },
                onUpdateAlarmVolume = { value ->
                    scope.launch { settingsRepository.setAlarmVolumePercent(value) }
                },
                onUpdateVibrationEnabled = { value ->
                    scope.launch { settingsRepository.setVibrationEnabled(value) }
                },
                onUpdateKeepNotification = { value ->
                    scope.launch { settingsRepository.setKeepNotification(value) }
                }
            )
        } else if (showProfileSettings) {
            ProfileSettingsContent(
                selectedAvatar = settings.profileAvatar,
                onBack = { showProfileSettings = false },
                onClose = onClose,
                onSelectAvatar = { avatar ->
                    scope.launch { settingsRepository.setProfileAvatar(avatar) }
                }
            )
        } else if (showThemeSettings) {
            ThemeSettingsContent(
                selectedTheme = settings.appTheme,
                onBack = { showThemeSettings = false },
                onClose = onClose,
                onSelectTheme = { theme ->
                    scope.launch { settingsRepository.setAppTheme(theme) }
                }
            )
        } else if (showPrivacyPolicy) {
            PrivacyPolicyContent(
                onBack = { showPrivacyPolicy = false }
            )
        } else {
            SettingsMenu(
                onShowPomodoro = { showPomodoroSettings = true },
                onShowProfile = { showProfileSettings = true },
                onShowTheme = { showThemeSettings = true },
                onShowPrivacyPolicy = { showPrivacyPolicy = true },
                onClose = onClose
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsMenu(
    onShowPomodoro: () -> Unit,
    onShowProfile: () -> Unit,
    onShowTheme: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Button3D(
                text = stringResource(R.string.settings_pomodoro),
                onClick = onShowPomodoro,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                depth = 4.dp
            )
        }
        item {
            Button3D(
                text = stringResource(R.string.settings_profile),
                onClick = onShowProfile,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                depth = 4.dp
            )
        }
        item {
            Button3D(
                text = stringResource(R.string.settings_theme),
                onClick = onShowTheme,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                depth = 4.dp
            )
        }
        item {
            Button3D(
                text = stringResource(R.string.settings_privacy_policy),
                onClick = onShowPrivacyPolicy,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                depth = 4.dp
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrivacyPolicyContent(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val policyText = remember(context) {
        context.resources.openRawResource(R.raw.privacy_policy).bufferedReader().use { it.readText() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_privacy_policy),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Text(
                text = policyText,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PomodoroSettingsContent(
    settings: PomodoroSettings,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onUpdateAutoStartBreaks: (Boolean) -> Unit,
    onUpdateAutoStartWork: (Boolean) -> Unit,
    onUpdatePauseOnExit: (Boolean) -> Unit,
    onUpdateAlarmEnabled: (Boolean) -> Unit,
    onUpdateAlarmVolume: (Int) -> Unit,
    onUpdateVibrationEnabled: (Boolean) -> Unit,
    onUpdateKeepNotification: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_pomodoro),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item { SectionTitle(stringResource(R.string.settings_behavior)) }
        item {
            SwitchRow(
                label = stringResource(R.string.settings_auto_start_breaks),
                checked = settings.autoStartBreaks,
                onCheckedChange = onUpdateAutoStartBreaks
            )
        }
        item {
            SwitchRow(
                label = stringResource(R.string.settings_auto_start_work),
                checked = settings.autoStartWork,
                onCheckedChange = onUpdateAutoStartWork
            )
        }
        item {
            SwitchRow(
                label = stringResource(R.string.settings_pause_on_exit),
                checked = settings.pauseOnExit,
                onCheckedChange = onUpdatePauseOnExit
            )
        }

        item { SectionTitle(stringResource(R.string.settings_sound_vibration)) }
        item {
            SwitchRow(
                label = stringResource(R.string.settings_alarm),
                checked = settings.alarmEnabled,
                onCheckedChange = onUpdateAlarmEnabled
            )
        }
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.settings_volume, settings.alarmVolumePercent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = settings.alarmVolumePercent.toFloat(),
                    onValueChange = { value -> onUpdateAlarmVolume(value.toInt()) },
                    valueRange = 0f..100f,
                    enabled = settings.alarmEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            SwitchRow(
                label = stringResource(R.string.settings_vibration),
                checked = settings.vibrationEnabled,
                onCheckedChange = onUpdateVibrationEnabled
            )
        }

        item { SectionTitle(stringResource(R.string.settings_notifications)) }
        item {
            SwitchRow(
                label = stringResource(R.string.settings_keep_notification),
                checked = settings.keepNotification,
                onCheckedChange = onUpdateKeepNotification
            )
        }
        item {
            Text(
                text = stringResource(R.string.settings_notification_hint),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileSettingsContent(
    selectedAvatar: ProfileAvatar,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSelectAvatar: (ProfileAvatar) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_profile),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarOption(
                    resId = R.drawable.maleavatar,
                    selected = selectedAvatar == ProfileAvatar.MALE,
                    onClick = { onSelectAvatar(ProfileAvatar.MALE) }
                )
                AvatarOption(
                    resId = R.drawable.femaleavatar,
                    selected = selectedAvatar == ProfileAvatar.FEMALE,
                    onClick = { onSelectAvatar(ProfileAvatar.FEMALE) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeSettingsContent(
    selectedTheme: AppTheme,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSelectTheme: (AppTheme) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_light),
                selected = selectedTheme == AppTheme.LIGHT,
                onClick = { onSelectTheme(AppTheme.LIGHT) }
            )
        }
        item {
            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_dark),
                selected = selectedTheme == AppTheme.DARK,
                onClick = { onSelectTheme(AppTheme.DARK) }
            )
        }
        item {
            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_pink),
                selected = selectedTheme == AppTheme.PINK,
                onClick = { onSelectTheme(AppTheme.PINK) }
            )
        }
        item {
            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_coffee),
                selected = selectedTheme == AppTheme.COFFEE,
                onClick = { onSelectTheme(AppTheme.COFFEE) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AvatarOption(
    resId: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(shape)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(108.dp)
                .clip(shape)
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.content_desc_selected),
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.content_desc_selected),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SnapGoalsTheme {
        SettingsScreen(onClose = {})
    }
}
