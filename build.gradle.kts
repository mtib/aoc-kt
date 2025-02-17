
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    kotlin("plugin.allopen") version "2.1.0"
}

group = "dev.mtib.aoc"
version = "0.24.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.slf4j:slf4j-nop:1.7.36")
    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.4")
    implementation("org.ojalgo:ojalgo:55.0.1")

    implementation("redis.clients:jedis:5.2.0")

    implementation("org.reflections:reflections:0.10.2")

    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:2.0.2")
    runtimeOnly("org.jetbrains.lets-plot:lets-plot-image-export:4.5.1")

    implementation("com.github.ajalt.mordant:mordant:3.0.1")

    implementation("tools.profiler:async-profiler:3.0")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
    testImplementation("io.kotest:kotest-property:5.9.0")
}

tasks.create("fatJar", Jar::class) {
    group = "build"
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "dev.mtib.aoc.AocRunnerKt"
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
