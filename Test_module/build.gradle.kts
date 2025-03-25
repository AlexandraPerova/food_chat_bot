group = "org.example.bot"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }  // Добавьте JitPack
}

dependencies {
    implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation ("com.google.code.gson:gson:2.8.8")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.example.bot.MainKt")
}