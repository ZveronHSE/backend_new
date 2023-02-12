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
   application:
      name: name-service
   datasource:
      url: URL
      password: PASSWORD
      username: USER

server:
   port: 0

grpc:
   server: 0 
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

### Как пользоваться pgAdmin

1. Переходим по url: `http://localhost:5050/browser/`
2. Вводим email `ya@frontender.com` и пароль `123`
3. Добавляем сервер с host name `db`, порт `5432`, юзер `zveron_user`, пароль `zveron_password`

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
Когда добавляем новый сервис, нужно:
1. Создать в новом модуле `Dockerfile` по анаогии с теми, что уже есть
2. Добавить `application-prod.yml`, где нужно указать порты для сервера и переопределить адрес registry
3. Добавить сервис в `docker-compose.yml`
4. Добавить шаг создания образа в `.github/workflows/builder.yml`
### Deploy
_In progress..._