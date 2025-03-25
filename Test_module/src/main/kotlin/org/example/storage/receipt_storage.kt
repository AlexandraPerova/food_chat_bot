package org.example.storage

import org.example.model.Recipe
import java.io.File
import com.google.gson.Gson

object RecipeStorage {

    private val gson = Gson()
    private val file = File("recipes.json")

    init {
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    fun getRecipes(): List<Recipe> {
        return if (file.exists()) {
            val recipesJson = file.readText()
            val recipeList = gson.fromJson(recipesJson, Array<Recipe>::class.java).toList()
            recipeList
        } else {
            emptyList()
        }
    }

    fun getRecipeByName(name: String): Recipe? {
        return getRecipes().find { it.name == name }
    }

    fun addRecipe(recipe: Recipe) {
        val recipes = getRecipes().toMutableList()
        recipes.add(recipe)
        saveRecipes(recipes)
    }

    fun updateRecipe(updatedRecipe: Recipe) {
        val recipes = getRecipes().toMutableList()
        val index = recipes.indexOfFirst { it.name == updatedRecipe.name }
        if (index != -1) {
            recipes[index] = updatedRecipe
            saveRecipes(recipes)
        }
    }

    private fun saveRecipes(recipes: List<Recipe>) {
        val recipesJson = gson.toJson(recipes)
        file.writeText(recipesJson)
    }
}
