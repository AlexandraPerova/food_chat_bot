package org.example.model

data class Recipe(
    val name: String,
    val ingredients: MutableList<String> = mutableListOf(),
    val instructions: MutableList<String> = mutableListOf(),
    var imageUrl: String? = null // Добавляем поле для хранения URL изображения
)
