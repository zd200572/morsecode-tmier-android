package com.morsecode.android.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsecode.android.ui.theme.*

@Composable
fun MainScreen(viewModel: MorseViewModel) {
    val state by viewModel.uiState.collectAsState()

    // 呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // === 顶部：时间显示 ===
            TimeDisplay(state, glowAlpha)

            Spacer(modifier = Modifier.height(16.dp))

            // === 中部：Morse 码文本 ===
            MorseTextDisplay(state)

            Spacer(modifier = Modifier.height(16.dp))

            // === Morse 可视化 ===
            MorseVisualization(state)

            Spacer(modifier = Modifier.height(24.dp))

            // === 输出开关 ===
            OutputToggles(state, viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // === 定时播报 ===
            ScheduleSection(state, viewModel)

            Spacer(modifier = Modifier.height(12.dp))

            // === 播报设置 ===
            SettingsSection(state, viewModel)

            Spacer(modifier = Modifier.weight(1f))

            // === 播报按钮 ===
            PlayButton(state, viewModel)
        }
    }
}

@Composable
private fun TimeDisplay(state: MorseUiState, glowAlpha: Float) {
    Box(contentAlignment = Alignment.Center) {
        // 光晕背景
        Box(
            modifier = Modifier
                .size(200.dp, 100.dp)
                .alpha(glowAlpha * 0.4f)
                .blur(40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Amber500.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        // 时间文字
        Text(
            text = String.format("%02d : %02d", state.hour, state.minute),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Thin,
                letterSpacing = 4.sp,
                fontSize = 64.sp
            ),
            color = Amber500
        )
    }
    // 秒数指示
    Text(
        text = String.format("%02d", state.second),
        style = MaterialTheme.typography.bodyMedium,
        color = TextDim,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun MorseTextDisplay(state: MorseUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("H  ", style = MaterialTheme.typography.labelMedium, color = TextDim)
            Text(
                text = state.hourMorse.replace('.', '·').replace('-', '−'),
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Light
                ),
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("M  ", style = MaterialTheme.typography.labelMedium, color = TextDim)
            Text(
                text = state.minuteMorse.replace('.', '·').replace('-', '−'),
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Light
                ),
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MorseVisualization(state: MorseUiState) {
    val symbols = state.displaySymbols
    if (symbols.isEmpty()) return

    // 按 group 分组显示（0,1 = 小时两位; 2,3 = 分钟两位）
    val hourSymbols = symbols.filter { it.groupIndex < 2 }
    val minuteSymbols = symbols.filter { it.groupIndex >= 2 }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MorseSymbolRow(hourSymbols, state.currentPlayingIndex, state.isPlaying)
        // 分隔线
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(TextDim, RoundedCornerShape(1.dp))
        )
        MorseSymbolRow(minuteSymbols, state.currentPlayingIndex, state.isPlaying)
    }
}

@Composable
private fun MorseSymbolRow(
    symbols: List<com.morsecode.android.morse.MorseCodeEngine.MorseSymbol>,
    currentIndex: Int,
    isPlaying: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        var prevGroup = -1
        symbols.forEach { symbol ->
            // 组间加间隔
            if (prevGroup != -1 && symbol.groupIndex != prevGroup) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            prevGroup = symbol.groupIndex

            val isActive = isPlaying && symbol.symbolIndex == currentIndex
            val color by animateColorAsState(
                targetValue = if (isActive) Amber500
                else if (isPlaying && symbol.symbolIndex < currentIndex) Amber500.copy(alpha = 0.4f)
                else MorseDotInactive,
                animationSpec = tween(150),
                label = "symbolColor"
            )

            if (symbol.isDash) {
                // 长横
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .width(24.dp)
                        .height(8.dp)
                        .background(color, RoundedCornerShape(4.dp))
                )
            } else {
                // 圆点
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun OutputToggles(state: MorseUiState, viewModel: MorseViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        ToggleChip(
            icon = Icons.Rounded.Vibration,
            label = "振动",
            checked = state.enableVibration,
            onToggle = { viewModel.setVibration(it) }
        )
        ToggleChip(
            icon = Icons.Rounded.VolumeUp,
            label = "声音",
            checked = state.enableSound,
            onToggle = { viewModel.setSound(it) }
        )
        ToggleChip(
            icon = Icons.Rounded.FlashlightOn,
            label = "闪光",
            checked = state.enableFlashlight,
            onToggle = { viewModel.setFlashlight(it) }
        )
    }
}

@Composable
private fun ToggleChip(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) Amber500.copy(alpha = 0.15f) else DarkSurfaceVariant,
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) Amber500.copy(alpha = 0.5f) else Color.Transparent,
        label = "chipBorder"
    )
    val contentColor by animateColorAsState(
        targetValue = if (checked) Amber500 else TextDim,
        label = "chipContent"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle(!checked) }
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
private fun ScheduleSection(state: MorseUiState, viewModel: MorseViewModel) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "定时播报",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Switch(
                    checked = state.isScheduled,
                    onCheckedChange = { viewModel.toggleSchedule() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = Amber500,
                        uncheckedThumbColor = TextDim,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 间隔选择
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(15, 30, 60).forEach { interval ->
                    IntervalChip(
                        interval = interval,
                        isSelected = state.scheduleInterval == interval,
                        onClick = { viewModel.setScheduleInterval(interval) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun IntervalChip(
    interval: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Amber500 else DarkSurfaceVariant,
        label = "intervalBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) DarkBackground else TextSecondary,
        label = "intervalText"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "${interval} 分钟",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsSection(state: MorseUiState, viewModel: MorseViewModel) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "播报设置",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 日期播报开关
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = "日期",
                        tint = if (state.includeDate) Amber500 else TextDim,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "播报日期",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "开启后将先播报日期再播报时间",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
                Switch(
                    checked = state.includeDate,
                    onCheckedChange = { viewModel.setIncludeDate(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = Amber500,
                        uncheckedThumbColor = TextDim,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun PlayButton(state: MorseUiState, viewModel: MorseViewModel) {
    val isPlaying = state.isPlaying

    // 播放时按钮脉冲动画
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor = if (isPlaying) Color(0xFFEF5350) else Amber500
    val buttonText = if (isPlaying) "■  停 止" else "▶  播 报"
    val scale = if (isPlaying) pulseScale else 1f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        // 按钮光晕
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.3f)
                    .blur(20.dp)
                    .background(
                        Color(0xFFEF5350),
                        RoundedCornerShape(28.dp)
                    )
            )
        }

        Button(
            onClick = { viewModel.togglePlay() },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = if (isPlaying) 12.dp else 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = buttonColor.copy(alpha = 0.3f),
                    spotColor = buttonColor.copy(alpha = 0.3f)
                )
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                ),
                color = if (isPlaying) Color.White else DarkBackground
            )
        }
    }
}
