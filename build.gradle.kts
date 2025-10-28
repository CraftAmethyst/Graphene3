plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "8.3.0"
}

group = "org.craftamethyst.graphene"
version = "0.1.0-SNAPSHOT"
val targetJavaVersion = 8
val pluginBaseVersion = "1.6.6"
val shadowGroup = "org.craftamethyst.graphene.libs"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.13-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.13") // NMS
    compileOnly("org.jetbrains:annotations:24.0.0")

    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("net.kyori:adventure-api:4.22.0")
    implementation("net.kyori:adventure-platform-bukkit:4.4.0")
    implementation("net.kyori:adventure-text-minimessage:4.22.0")
    implementation("de.tr7zw:item-nbt-api:2.15.3-SNAPSHOT")
    // implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    implementation("top.mrxiaom.pluginbase:library:$pluginBaseVersion")
    implementation("top.mrxiaom.pluginbase:paper:$pluginBaseVersion")
    // implementation("top.mrxiaom:LibrariesResolver:$pluginBaseVersion")
}
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    shadowJar {
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "net.kyori" to "kyori",
            // "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
    val copyTask = create<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "${project.name}-$version.jar" }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
        }
    }
}
