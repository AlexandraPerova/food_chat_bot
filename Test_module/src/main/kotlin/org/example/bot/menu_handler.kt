package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import org.example.model.Recipe
import org.example.storage.RecipeStorage

object MenuHandler {

    private var isAddingRecipe = false
    private var isEditingRecipe = false
    private var currentRecipe: Recipe? = null
    private var step = 0

    fun showMainMenu(bot: Bot, chatId: Long) {
        val menuItems = listOf(
            "Показать все рецепты",
            "Поиск рецепта",
            "Добавить рецепт",
            "Изменить рецепт"
        )

        val keyboardMarkup = KeyboardReplyMarkup(
            keyboard = menuItems.map { listOf(KeyboardButton(it)) },
            resizeKeyboard = true
        )

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = "Выберите действие:",
            replyMarkup = keyboardMarkup
        )
    }

    fun handleMenuSelection(bot: Bot, chatId: Long, text: String) {
        when (text) {
            "Показать все рецепты" -> showAllRecipes(bot, chatId)
            "Поиск рецепта" -> {
                bot.sendMessage(ChatId.fromId(chatId), "Введите название рецепта для поиска:")
                isEditingRecipe = false
            }
            "Добавить рецепт" -> {
                isAddingRecipe = true
                step = 1
                bot.sendMessage(ChatId.fromId(chatId), "Введите название рецепта:")
            }
            "Изменить рецепт" -> {
                isEditingRecipe = true
                bot.sendMessage(ChatId.fromId(chatId), "Введите название рецепта, который хотите изменить:")
            }
            "Назад в меню" -> {
                resetState()
                showMainMenu(bot, chatId)
            }
            else -> handleRecipeActions(bot, chatId, text)
        }
    }

    private fun showAllRecipes(bot: Bot, chatId: Long) {
        val recipes = RecipeStorage.getRecipes()
        if (recipes.isEmpty()) {
            bot.sendMessage(ChatId.fromId(chatId), "Рецепты отсутствуют.")
            return
        }

        val keyboardMarkup = KeyboardReplyMarkup(
            keyboard = recipes.map { listOf(KeyboardButton(it.name)) },
            resizeKeyboard = true
        )

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = "Выберите рецепт:",
            replyMarkup = keyboardMarkup
        )
    }

    private fun handleRecipeActions(bot: Bot, chatId: Long, text: String) {
        when {
            isAddingRecipe -> handleRecipeAdding(bot, chatId, text)
            isEditingRecipe -> handleRecipeEditing(bot, chatId, text)
            else -> showRecipeDetails(bot, chatId, text)
        }
    }

    private fun handleRecipeAdding(bot: Bot, chatId: Long, text: String) {
        when (step) {
            1 -> {
                currentRecipe = Recipe(name = text, ingredients = mutableListOf(), instructions = mutableListOf(), imageUrl = null)
                step++
                bot.sendMessage(ChatId.fromId(chatId), "Введите ингредиенты (через запятую):")
            }
            2 -> {
                currentRecipe?.ingredients?.addAll(text.split(",").map { it.trim() })
                step++
                bot.sendMessage(ChatId.fromId(chatId), "Введите шаги приготовления (каждый шаг с новой строки):")
            }
            3 -> {
                currentRecipe?.instructions?.addAll(text.split("\n").map { it.trim() })
                step++
                bot.sendMessage(ChatId.fromId(chatId), "Отправьте фото рецепта (или напишите 'Пропустить'):")
            }
            4 -> {
                if (text.lowercase() != "пропустить") {
                    currentRecipe?.imageUrl = text
                }
                RecipeStorage.addRecipe(currentRecipe!!)
                bot.sendMessage(ChatId.fromId(chatId), "Рецепт успешно добавлен!", replyMarkup = createBackToMenuKeyboard())
                resetState()
            }
        }
    }

    private fun handleRecipeEditing(bot: Bot, chatId: Long, text: String) {
        if (currentRecipe == null) {
            currentRecipe = RecipeStorage.getRecipeByName(text)
            if (currentRecipe == null) {
                bot.sendMessage(ChatId.fromId(chatId), "Рецепт не найден.")
                return
            }
            bot.sendMessage(ChatId.fromId(chatId), "Введите новые ингредиенты (через запятую):")
            step = 1
        } else {
            when (step) {
                1 -> {
                    currentRecipe!!.ingredients = text.split(",").map { it.trim() }.toMutableList()
                    step++
                    bot.sendMessage(ChatId.fromId(chatId), "Введите новые шаги приготовления (каждый шаг с новой строки):")
                }
                2 -> {
                    currentRecipe!!.instructions = text.split("\n").map { it.trim() }.toMutableList()
                    step++
                    bot.sendMessage(ChatId.fromId(chatId), "Отправьте новую ссылку на изображение (или напишите 'Пропустить'):")
                }
                3 -> {
                    if (text.lowercase() != "пропустить") {
                        currentRecipe!!.imageUrl = text
                    }
                    RecipeStorage.updateRecipe(currentRecipe!!)
                    bot.sendMessage(ChatId.fromId(chatId), "Рецепт успешно обновлен!", replyMarkup = createBackToMenuKeyboard())
                    resetState()
                }
            }
        }
    }

    private fun showRecipeDetails(bot: Bot, chatId: Long, recipeName: String) {
        val recipe = RecipeStorage.getRecipeByName(recipeName)
        if (recipe == null) {
            bot.sendMessage(ChatId.fromId(chatId), "Рецепт не найден.")
            return
        }

        val recipeDetails = buildString {
            append("📌 *${recipe.name}*\n\n")
            append("🍽 *Ингредиенты:*\n")
            recipe.ingredients.forEach { append("- $it\n") }
            append("\n📖 *Шаги приготовления:*\n")
            recipe.instructions.forEachIndexed { index, step -> append("${index + 1}. $step\n") }
            if (recipe.imageUrl != null) append("\n🖼 [Изображение](${recipe.imageUrl})")
        }

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = recipeDetails,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = createBackToMenuKeyboard()
        )
    }

    private fun resetState() {
        isAddingRecipe = false
        isEditingRecipe = false
        currentRecipe = null
        step = 0
    }

    private fun createBackToMenuKeyboard(): KeyboardReplyMarkup {
        return KeyboardReplyMarkup(
            keyboard = listOf(listOf(KeyboardButton("Назад в меню"))),
            resizeKeyboard = true
        )
    }
}
