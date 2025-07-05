package com.joshua.inkscape.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationManager {
    private var hasUnsavedChangesCallback: () -> Boolean = { false }
    private var navController: NavController? = null

    private val _showUnsavedChangesDialog = MutableStateFlow(false)
    val showUnsavedChangesDialog = _showUnsavedChangesDialog.asStateFlow()

    private var pendingNavigation: (() -> Unit)? = null

    fun setNavController(controller: NavController) {
        navController = controller
    }

    fun setHasUnsavedChangesCallback(callback: () -> Boolean) {
        hasUnsavedChangesCallback = callback
    }

    fun navigate(navigationAction: () -> Unit) {
        if (hasUnsavedChangesCallback()) {
            pendingNavigation = navigationAction
            _showUnsavedChangesDialog.value = true
        } else {
            navigationAction()
        }
    }

    fun handleBackPress() {
        navigate {
            navController?.popBackStack()
        }
    }

    fun onConfirmDiscardChanges() {
        _showUnsavedChangesDialog.value = false
        pendingNavigation?.invoke()
        pendingNavigation = null
    }

    fun onDismissUnsavedChangesDialog() {
        _showUnsavedChangesDialog.value = false
        pendingNavigation = null
    }

    fun clearCallback() {
        hasUnsavedChangesCallback = { false }
    }
} 