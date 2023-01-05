# Zveron Backend
## Корневой модуль:
1. Если нужно создавать новую БД под каждый сервис, то по пути `db/create_database.sql` нужно добавить новый скрипт
   аналогичный другим скриптам, но со своим названием БД.
2. Стоит понимать, что в `gradle.properties` записываются все переменные(как правило это версии), где будут
   переиспользованы несколько раз в разных местах
3. В `build.gradle.kts` добавляем только те зависимости или задачи, которые пригодятся всем проектам, иначе используем
   уже во вложенных модулях `build.gradle.kts`

## Модули сервисов:

- В каждом модуле находится свой `build.gradle.kts` для того, чтобы добавлять собственные зависимости, которые не нужно
  использовать в других модулях
- Если нужен необходимый какой-либо контракт, то в репозитории https://github.com/ZveronHSE/contract создаем ветку свою,
  где мы создаем модуль и публикуем контракт по примеру, потом свой тег на эту ветку. Добавляем зависимость по примеру:

```kotlin
  implementation("com.github.ZveronHse:contract:NAME_MODULE:TAG")
```

- Обязательно иметь `application.yml`, где внутри будет такая структура:

```yaml
spring:
  datasource:
    url: URL
    password: PASSWORD
    username: USER
```

Поскольку это влияет на то, чтобы успешно подключаться к собственным БД и генерировать миграции при необходимости.

## LiquiBase или как генерировать миграции

1. Перед запуском нужно, чтобы БД находилась в актуальном состоянии (все старые changeSet(миграции) были применены), они
   находятся по папке: `src/main/resources/db/changelog`. Также чтобы ваши изменения были отражены в коде
   (entity, переименование полей и т.д.)
2. Как сгенерировать миграции:
    - Через плагин Gradle в iDEA в нужном модуле: `Tasks -> liquibase -> createNextChangeSet`
    - Через консоль: `./gradlew createNextChangeSet`

   Опциональные параметры:
    - **migrationName** - название нового changeSet'а (без цифры)
      Дефолтные значения параметров лежат в gradle.properties

   Пример запуска:
   `./gradlew -PmigrationName=new_cool_change_set createNextChangeSet`

## CI / CD / Deploy
### Continuous integration
1. Пайплайны запускаются при создании PR. `Test affected modules` подготавливает тестовую среду и запускает 
тесты в тех модулях, файлы которых были изменены.
2. **Поэтому**, когда вы создаете новый модуль, необходимо в `.github/workflows/tester.yml` добавить обработку 
изменений этого модуля. Пример:
```yaml
      - name: Test <название сервиса>
        shell: bash
        run: if grep -q "<название сервиса>" "temp.txt"; then gradle :<название сервиса>:test; fi
```
### Continuous delivery
_In progress..._
### Deploy
_In progress..._

## CI / CD / Deploy
### Continuous integration
1. Пайплайны запускаются при создании PR. `Test affected modules` подготавливает тестовую среду и запускает 
тесты в тех модулях, файлы которых были изменены.
2. **Поэтому**, когда вы создаете новый модуль, необходимо в `.github/workflows/tester.yml` добавить обработку 
изменений этого модуля. Пример:
```yaml
      - name: Test <название нового модуля>
        if: contains(steps.changed-files.outputs.modified_files, '<директория нового модуля>')
        run: gradle :<директория нового модуля>:test
```
### Continuous delivery
_In progress..._
### Deploy
_In progress..._