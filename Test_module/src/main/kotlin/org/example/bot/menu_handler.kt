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

    // Функция для отображения всех рецептов как кнопок
    fun showAllRecipes(bot: Bot, chatId: Long) {
        val recipes = RecipeStorage.getRecipes()
        val recipeButtons = recipes.map { recipe ->
            KeyboardButton(recipe.name)
        }.chunked(1)  // Кнопки по одной в ряд

        val keyboardMarkup = KeyboardReplyMarkup(
            keyboard = recipeButtons,
            resizeKeyboard = true
        )

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = "Выберите рецепт:",
            replyMarkup = keyboardMarkup
        )
    }

    // Обработка выбора рецепта
    fun handleMenuSelection(bot: Bot, chatId: Long, text: String) {
        when {
            text.startsWith("1. Показать все рецепты") -> {
                val recipes = RecipeStorage.getRecipes()
                val recipeList = recipes.joinToString("\n") { it.name }
                val buttons = recipes.map { recipe ->
                    KeyboardButton(recipe.name)
                }.chunked(1)

                val keyboardMarkup = KeyboardReplyMarkup(
                    keyboard = buttons,
                    resizeKeyboard = true
                )

                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Список всех рецептов:\n$recipeList",
                    replyMarkup = keyboardMarkup
                )
            }

            text.startsWith("2. Поиск нужного рецепта") -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите название рецепта для поиска:",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }

            text.startsWith("3. Добавление нового рецепта") -> {
                isAddingRecipe = true
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите название рецепта:",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }

            text.startsWith("4. Изменить рецепт") -> {
                isEditingRecipe = true
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите название рецепта, который вы хотите изменить:",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }

            text == "Назад в меню" -> {
                // Сброс всех флагов при выходе в главное меню
                resetRecipeData()
                showMainMenu(bot, chatId)
            }

            // Обработка названия рецепта
            else -> {
                val recipe = RecipeStorage.getRecipeByName(text)
                if (recipe != null) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = """
                        Рецепт найден:
                        Название: ${recipe.name}
                        Ингредиенты: ${recipe.ingredients.joinToString(", ")}
                        Шаги: ${recipe.instructions.joinToString("\n")}
                        ${recipe.imageUrl ?: "Изображение отсутствует"}
                    """.trimIndent(),
                        replyMarkup = createBackToMenuKeyboard()
                    )
                } else {
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Рецепт с таким названием не найден.",
                        replyMarkup = createBackToMenuKeyboard()
                    )
                }
            }
        }
    }

    // Функция для отображения рецепта с кнопкой "Изменить"
    private fun showRecipeDetailsWithEditOption(bot: Bot, chatId: Long, recipe: Recipe) {
        val recipeDetails = buildString {
            append("Рецепт: ${recipe.name}\n")
            append("Ингредиенты:\n")
            recipe.ingredients.forEach { ingredient ->
                append("- $ingredient\n")
            }
            append("Шаги приготовления:\n")
            recipe.instructions.forEach { step ->
                append("$step\n")
            }
            if (recipe.imageUrl != null) {
                append("Изображение рецепта: ${recipe.imageUrl}")
            } else {
                append("Изображение не загружено.")
            }
        }

        val editButton = KeyboardButton("Изменить")
        val keyboardMarkup = KeyboardReplyMarkup(
            keyboard = listOf(listOf(editButton)),
            resizeKeyboard = true
        )

        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = recipeDetails,
            replyMarkup = keyboardMarkup
        )
    }

    // Функция для изменения рецепта
    private fun handleRecipeEdit(bot: Bot, chatId: Long, text: String) {
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
                    recipe.instructions.addAll(text.split("\n"))
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Загрузите изображение для рецепта (отправьте фото)."
                    )
                }
                recipe.imageUrl == null -> {
                    // Здесь будет обработка загрузки изображения
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Ваш рецепт обновлён! Нажмите кнопку 'Сохранить', чтобы завершить."
                    )

                    // Обновляем рецепт в хранилище
                    RecipeStorage.updateRecipe(recipe)

                    // Сброс данных после редактирования
                    resetRecipeData()
                }
                else -> {
                    // Этот блок можно использовать, если какие-то неожиданные состояния попадут
                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "Что-то пошло не так. Пожалуйста, попробуйте снова.",
                        replyMarkup = createBackToMenuKeyboard()
                    )
                }
            }
        } ?: run {
            // Если рецепт не найден
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Ошибка: Рецепт не найден. Пожалуйста, попробуйте снова.",
                replyMarkup = createBackToMenuKeyboard()
            )
        }
    }


    // Обработка ввода рецепта
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

    private fun resetRecipeData() {
        // Сброс всех данных о рецептах, чтобы очистить состояние
        isAddingRecipe = false
        isEditingRecipe = false
        currentRecipeName = null
        currentRecipeIngredients.clear()
        currentRecipeInstructions.clear()
        currentRecipeImageUrl = null
        currentEditingRecipe = null
    }

    private fun createBackToMenuKeyboard(): KeyboardReplyMarkup {
        val backButton = KeyboardButton("Назад в меню")
        return KeyboardReplyMarkup(
            keyboard = listOf(listOf(backButton)),
            resizeKeyboard = true
        )
    }
}
