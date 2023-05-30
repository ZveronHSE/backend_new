import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.liquibase.gradle") version "2.1.1"
    id("com.google.cloud.tools.jib") version "3.3.1"

    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

group = "ru.zveron"
version = "0.0.1"

val protobufVersion: String by project
val grpcVersion: String by project
val grpcKotlinVersion: String by project
val springVersion: String by project
val kotlinVersion: String by project
val eurekaVersion: String by project
val kotlinxVersion: String by project
val testcontainersVersion: String by project
val arch = System.getProperty("os.arch")
val jooqVersion: String = "3.16.6"

val modulesWithoutPostgreSql = setOf("e2e", "chat_service")


allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

buildscript {
    dependencies {
        classpath("org.yaml:snakeyaml:1.33")
    }
}

subprojects {
    data class DataSource(
        // TODO надо бы все это вынести в отдельный градл скриптик
        val url: String,
        val username: String,
        val password: String,
    )

    extra["getDataSource"] = fun(): DataSource? { // TODO надо бы все это вынести в отдельный градл скриптик
        val pathToApplicationYml = "src/main/resources/application.yml"

        if (project.name in modulesWithoutPostgreSql) {
            return null
        }

        val applicationYaml = try {
            File(projectDir, pathToApplicationYml).inputStream().use {
                (org.yaml.snakeyaml.Yaml().load(it) as Map<*, *>)
            }
        } catch (ex: Exception) {
            throw IllegalArgumentException("message=${ex.message}")
        }

        try {
            val map = ((applicationYaml["spring"] as Map<*, *>)["datasource"] as Map<*, *>)

            return DataSource(
                url = map["url"] as String,
                username = map["username"] as String,
                password = map["password"] as String
            )
        } catch (ex: Exception) {
            throw IllegalArgumentException(
                "Вероятно, не хватает в application.yml " +
                    "параметров url, username или password: ${ex.message}"
            )
        }
    }

    apply {
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.liquibase.gradle")
        plugin("com.google.cloud.tools.jib")
    }

    dependencies {
        // Для компиляции проекта
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        // Grpc корутины, сервисы и клиенты
        implementation("net.devh:grpc-spring-boot-starter:2.14.0.RELEASE")
        implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
        implementation("io.grpc:grpc-stub:$grpcVersion")
        implementation("io.grpc:grpc-core:$grpcVersion")

        //Настоящие корутины
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")

        // Логгирование
        implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
        //logging
        implementation("net.logstash.logback:logstash-logback-encoder:7.2")
        implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
        implementation("org.apache.logging.log4j:log4j-core:2.17.2")

        // База данных
        if (project.name != "chat_service") {
            implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springVersion")
            implementation("org.postgresql:postgresql:42.3.8")
            implementation("org.liquibase:liquibase-core:4.18.0")
            implementation("org.hibernate:hibernate-core:5.6.7.Final")
        }

        //Eureka
        implementation("org.springframework.boot:spring-boot-starter-web:$springVersion")
        implementation("org.springframework:spring-webflux:5.3.24")
        implementation("io.projectreactor.netty:reactor-netty:1.1.1")
        if (project.name != "service_registry") {
            implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:$eurekaVersion")
        }

        //Без этой штуки у М1 жопа отваливается с вебфлаксом
        if (arch.equals("aarch64")) {
            implementation("io.netty:netty-resolver-dns-native-macos:4.1.86.Final:osx-aarch_64")
        }

        // Миграции ликвибейза
        liquibaseRuntime("org.springframework.data:spring-data-jpa:$springVersion")
        liquibaseRuntime("org.liquibase.ext:liquibase-hibernate5:4.8.0")
        liquibaseRuntime("org.springframework.boot:spring-boot:$springVersion")
        liquibaseRuntime(sourceSets.getByName("main").compileClasspath)
        liquibaseRuntime(sourceSets.getByName("main").output)

        // Платформенная либа:
        implementation("com.github.zveronHse:platform-library:1.1.0")

        // Тесты
        testImplementation("io.grpc:grpc-testing:$grpcVersion")
        testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
        if (project.name != "chat_service") {
            testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
        }
        testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
        testImplementation("com.ninja-squad:springmockk:4.0.0")
        testImplementation("io.kotest:kotest-assertions-core-jvm:5.3.1")
        testImplementation("org.assertj:assertj-core:3.22.0")
        testImplementation("io.mockk:mockk:1.13.3")
    }

    group = rootProject.group
    version = rootProject.version

    java.sourceCompatibility = JavaVersion.VERSION_17

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all","-Xss512k",)
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    val dataSource = (extra["getDataSource"] as () -> DataSource?)()

    tasks.register("createNextChangeSet") {
        dataSource ?: return@register
        group = "liquibase"

        val migrationName: String? by project

        dependsOn("build")
        dependsOn("clean")
        dependsOn("diffChangeLog")
        tasks.findByName("build")!!.mustRunAfter("clean")
        tasks.findByName("diffChangeLog")!!.mustRunAfter("build")

        val changeSetName = migrationName ?: project.properties["changeSetDefaultName"]
        val changeSetUrl = dataSource.url
        val changeSetUsername = dataSource.username
        val changeSetPassword = dataSource.password
        val changeSetDriver = project.properties["dbDriver"]
        val changeSetsDirectory = "src/main/resources/db/changelog/"
        val changeSetReferenceUrl = project.properties["referenceUrl"]
        val changeSetNumber = getNextChangeSetNumber(changeSetsDirectory)

        liquibase {
            activities.register("main") {
                this.arguments = mapOf(
                    "url" to changeSetUrl,
                    "password" to changeSetPassword,
                    "username" to changeSetUsername,
                    "driver" to changeSetDriver,
                    "referenceUrl" to changeSetReferenceUrl,
                    "changeLogFile" to "$changeSetsDirectory${changeSetNumber}_${changeSetName}.sql"
                )
            }
            runList = "main"
        }
    }
}

// TODO надо бы все это вынести в отдельный градл скриптик, можно будет сделать отдельный класс и покрыть его тестами
fun getNextChangeSetNumber(path: String): String {
    val result = (
        File(path)
            .listFiles()
            ?.mapNotNull { file -> file.name.split('_').firstOrNull()?.toIntOrNull() }
            ?.maxOrNull() ?: 0
        ) + 1

    return result.toString().padStart(2, '0')
}