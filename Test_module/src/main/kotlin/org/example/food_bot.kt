package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel

fun main() {
    val bot = bot {
        token = "8159582597:AAEeNMO_vW2EAh9PPuFNhTUU-SON3M7V3ZM"

        logLevel = LogLevel.Error


        dispatch {
            command("start") {
                val chatId = message?.chat?.id ?: return@command
                bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Привет! Я бот! 🎉")
            }
        }
    }

    bot.startPolling()
}
