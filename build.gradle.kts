plugins {
    kotlin("jvm") version "2.2.20"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

application {
    // Kotlin object Main with @JvmStatic main
    mainClass.set("Main")
}

sourceSets {
    val main by getting {
        kotlin.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("src"))
        // include Mundos as resources so they get on classpath if needed
        resources.srcDir("src/Mundos")
    }
    val test by getting {
        kotlin.setSrcDirs(listOf("test"))
        resources.setSrcDirs(listOf("test"))
    }
}

// Ensure Mundos directory is present in run working dir for FileReader("Mundos/...")
val prepareRunResources by tasks.registering(Sync::class) {
    from("src/Mundos")
    into(layout.buildDirectory.dir("run/Mundos"))
}

tasks.named<JavaExec>("run") {
    dependsOn(prepareRunResources)
    workingDir = layout.buildDirectory.dir("run").get().asFile
}
