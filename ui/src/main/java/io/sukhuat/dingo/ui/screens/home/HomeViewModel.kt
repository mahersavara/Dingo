package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.common.utils.ToastHelper
import io.sukhuat.dingo.data.repository.DummyRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import io.sukhuat.dingo.usecases.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DummyRepository,
    private val signOutUseCase: SignOutUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Initial)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun signOut(onSignOutSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            when (val result = signOutUseCase()) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Sign out successful")
                    _uiState.value = HomeUiState.Initial
                    onSignOutSuccess()
                }
                is AuthResult.Error -> {
                    val errorMsg = result.message
                    Log.e(TAG, "Sign out failed: $errorMsg")
                    ToastHelper.showMedium(context, errorMsg)
                    _uiState.value = HomeUiState.Error(errorMsg)
                }
                is AuthResult.Loading -> {
                    _uiState.value = HomeUiState.Loading
                }
            }
        }
    }
}
