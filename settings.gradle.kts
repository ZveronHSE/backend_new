rootProject.name = "backend_new"

pluginManagement {
    val kotlinVersion: String by settings
    val springVersion: String by settings
    val springDependencyManagementPluginVersion = "1.0.14.RELEASE"

    plugins {
        id("org.springframework.boot") version springVersion
        id("io.spring.dependency-management") version springDependencyManagementPluginVersion

        id("org.jooq.codegen") version "3.15.2"

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
    }

    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven { url = uri("https://plugins.gradle.org/m2/") }
        }
    }
}

include(
        "address_service",
        "parameter_service",
        "blacklist_service",
        "favorites_service",
        "blacklist_service",
        "service_registry",
        "apigateway",
        "lot_service",
        "profile_service",
        "auth-service",
        "object-storage",
        "push_notification",
        "e2e",
        "chat_service",
        "order-service",
    "specialist-service",
)