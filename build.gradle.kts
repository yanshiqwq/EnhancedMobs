
import io.izzel.taboolib.gradle.BUKKIT_ALL
import io.izzel.taboolib.gradle.CHAT
import io.izzel.taboolib.gradle.EXPANSION_SUBMIT_CHAIN
import io.izzel.taboolib.gradle.UNIVERSAL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.11"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.7.20"
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.Keelar:ExprK:91fdabf")
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
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
        install(UNIVERSAL, BUKKIT_ALL, CHAT, EXPANSION_SUBMIT_CHAIN)
    }
    
    version { taboolib = "6.1.2-beta10" }
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