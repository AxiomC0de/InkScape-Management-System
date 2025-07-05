package com.joshua.inkscape.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryViewModel : ViewModel() {

    private val database = Firebase.database
    private val categoriesRef = database.getReference("categories")

    private val _allCategories = MutableStateFlow<List<String>>(emptyList())
    private val _pendingDeletionCategory = MutableStateFlow<String?>(null)
    private var deletionJob: Job? = null


    val categories: StateFlow<List<String>> =
        combine(_allCategories, _pendingDeletionCategory) { categories, pendingDeletion ->
            if (pendingDeletion != null) {
                categories.filter { it != pendingDeletion }
            } else {
                categories
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var newCategoryName by mutableStateOf("")

    init {
        listenForCategoryChanges()
    }

    private fun listenForCategoryChanges() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                _allCategories.value = categoryList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryViewModel", "Failed to listen for category changes.", error.toException())
            }
        }
        categoriesRef.addValueEventListener(listener)
    }

    fun addCategory() {
        if (newCategoryName.isBlank()) return

        viewModelScope.launch {
            try {
                val currentCategories = _allCategories.value.toMutableList()
                if (!currentCategories.contains(newCategoryName)) {
                    currentCategories.add(newCategoryName)
                    categoriesRef.setValue(currentCategories).await()
                }
                newCategoryName = ""
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Failed to add category.", e)
            }
        }
    }

    fun deleteCategoryWithUndo(categoryToDelete: String) {
        deletionJob?.cancel()
        _pendingDeletionCategory.value = categoryToDelete
        deletionJob = viewModelScope.launch {
            delay(5000)
            val currentCategories = _allCategories.value.toMutableList()
            currentCategories.remove(categoryToDelete)
            categoriesRef.setValue(currentCategories).await()
            _pendingDeletionCategory.value = null
        }
    }

    fun undoDelete() {
        deletionJob?.cancel()
        _pendingDeletionCategory.value = null
    }
} 