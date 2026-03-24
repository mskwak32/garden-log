package com.mskwak.gardendailylog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskwak.domain.usecase.config.GetMinimumAppVersionUseCase
import com.mskwak.gardendailylog.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMinimumAppVersionUseCase: GetMinimumAppVersionUseCase
) : ViewModel() {

    private val _isForceUpdateRequired = MutableStateFlow(false)
    val isForceUpdateRequired: StateFlow<Boolean> = _isForceUpdateRequired.asStateFlow()

    init {
        checkAppVersion()
    }

    private fun checkAppVersion() {
        viewModelScope.launch {
            val minVersion = getMinimumAppVersionUseCase()
            _isForceUpdateRequired.value = BuildConfig.VERSION_CODE < minVersion
        }
    }
}
