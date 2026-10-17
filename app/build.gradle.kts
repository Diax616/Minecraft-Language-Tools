plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.picocli)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass = "dev.dixmk.language.minecraft.app.Laucher"
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    
    // Every JavaFX jar ships its own module-info.class; they are useless in a fat jar.
    exclude("module-info.class")
    
    archiveBaseName = "MCLanguageTools"
    archiveClassifier = "" // remove "-all"
    archiveVersion = "1.0.0"
}
