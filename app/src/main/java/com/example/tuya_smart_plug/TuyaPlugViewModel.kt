package com.example.tuya_smart_plug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlugStatus {
    ON, OFF, UNKNOWN
}

data class PlugUiState(
    val status: PlugStatus = PlugStatus.UNKNOWN,
    val isLoading: Boolean = false,
    val timerRemainingSeconds: Int? = null,
    val timerTargetIsOn: Boolean? = null,
    val errorMessage: String? = null,
    val toastMessage: String? = null
)

class TuyaPlugViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlugUiState())
    val uiState: StateFlow<PlugUiState> = _uiState.asStateFlow()

    private val apiService = NetworkClient.apiService
    private var timerJob: Job? = null

    init {
        // App fetches current status from /status when it starts
        fetchStatus()
    }

    /**
     * Fetches current plug status from GET /status.
     * Expected response: {"status": "on"} or {"status": "off"}
     */
    fun fetchStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getStatus()
                val parsedStatus = when (response.status.trim().lowercase()) {
                    "on" -> PlugStatus.ON
                    "off" -> PlugStatus.OFF
                    else -> PlugStatus.UNKNOWN
                }
                _uiState.update {
                    it.copy(
                        status = parsedStatus,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                val err = "Status fetch error: ${e.localizedMessage ?: "Connection failed"}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err,
                        toastMessage = "Network Error: Could not reach 192.168.66.6:5000"
                    )
                }
            }
        }
    }

    /**
     * Sends GET request to /on.
     * Automatically refreshes status after completion.
     */
    fun turnOn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                apiService.turnOn()
                _uiState.update {
                    it.copy(
                        status = PlugStatus.ON,
                        isLoading = false,
                        errorMessage = null,
                        toastMessage = "Plug turned ON successfully"
                    )
                }
                // Refresh status from server to ensure sync
                fetchStatus()
            } catch (e: Exception) {
                val err = "Turn ON error: ${e.localizedMessage ?: "Connection failed"}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err,
                        toastMessage = "Failed to turn plug ON (Network error)"
                    )
                }
            }
        }
    }

    /**
     * Sends GET request to /off.
     * Automatically refreshes status after completion.
     */
    fun turnOff() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                apiService.turnOff()
                _uiState.update {
                    it.copy(
                        status = PlugStatus.OFF,
                        isLoading = false,
                        errorMessage = null,
                        toastMessage = "Plug turned OFF successfully"
                    )
                }
                // Refresh status from server to ensure sync
                fetchStatus()
            } catch (e: Exception) {
                val err = "Turn OFF error: ${e.localizedMessage ?: "Connection failed"}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err,
                        toastMessage = "Failed to turn plug OFF (Network error)"
                    )
                }
            }
        }
    }

    /**
     * Starts a 15-minute countdown timer (900 seconds) using Kotlin Coroutines.
     * When it reaches zero, sends GET request to /on (if turnOn=true) or /off (if turnOn=false).
     */
    fun startTimer(turnOn: Boolean) {
        // Cancel any active countdown first
        timerJob?.cancel()

        val totalSeconds = 15 * 60 // 15 minutes = 900 seconds
        _uiState.update {
            it.copy(
                timerRemainingSeconds = totalSeconds,
                timerTargetIsOn = turnOn,
                toastMessage = "Timer started for 15 minutes to ${if (turnOn) "Turn ON" else "Turn OFF"}"
            )
        }

        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L) // 1 second coroutine delay
                remaining--
                _uiState.update { it.copy(timerRemainingSeconds = remaining) }
            }

            // Timer expired: trigger action and clear timer state
            _uiState.update {
                it.copy(
                    timerRemainingSeconds = null,
                    timerTargetIsOn = null,
                    toastMessage = "Timer expired! Executing ${if (turnOn) "Turn ON" else "Turn OFF"}..."
                )
            }

            if (turnOn) {
                turnOn()
            } else {
                turnOff()
            }
        }
    }

    /**
     * Cancels the active timer countdown and clears displayed time.
     */
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                timerRemainingSeconds = null,
                timerTargetIsOn = null,
                toastMessage = "Timer cancelled"
            )
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}