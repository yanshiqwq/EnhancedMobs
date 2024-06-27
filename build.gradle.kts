import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.9.20"
    application
}

group = "cn.yanshiqwq"
version = properties["version"] as String

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("script-util", "1.8.22"))
    implementation(kotlin("script-runtime", "2.0.0"))
    implementation(kotlin("scripting-jsr223", "2.0.0"))
    implementation("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        version = project.version
        application.mainClass.set("cn.yanshiqwq.enhanced_mobs.Main")
        dependencies {
            include(dependency("org.jetbrains.kotlin:kotlin-script-util:1.8.22"))
            include(dependency("org.jetbrains.kotlin:kotlin-script-runtime"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jsr223:2.0.0"))
        }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<KotlinCompile> {
        kotlinOptions {
            //jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }
}