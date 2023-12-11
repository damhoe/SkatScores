package com.damhoe.skatscores.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import javax.inject.Inject

class ViewModelFactory @Inject constructor(private var storageManager: SettingsStorageManager)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == AppSettingsViewModel::class.java) {
            return AppSettingsViewModel(storageManager) as T
        }
        throw IllegalArgumentException("Expected AppSettingsViewModel class.")
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass == AppSettingsViewModel::class.java) {
            return AppSettingsViewModel(storageManager) as T
        }
        throw IllegalArgumentException("Expected AppSettingsViewModel class.")
    }
}