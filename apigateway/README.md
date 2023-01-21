# ApiGateway

Запросы к эндпоинту выполняются в формате 

```json
{
    "method_alias": "blAddToBlacklist",
    "request_body": "ewogICAgIm93bmVyX2lkIjogMTIzLAogICAgInRhcmdldF91c2VyX2lkIjogMTIzNAp9"
}
```
Где `method_alias` метчится в бд название грпц сервиса, сервиса и название вызываемого метода

```json
{
  "alias": "blGetBlacklist",
  "serviceName": "blacklist-service",
  "grpcServiceName": "BlacklistService",
  "grpcMethodName": "GetBlacklist"
}
```

Ручки для положить/вытащить временные и сделаны скорее для удобства и потому обычный рест
```http request
/api/v1/method/alias/upsert
{
  "alias": "blGetBlacklist",
  "serviceName": "blacklist-service",
  "grpcServiceName": "BlacklistService",
  "grpcMethodName": "GetBlacklist"
}

/api/v1/method/alias/get
```

Тело запроса это сконверченный джейсон. Чтобы сконвертить тело запроса, можно воспользоваться ссылкой https://codebeautify.org/json-to-base64-converter


Для того, чтобы апигв смог извлечь информацию по сервису, в метод которого будет стучаться, в `properties.yml` нужно указать путь до прото файла для сервиса
Например путь до `BlacklistService` это просто название файла `crud.proto`

```yaml
eureka:
  instance:
    metadata-map:
      BlacklistService: 'crud.proto'
```

Путь проставляется относительно proto файла в директории контрактов, то есть 
```jsonpath
contract
- blacklist
  - src
   - proto
    - crud.proto
```
Переваривается как `crud.proto`

Если добавим папку, например
```jsonpath
contract
- blacklist
  - src
    - proto
      - bl
        - crud.proto
```
```
То будет уже `bl/crud.proto`