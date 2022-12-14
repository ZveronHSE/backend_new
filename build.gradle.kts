import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    idea

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "ru.zveron"
version = "0.0.1"

val protobufVersion: String by project
val grpcVersion: String by project
val grpcKotlinVersion: String by project

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
        implementation("io.grpc:grpc-stub:$grpcVersion")
        implementation("io.grpc:grpc-core:$grpcVersion")
    }

    group = rootProject.group
    version = rootProject.version

    java.sourceCompatibility = JavaVersion.VERSION_17

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
}