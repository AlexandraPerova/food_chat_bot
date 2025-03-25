package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup

object MenuHandler {
    fun showMainMenu(bot: Bot, chatId: Long) {
        val menuItems = listOf(
            "1. Показать все рецепты",
            "2. Поиск нужного рецепта",
            "3. Добавление нового рецепта"
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
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Список всех рецептов:\n1. Паста Карбонара\n2. Омлет\n3. Салат Цезарь",
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
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Введите новый рецепт в формате:\nНазвание\nИнгредиенты\nИнструкция",
                    replyMarkup = createBackToMenuKeyboard()
                )
            }
            "Назад в меню" -> {
                showMainMenu(bot, chatId)
            }
            else -> {
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Неизвестная команда. Хотите вернуться в меню?",
                    replyMarkup = createBackToMenuKeyboard()
                )
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
}
