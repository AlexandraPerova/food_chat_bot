package org.example.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.bot.MenuHandler

class BotHandler {
    private val token = "8159582597:AAEeNMO_vW2EAh9PPuFNhTUU-SON3M7V3ZM"

    private val bot = bot {
        token = this@BotHandler.token
        logLevel = LogLevel.Error

        dispatch {
            command("start") {
                val chatId = message?.chat?.id ?: return@command
                MenuHandler.showMainMenu(bot, chatId)
            }

            command("menu") {
                val chatId = message?.chat?.id ?: return@command
                MenuHandler.showMainMenu(bot, chatId)
            }

            text {
                val chatId = message?.chat?.id ?: return@text
                val text = message?.text ?: return@text
                MenuHandler.handleMenuSelection(bot, chatId, text)
            }
        }
    }

    fun start() {
        bot.startPolling()
    }
}
