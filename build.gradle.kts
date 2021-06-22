val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    jacoco
    application
    `maven-publish`
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.10"
    id("net.nemerosa.versioning") version "+"
    id("org.jlleitschuh.gradle.ktlint") version "+"
    id("com.avast.gradle.docker-compose") version "+"
    id("org.sonarqube") version "+"
}

group = "io.github.flecomte"
version = versioning.info.run {
    if (dirty) {
        versioning.info.full
    } else {
        versioning.info.lastTag
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}

application {
    mainClass.set("io.github.flecomte.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
    // lockMode.set(LockMode.STRICT)
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

tasks.distZip.configure { enabled = false }
tasks.distTar.configure { enabled = false }

apply(plugin = "docker-compose")
dockerCompose {
    projectName = "skilningur"
    useComposeFiles = listOf("docker-compose.yml")
    startedServices = listOf("app", "postgres", "openapi")
    stopContainers = false
    removeVolumes = false
    removeContainers = false
    isRequiredBy(project.tasks.run)

    createNested("test").apply {
        projectName = "skilningur_test"
        useComposeFiles = listOf("docker-compose-test.yml")
        stopContainers = false
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

tasks.sonarqube.configure {
    dependsOn(tasks.jacocoTestReport)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    if (versioning.info.dirty == false) {
        repositories {
            maven {
                name = "skilningur"
                group = "io.github.flecomte"
                url = uri("https://maven.pkg.github.com/flecomte/skilningur")
                credentials {
                    username = System.getenv("GITHUB_USERNAME")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {
            create<MavenPublication>("skilningur") {
                from(components["java"])
                artifact(sourcesJar)
            }
        }
    } else {
        org.slf4j.LoggerFactory.getLogger("gradle")
            .error("The git is DIRTY (${versioning.info.full})")
    }
}
