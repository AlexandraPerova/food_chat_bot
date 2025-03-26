package org.example.model

data class Recipe(
    val name: String,
    var ingredients: MutableList<String> = mutableListOf(),
    var instructions: MutableList<String> = mutableListOf(),
    var imageUrl: String? = null // Добавляем поле для хранения URL изображения
)
