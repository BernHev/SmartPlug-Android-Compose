package com.example.tuya_smart_plug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val viewModel: TuyaPlugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1976D2),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE3F2FD),
                    onPrimaryContainer = Color(0xFF0D47A1),
                    background = Color(0xFFF8FAFC),
                    surface = Color.White
                )
            ) {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsState()

                // Show Android Toast messages for feedback or network errors
                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartPlugScreen(
                        uiState = uiState,
                        onTurnOn = { viewModel.turnOn() },
                        onTurnOff = { viewModel.turnOff() },
                        onStartTimer = { turnOn -> viewModel.startTimer(turnOn) },
                        onCancelTimer = { viewModel.cancelTimer() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlugScreen(
    uiState: PlugUiState,
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit,
    onStartTimer: (Boolean) -> Unit,
    onCancelTimer: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Smart Plug",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Large status indicator below the title: shows "ON" in green or "OFF" in red
            StatusIndicator(
                status = uiState.status,
                isLoading = uiState.isLoading
            )

            // Two large buttons side by side: "TURN ON" and "TURN OFF"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onTurnOn,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32), // Green
                        contentColor = Color.White
                    ),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "TURN ON",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onTurnOff,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828), // Red
                        contentColor = Color.White
                    ),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "TURN OFF",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Timer section below the buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Timer Controls",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )

                    // Button "Turn OFF in 15 minutes"
                    Button(
                        onClick = { onStartTimer(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF475569)
                        )
                    ) {
                        Text(
                            text = "Turn OFF in 15 minutes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Button "Turn ON in 15 minutes"
                    Button(
                        onClick = { onStartTimer(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        )
                    ) {
                        Text(
                            text = "Turn ON in 15 minutes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Text showing remaining time when a timer is active (e.g., "Timer: 14:32")
                    if (uiState.timerRemainingSeconds != null) {
                        val minutes = uiState.timerRemainingSeconds / 60
                        val seconds = uiState.timerRemainingSeconds % 60
                        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                        val targetAction = if (uiState.timerTargetIsOn == true) "Turn ON" else "Turn OFF"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFEEF2FF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Timer: $timeFormatted",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Action queued: $targetAction",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Button "Cancel Timer"
                        OutlinedButton(
                            onClick = onCancelTimer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFDC2626)
                            )
                        ) {
                            Text(
                                text = "Cancel Timer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Small error message if request fails
            uiState.errorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(status: PlugStatus, isLoading: Boolean) {
    val (statusText, statusColor, bgTint) = when (status) {
        PlugStatus.ON -> Triple("ON", Color(0xFF2E7D32), Color(0xFFE8F5E9))
        PlugStatus.OFF -> Triple("OFF", Color(0xFFC62828), Color(0xFFFFEBEE))
        PlugStatus.UNKNOWN -> Triple("OFFLINE", Color(0xFF64748B), Color(0xFFF1F5F9))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                color = bgTint,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STATUS",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = statusColor
            )
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = statusColor,
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}