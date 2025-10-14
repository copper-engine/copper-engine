plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
    id("com.github.hierynomus.license-base") version "0.16.1"
    id("com.gradleup.nmcp") version "1.2.0"
    id("com.gradleup.nmcp.aggregation") version "1.2.0"
    `maven-publish`
    `java-library`
    signing
}

nmcpAggregation {
    centralPortal {
        username = project.findProperty("SONA_TOKEN_USERNAME")?.toString() ?: ""
        password = project.findProperty("SONA_TOKEN_PASSWORD")?.toString() ?: ""
        publishingType = "USER_MANAGED"
    }
}
dependencies {
    nmcpAggregation(project(":projects:copper-coreengine"))
    nmcpAggregation(project(":projects:copper-ext"))
    nmcpAggregation(project(":projects:copper-jmx-interface"))
    nmcpAggregation(project(":projects:copper-regtest"))
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "com.gradleup.nmcp")

    group = "org.copper-engine"

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }
    tasks.register<Jar>("createSourcesJar") {
        dependsOn(tasks.classes)
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    tasks.register<Jar>("createJavadocJar") {
        dependsOn(tasks.javadoc)
        archiveClassifier.set("javadoc")
        from(tasks.javadoc.get().destinationDir)
    }

    artifacts {
        archives(tasks.named("createSourcesJar"))
        archives(tasks.named("createJavadocJar"))
    }

    apply(plugin = "com.github.hierynomus.license")
    license {
        header = file("$rootDir/common/apache-license-file.txt")
        setSkipExistingHeaders(true)
        setIgnoreFailures(true)
    }

    signing {
        val signingKeyId = project.findProperty("SIGNING_KEY_ID")?.toString() ?: ""
        val signingKey = project.findProperty("SECRING_HEX")?.toString() ?: ""
        val signingPassword = project.findProperty("SIGNING_PASSWORD")?.toString() ?: ""
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }

    publishing {
        publications {
            signing.sign(
                create<MavenPublication>("library") {
                    from(components["java"])
                    pom {
                        name.set("COPPER high-performance workflow engine")
                        groupId = "io.github.keymaster65"
                        packaging = "jar"
                        description.set("COPPER is an open-source, powerful, light-weight, and easily configurable workflow engine. The power of COPPER is that it uses Java as a description language for workflows.")
                        url.set("http://copper-engine.org/")

                        scm {
                            url.set("https://github.com/copper-engine/copper-engine")
                            connection.set("scm:git@github.com:copper-engine/copper-engine.git")
                        }

                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }

                        developers {
                            developer {
                                id.set("copper-team")
                                name.set("Copper Engine Development Team")
                            }
                        }
                    }
                }
            )
        }
    }

    dependencies {
        implementation("com.github.javaparser:javaparser-symbol-solver-core:3.6.23") {
            exclude(module = "javaparser-symbol-solver-model")
        }
        implementation("org.slf4j:slf4j-api:2.0.13")

        testImplementation("junit:junit:4.13.2") {
            exclude(module = "hamcrest-core")
        }
        testImplementation("org.mockito:mockito-core:5.11.0")
        testImplementation("net.bytebuddy:byte-buddy:1.14.13")
        testImplementation("org.hamcrest:hamcrest-core:2.2")
        testImplementation("ch.qos.logback:logback-classic:1.5.6")
    }

    tasks.jar {
        manifest {
            attributes["provider"] = "gradle"
        }
    }
}