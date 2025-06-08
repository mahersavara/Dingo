package io.sukhuat.dingo.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.domain.usecase.auth.GetAuthStatusUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getAuthStatusUseCase: GetAuthStatusUseCase
) : ViewModel() {

    /**
     * Checks if the user is currently authenticated
     * @return true if user is authenticated, false otherwise
     */
    suspend fun checkUserAuthStatus(): Boolean {
        return getAuthStatusUseCase()
    }

    fun initializeApp(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Simulate initialization delay
            delay(2000)
            onComplete()
        }
    }
}
