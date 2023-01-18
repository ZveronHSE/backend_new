# ApiGateway

Запросы к эндпоинту выполняются в формате 

```json
{
    "method_alias": "blacklist-service:BlacklistService:AddToBlacklist",
    "request_body": "ewogICAgIm93bmVyX2lkIjogMTIzLAogICAgInRhcmdldF91c2VyX2lkIjogMTIzNAp9"
}
```
Где `method_alias` состоит из `название-сервиса:название-грпц-сервиса:название-грпц-метода`

Тело запроса это сконверченный джейсон. Чтобы сконвертить тело запроса, можно воспользоваться ссылкой https://codebeautify.org/json-to-base64-converter


Для того, чтобы апигв смог извлечь информацию по сервису, в метод которого будет стучаться, в `properties.yml` нужно указать путь до прото файла для сервиса
Например путь до `BlacklistService` это просто название файла `crud.proto`

```yaml
eureka:
  instance:
    metadata-map:
      BlacklistService: 'crud.proto'
```