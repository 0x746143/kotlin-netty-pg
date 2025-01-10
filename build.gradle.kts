plugins {
    kotlin("jvm") version "2.1.0"
}

group = "x746143"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.netty.all)
    implementation(libs.scram.client)
    implementation(libs.logback.classic)
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}