Пока не введу секьюерность (а я пока не собираюсь, все мы помним как с ней больно тестировать приложение)
Можно просто пускать сервис на локал хосте

По ссылке 
`
http://localhost:8761 
`
будет доступен интерфейс эврики 

Можно так же бахнуть еще запрос в эндпоинт `http://localhost:8761/eureka/apps` чтобы понимать, какую инфу по сервису отдает регистр


Так как умные китайцы уже все синтегрировали для нас, чтобы пользоваться регистром нужно всего лишь...

## Со стороны сервиса из которого делаем вызов 

- Во входной точке приложения добавляем `@EnableEurekaClient`
```kt
@EnableEurekaClient
@SpringBootApplication
class ApplicationToCallFrom

fun main(args: Array<String>) {
    runApplication<ApplicationToCallFrom>(*args)
}
```

- в `properties.yml`
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

grpc:
  client:
    grpc-whatever-service:
      address: 'discovery:///whatever-service'
      enableKeepAlive: true # пока не оч ясно будет ли влиять я бы посмотрел
      keepAliveWithoutCalls: true # то же самое
      negotiationType: plaintext # пока что так, потом добавим всякие сертификаты
```

Далее для вызова клиента 

```kt
@Service
class CallerService {
    @GrpcClient("whatever-service") 
    lateinit var service: WhateverServiceGrpcKt.WhateverServiceCoroutineStub 
    
    suspend fun get() = service.get() 
}
```

## Со стороны сервиса в который стучимся / который нужно зарегистрировать в регистре
- добавить в `properties.yml` 
```yaml
eureka:
  client:
    register-with-eureka: true
    tls:
      enabled: false
    webclient:
      enabled: true
```