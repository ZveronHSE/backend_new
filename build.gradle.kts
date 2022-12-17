import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.liquibase.gradle") version "2.1.1"

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
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.liquibase.gradle")
    }

    dependencies {
        // Для включения Dependcy Injection и прочих аннотаций
        implementation("org.springframework.boot:spring-boot-starter-web:$springVersion")

        // Для компиляции проекта
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        // Grpc корутины, сервисы и клиенты
        implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
        implementation("io.grpc:grpc-stub:$grpcVersion")
        implementation("io.grpc:grpc-core:$grpcVersion")

        // База данных
        implementation("org.postgresql:postgresql:42.3.8")
        implementation("org.liquibase:liquibase-core:4.18.0")
        implementation("org.hibernate:hibernate-core:5.6.7.Final")

        // Миграции ликвибейза
        liquibaseRuntime("org.springframework.data:spring-data-jpa:$springVersion")
        liquibaseRuntime("org.liquibase.ext:liquibase-hibernate5:4.8.0")
        liquibaseRuntime("org.springframework.boot:spring-boot:$springVersion")
        liquibaseRuntime(sourceSets.getByName("main").compileClasspath)
        liquibaseRuntime(sourceSets.getByName("main").output)
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

//    val properties = File(rootDir, "src/main/resources/application.yml").inputStream().use {
//        Properties().apply { load(it) }
//    }
//    val propNameSomething = properties.getValue("prop.name.something") as String

    /*
    Таска на генерацию следующего changeSet'а.

    Перед запуском нужно, чтобы БД находилась
    в актуальном состоянии (все старые changeSet'ы были применены)

    Как запустить:
        1) Через плагин в идее: zveron -> Tasks -> liquibase -> createNextChangeSet
        2) Через консоль: ./gradlew createNextChangeSet

    Опциональные параметры:
        *) migrationName - название нового changeSet'а (без цифры)
        *) url - url для подключения к БД
        *) username - пользователь для подключения к БД
        *) password - пароль для подключения к БД
    Дефолтные значения параметров лежат в gradle.properties

    Пример запуска:
        ./gradlew -PmigrationName=new_cool_change_set createNextChangeSet

    P.S. - полученный скрипт по-прежнему нужно редактировать
     */
    tasks.register("createNextChangeSet") {
        group = "liquibase"

        val migrationName: String? by project
        val username: String? by project
        val password: String? by project

        dependsOn("build")
        dependsOn("clean")
        dependsOn("diffChangeLog")
        tasks.findByName("build")!!.mustRunAfter("clean")
        tasks.findByName("diffChangeLog")!!.mustRunAfter("build")

        val changeSetName = migrationName ?: project.properties["changeSetDefaultName"]
        val changeSetUrl =
            "jdbc:postgresql://localhost:5555/" + project.properties["db"] // https://stackoverflow.com/questions/37748721/how-to-use-spring-properties-in-gradle-build
        val changeSetUsername = username ?: project.properties["dbUser"]
        val changeSetPassword = password ?: project.properties["dbPassword"]
        val changeSetDriver = project.properties["dbDriver"]
        val changeSetsDirectory = project.properties["changeLogDirectory"]
        val changeSetReferenceUrl = project.properties["referenceUrl"]
        val changeSetNumber = getNextChangeSetNumber(changeSetsDirectory.toString())

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

// Когда будет много методов для грейдла, можно будет сделать отдельный класс и покрыть его тестами
fun getNextChangeSetNumber(path: String): String {
    val result = (
            File(path)
                .listFiles()
                ?.mapNotNull { file -> file.name.split('_').firstOrNull()?.toIntOrNull() }
                ?.maxOrNull() ?: 0
            ) + 1

    return result.toString().padStart(2, '0')
}