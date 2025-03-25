package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import org.example.model.Recipe
import org.example.storage.RecipeStorage

object MenuHandler {

    private var isAddingRecipe = false
    private var isEditingRecipe = false
    private var currentRecipeName: String? = null
    private var currentRecipeIngredients: MutableList<String> = mutableListOf()
    private var currentRecipeInstructions: MutableList<String> = mutableListOf()
    private var currentRecipeImageUrl: String? = null
    private var currentEditingRecipe: Recipe? = null

    fun showMainMenu(bot: Bot, chatId: Long) {
        val menuItems = listOf(
            "1. Показать все рецепты",
            "2. Поиск нужного рецепта",
            "3. Добавление нового рецепта",
            "4. Изменить рецепт"
        )

        val keyboardButtons = menuItems.map { menuItem ->
            KeyboardButton(menuItem)
        }.chunked(1)

        val keyboardMarkup = KeyboardReplyMarkup(
            keyboard = keyboardButtons,
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
            "1. Показать все рецепты" -> {
                val recipes = RecipeStorage.getRecipes()
                val recipeList = recipes.joinToString("\n") { it.name }
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Список всех рецептов:\n$recipeList",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }
            "2. Поиск нужного рецепта" -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите название рецепта для поиска:",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }
            "3. Добавление нового рецепта" -> {
                isAddingRecipe = true
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите название рецепта:",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }
            "4. Изменить рецепт" -> {
                isEditingRecipe = true
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите название рецепта, который вы хотите изменить:",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }
            "Назад в меню" -> {
                showMainMenu(bot, chatId)
            }
            else -> {
                if (isAddingRecipe) {
                    handleRecipeInput(bot, chatId, text)
                } else if (isEditingRecipe) {
                    handleRecipeEdit(bot, chatId, text)
                } else {
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Неизвестная команда. Хотите вернуться в меню?",
                        replyMarkup = createBackToMenuKeyboard()
                    )
                }
            }
        }
    }

    private fun handleRecipeInput(bot: Bot, chatId: Long, text: String) {
        when {
            currentRecipeName == null -> {
                currentRecipeName = text
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите ингредиенты рецепта, разделённые запятыми:"
                )
            }
            currentRecipeIngredients.isEmpty() -> {
                currentRecipeIngredients.addAll(text.split(","))
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите шаги приготовления рецепта (все в одном сообщении, разделённые новым абзацем):"
                )
            }
            currentRecipeInstructions.isEmpty() -> {
                currentRecipeInstructions.addAll(text.split(","))
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Загрузите изображение для рецепта (отправьте фото)."
                )
            }
            currentRecipeImageUrl == null -> {
                // Здесь можно обработать загрузку изображения
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Ваш рецепт добавлен!"
                )

                // Сохраняем рецепт в хранилище
                val recipe = Recipe(
                    name = currentRecipeName!!,
                    ingredients = currentRecipeIngredients,
                    instructions = currentRecipeInstructions,
                    imageUrl = currentRecipeImageUrl
                )
                RecipeStorage.addRecipe(recipe)

                // Сброс данных после добавления рецепта
                resetRecipeData()
            }
        }
    }

    private fun handleRecipeEdit(bot: Bot, chatId: Long, text: String) {
        val recipe = RecipeStorage.getRecipeByName(text)
        if (recipe != null) {
            currentEditingRecipe = recipe
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Вы выбрали рецепт '${recipe.name}'. Теперь вы можете изменить его. Введите новые ингредиенты, разделённые запятыми:"
            )
        } else {
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Рецепт с таким названием не найден. Попробуйте снова.",
                replyMarkup = createBackToMenuKeyboard()
            )
        }
    }

    // Функция для редактирования рецепта после того как мы нашли его
    fun editExistingRecipe(bot: Bot, chatId: Long, text: String) {
        currentEditingRecipe?.let { recipe ->
            when {
                recipe.ingredients.isEmpty() -> {
                    recipe.ingredients.addAll(text.split(","))
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Введите шаги приготовления рецепта (все в одном сообщении, разделённые новым абзацем):"
                    )
                }

                recipe.instructions.isEmpty() -> {
                    recipe.instructions.addAll(text.split(","))
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Загрузите изображение для рецепта (отправьте фото)."
                    )
                }

                recipe.imageUrl == null -> {
                    // Обработка загрузки изображения
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Ваш рецепт обновлён!"
                    )

                    // Сохраняем изменения рецепта в хранилище
                    RecipeStorage.updateRecipe(recipe)

                    // Сброс данных после редактирования
                    resetRecipeData()
                }

                else -> {
                    // Этот блок обрабатывает случай, когда все данные рецепта уже заполнены
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Рецепт уже полностью обновлён. Хотите вернуться в меню?",
                        replyMarkup = createBackToMenuKeyboard()
                    )

                    // Сброс данных после завершения редактирования
                    resetRecipeData()
                }
            }
        }
    }

    private fun createBackToMenuKeyboard(): KeyboardReplyMarkup {
        val backButton = KeyboardButton("Назад в меню")
        return KeyboardReplyMarkup(
            keyboard = listOf(listOf(backButton)),
            resizeKeyboard = true
        )
    }

    // Сброс данных рецепта после сохранения
    private fun resetRecipeData() {
        currentRecipeName = null
        currentRecipeIngredients.clear()
        currentRecipeInstructions.clear()
        currentRecipeImageUrl = null
        isAddingRecipe = false
        isEditingRecipe = false
        currentEditingRecipe = null
    }
}
