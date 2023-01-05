Пока не введу секьюерность (а я пока не собираюсь, все мы помним как с ней больно тестировать приложение)
Можно просто пускать сервис на локал хосте

по ссылке 
`
http://localhost:8761 
`
будет доступен интерфейс эврики 

можно так же бахнуть еще запрос в эндпоинт `http://localhost:8761/eureka/apps` чтобы понимать, какую инфу по сервису отдает регистр


Так как умные китайцы уже все синтегрировали для нас, чтобы пользоваться регистром нужно

- Пометить входную точку приложения `@EnableEurekaClient`
- Добавить в конфигу сервиса, который будет делать вызовы

```yaml
eureka:
  client:
    webclient:
      enabled: true # но нужно своими руками инжектить бин WebClient
    register-with-eureka: true # если сам сервис нужно регать тоже
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
    enabled: true
```

- добавить в проперти себе сервис, в который будем стучаться

```yaml
grpc:
  client:
    you-name-it-whatever:
      address: 'discovery:///whatever-service'
      enableKeepAlive: true # пока не оч ясно будет ли влиять я бы посмотрел
      keepAliveWithoutCalls: true # то же самое
      negotiationType: plaintext # пока что так, потом добавим всякие сертификаты
```

Далее уже со стороны сервиса, который зовем, так же ставим анноташку `@EnabledEurekaClient` и прописываем в пропертях

```yaml
eureka:
  client:
    register-with-eureka: true
    tls:
      enabled: false
```