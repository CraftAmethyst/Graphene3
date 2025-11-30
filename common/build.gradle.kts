plugins {
    idea
    java
    id("java-library")
    id("xyz.wagyourtail.unimined")
    id("com.github.johnrengelman.shadow")
}

val minecraftVersion: String = property("minecraft_version") as String
val mcpVersion: String = property("mcp_version") as String
val shadowBundle: Configuration by configurations.creating
val noRemap: Configuration by configurations.creating

unimined.minecraft {
    version(minecraftVersion)

    mappings {
        searge()
        mcp("snapshot", mcpVersion)
    }

    defaultRemapJar = false
}

dependencies {
    //implementation("com.logisticscraft:occlusionculling:0.0.8-SNAPSHOT")
    compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "no-remap"
    archiveFileName = "${base.archivesName.get()}-${archiveClassifier.get()}.jar"
}

artifacts {
    add(noRemap.name, tasks.shadowJar)
}