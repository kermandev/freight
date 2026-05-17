plugins {
    id("java")
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.spotless)
}

group = "dev.kerman"
version = findProperty("version") ?: "dev"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.minestom)
    compileOnly(libs.adventure)
    compileOnly(libs.jetbrains.annotations)
    testImplementation(libs.minestom.testing)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        modularity.inferModulePath.set(true)

        compileJava {
            options.compilerArgs.add("-Werror")
        }
    }

    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://javadoc.minestom.net/")
            links("https://jd.papermc.io/adventure/${libs.versions.adventure.get()}/")
            links("https://javadoc.io/doc/org.jetbrains/annotations/${libs.versions.jetbrains.annotations.get()}/")
            addBooleanOption("Xdoclint:all,-missing", true)
        }
    }

    test {
        useJUnitPlatform()

        // Required for Minestom tests to run properly
        jvmArgs("-Dminestom.viewable-packet=false")
        jvmArgs("-Dminestom.inside-test=true")
    }
}

spotless {
    format("misc") {
        target("*.gradle.kts", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
    java {
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = !version.toString().contains("v"))
    signAllPublications()

    pom {
        name.set("Freight")
        description.set("A messaging wrapper for the BungeeCord protocol, designed for Minestom.")
        inceptionYear.set("2025")
        url.set("https://github.com/kermandev/freight/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("kermandev")
                name.set("Kerman")
                url.set("https://github.com/kermandev/")
            }
        }
        scm {
            url.set("https://github.com/kermandev/freight/")
            connection.set("scm:git:git://github.com/kermandev/freight.git")
            developerConnection.set("scm:git:ssh://git@github.com/kermandev/freight.git")
        }
    }
}
