package org.example.org.example.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel

fun main() {
    val bot = bot {
        token = "8159582597:AAEeNMO_vW2EAh9PPuFNhTUU-SON3M7V3ZM"
        logLevel = LogLevel.Error

        dispatch {
            command("start") {
                val chatId = message?.chat?.id ?: return@command
                showMainMenu(bot, chatId)
            }

            command("menu") {
                val chatId = message?.chat?.id ?: return@command
                showMainMenu(bot, chatId)
            }

            text {
                val chatId = message?.chat?.id ?: return@text
                val text = message?.text ?: return@text

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
                        // Если введён неизвестный текст, предлагаем вернуться в меню
                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Неизвестная команда. Хотите вернуться в меню?",
                            replyMarkup = createBackToMenuKeyboard()
                        )
                    }
                }
            }
        }
    }

    bot.startPolling()
}

fun showMainMenu(bot: com.github.kotlintelegrambot.Bot, chatId: Long) {
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
        resizeKeyboard = true,
        oneTimeKeyboard = false,
        selective = false
    )

    bot.sendMessage(
        chatId = ChatId.fromId(chatId),
        text = "Выберите действие:",
        replyMarkup = keyboardMarkup
    )
}

fun createBackToMenuKeyboard(): KeyboardReplyMarkup {
    val backButton = KeyboardButton("Назад в меню")
    return KeyboardReplyMarkup(
        keyboard = listOf(listOf(backButton)),
        resizeKeyboard = true,
        oneTimeKeyboard = false,
        selective = false
    )
}