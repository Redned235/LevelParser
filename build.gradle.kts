import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

description = "levelparser-parent"

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()

        maven("https://repo.opencollab.dev/maven-releases/")
        maven("https://repo.opencollab.dev/maven-snapshots/")
    }

    group = "me.redned.levelparser"
    version = "1.0-SNAPSHOT"

    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17

    tasks.withType<ShadowJar> {
        from("src/main/java/resources") {
            include("*")
        }

        archiveBaseName.set("${project.description}.jar")
        archiveClassifier.set("")
    }

    tasks.jar {
        archiveClassifier.set("unshaded")
    }

    tasks.named("build") {
        dependsOn(tasks.shadowJar)
    }

    publishing {
        publications.create<MavenPublication>("library") {
            artifact(tasks.shadowJar)
        }
    }
}