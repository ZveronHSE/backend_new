import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    idea

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.protobuf") version "0.8.18"
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
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("com.google.protobuf")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
        implementation("io.grpc:grpc-stub:$grpcVersion")
        implementation("io.grpc:grpc-core:$grpcVersion")
        implementation("io.grpc:grpc-protobuf:$grpcVersion")
        implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
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

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        }
        plugins {
            id("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
            }
            id("grpckt") {
                artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
            }
        }
        generateProtoTasks {
            ofSourceSet("main").forEach {
                it.plugins {
                    id("grpc")
                    id("grpckt")
                }
                it.builtins {
                    id("kotlin")
                }
            }
        }
    }
}