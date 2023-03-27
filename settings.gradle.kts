rootProject.name = "backend_new"

pluginManagement {
    val kotlinVersion: String by settings
    val springVersion: String by settings
    val springDependencyManagementPluginVersion = "1.0.14.RELEASE"

    plugins {
        id("org.springframework.boot") version springVersion
        id("io.spring.dependency-management") version springDependencyManagementPluginVersion

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
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

)