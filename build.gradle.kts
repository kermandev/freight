plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "dev.kerman"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.minestom)
    compileOnly(libs.adventure)
    testImplementation(libs.minestom.testing)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://javadoc.minestom.net/")
            links("https://jd.advntr.dev/api/${libs.versions.adventure.get()}/")
        }
    }

    test {
        useJUnitPlatform()

        // Required for Minestom tests to run properly
        jvmArgs("-Dminestom.viewable-packet=false")
        jvmArgs("-Dminestom.inside-test=true")
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = !version.toString().contains("SNAPSHOT"))
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