
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.MinecraftChat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.7.20"
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("ink.ptms.core:v12104:12104:mapped")
    compileOnly("paper:v12104:12104:core")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

taboolib {
    description {
        name("EnhancedMobs")
        desc("一个自定义生物插件")
        contributors {
            name("延时qwq")
        }
    }
    
    env {
        install(BukkitUtil, Bukkit, MinecraftChat, Basic)
    }
    
    version { taboolib = "6.2.3" }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}