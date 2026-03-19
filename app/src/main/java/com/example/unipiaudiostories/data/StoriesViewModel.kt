package com.example.unipiaudiostories.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel responsible for managing and fetching story data from Firestore.
 * Exposes UI state via StateFlow.
 */
class StoriesViewModel : ViewModel() {

    // Backing property for the list of stories
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories

    // Backing property for loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchStories()
    }

    /**
     * Asynchronously fetches the "stories" collection from Firebase Firestore.
     * Maps the results to Story objects and updates the UI state.
     */
    private fun fetchStories() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val result = db.collection("stories").get().await()

                val storyList = result.documents.mapNotNull { document ->
                    // Convert JSON document to Story object and inject the Document ID
                    document.toObject(Story::class.java)?.copy(id = document.id)
                }

                _stories.value = storyList
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error fetching stories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}