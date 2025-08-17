plugins {
    kotlin("jvm") version "2.0.20" // Kotlin
    id("com.github.johnrengelman.shadow") version "8.1.1" // Shadow
    id("xyz.jpenilla.run-paper") version "2.3.1" // Run Paper
    id("maven-publish") // Maven Publish
}

group = "top.catnies"
version = "1.0.12"
kotlin.jvmToolchain(17)

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://maven.chengzhimeow.cn/releases") // ChengZhiMeow
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compileOnly("com.zaxxer:HikariCP:5.1.0") // Hikari CP
    implementation("cn.chengzhiya:MHDF-Scheduler:1.0.1") // Scheduler
    compileOnly("me.clip:placeholderapi:2.11.6") // PlaceholderAPI
}

tasks{
    build {
        dependsOn("shadowJar")
    }
    runServer {
        dependsOn(shadowJar)
        dependsOn(jar)
        minecraftVersion("1.21.8")
        downloadPlugins {
            hangar("PlaceholderAPI", "2.11.6")
        }
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

publishing {
    repositories {
        maven {
            isAllowInsecureProtocol = true
            name = "Catnies"
            url = uri("http://repo.catnies.top/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "top.catnies"
            artifactId = "firOnlineTime"
            version = project.version.toString()
            from(components["java"])
        }
    }
}