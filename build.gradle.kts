plugins {
    id("java")
}

group = "dev.kerman"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.minestom)
    testImplementation(libs.minestom.testing)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    test {
        useJUnitPlatform()

        // Required for Minestom tests to run properly
        jvmArgs("-Dminestom.viewable-packet=false")
        jvmArgs("-Dminestom.inside-test=true")
    }
}