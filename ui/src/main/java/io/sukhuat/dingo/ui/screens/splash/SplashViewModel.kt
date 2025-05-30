package io.sukhuat.dingo.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    fun initializeApp(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Simulate initialization delay
            delay(2000)
            onComplete()
        }
    }
}
