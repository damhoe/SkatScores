package com.damhoe.skatscores.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppSettingsViewModel @Inject constructor(private val storageManager: SettingsStorageManager) : ViewModel() {

    val theme = storageManager.themeFlow.asLiveData(Dispatchers.IO)

    fun setTheme(theme: String) =
        viewModelScope.launch {
            storageManager.setTheme(theme)
        }

    val language = storageManager.languageFlow.asLiveData(Dispatchers.IO)

    fun setLanguage(language: String) =
        viewModelScope.launch {
            storageManager.setLanguage(language)
        }
}