package com.example.smartfit.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.PrefsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ThemeViewModel(prefs: PrefsRepository) : ViewModel() {
    // "SYSTEM" | "LIGHT" | "DARK"
    val themeMode = prefs.themeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")
}
