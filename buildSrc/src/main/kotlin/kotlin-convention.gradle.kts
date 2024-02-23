import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    java
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(Dependencies.JB_ANNOTATIONS)
    testCompileOnly(Dependencies.JB_ANNOTATIONS)

    testImplementation(Dependencies.JUNIT_JUPITER)
    testRuntimeOnly(Dependencies.JUNIT_PLATFORM)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {}
val sharedTestSourceSet = sourceSets.create("sharedTest") {}

val functionalTest by tasks.registering(Test::class) {
    group = "verification"
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
gradlePlugin.testSourceSets(sharedTestSourceSet)

configurations["sharedTestImplementation"].extendsFrom(configurations["implementation"])
// Adds all dependencies from sharedTest to test and functionalTest
configurations.filter { conf -> conf.name.startsWith("sharedTest") }.forEach {
    if (!it.name.endsWith("ForTest")) {
        configurations["functionalT${it.name.substring(7)}"].extendsFrom(it)
        configurations["t${it.name.substring(7)}"].extendsFrom(it)
    }
}

tasks.named("check") {
    if (System.getenv("CI") == "true")
        dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("MCMappingOberrider") {
            id = "MCMappingOberrider"
            implementationClass = "io.github.mjaroslav.mcmo.MCMappingOberriderPlugin"
        }
    }
}
