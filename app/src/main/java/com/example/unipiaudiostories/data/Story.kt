package com.example.unipiaudiostories.data

/**
 * Data model representing a Story entity.
 * Default values are required for Firebase Firestore deserialization.
 */
data class Story(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val imageUrl: String = "",
    val text: String = "",
    val durationMinutes: Int = 0,
    val ageRange: String = "",
    val year: String = "2024"
)