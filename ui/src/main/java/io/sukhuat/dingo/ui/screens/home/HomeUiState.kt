package io.sukhuat.dingo.ui.screens.home

sealed class HomeUiState {
    object Initial : HomeUiState()
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
