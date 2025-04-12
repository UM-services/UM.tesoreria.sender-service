# UM.tesoreria.sender-service

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-blue.svg)](https://spring.io/projects/spring-cloud)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple.svg)](https://kotlinlang.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-2.8.6-lightblue.svg)](https://www.openapis.org/)
[![Guava](https://img.shields.io/badge/Guava-33.4.7-jre-orange.svg)](https://github.com/google/guava)

## Autor ✒️

* **Daniel Quinteros** - *Desarrollo y Documentación*

## Estado de Integración Continua

[![UM.tesoreria.sender-service CI](https://github.com/UM-services/UM.tesoreria.sender-service/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/UM-services/UM.tesoreria.sender-service/actions/workflows/maven.yml)

## Descripción

Servicio de envío de correos electrónicos y notificaciones para UM Tesorería. Maneja el envío de chequeras, comprobantes y notificaciones transaccionales mediante RabbitMQ.

## Características

- Envío de correos electrónicos con chequeras y comprobantes
- Procesamiento asíncrono de mensajes mediante RabbitMQ
- Gestión transaccional de mensajes
- Manejo optimizado de recursos (CPU/RAM)
- Sistema de deduplicación de mensajes
- Soporte para múltiples colas (recibo_queue, chequera_queue)
- Generación automática de documentación y wiki

## Tecnologías

- Java 21
- Spring Boot 3.4.4
- Spring Cloud 2024.0.1
- RabbitMQ
- Spring Mail
- Spring Cloud Netflix Eureka Client
- OpenAPI (Springdoc) 2.8.6
- Kotlin 2.1.20
- Guava 33.4.7-jre

## Configuración

### RabbitMQ

```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: admin
    password: admin
    listener:
      simple:
        prefetch: 5
        concurrency: 1
        max-concurrency: 2
        batch-size: 5
        missing-queues-fatal: false
        default-requeue-rejected: false
    cache:
      channel:
        size: 10
      connection:
        mode: channel
```

### Mail

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${app.mail.username}
    password: ${app.mail.password}
    properties:
      mail.smtp:
        starttls:
          enable: true
          required: true
      auth: true
      connection timeout: 15000
      timeout: 15000
      write timeout: 15000
```

### Testing Mode

```yaml
app:
  testing: false  # Set to true for testing mode
```

## Colas RabbitMQ

- `recibo_queue`: Cola para procesamiento de recibos electrónicos
- `chequera_queue`: Cola para procesamiento de chequeras

## Documentación

- [Documentación del Proyecto](https://um-services.github.io/UM.tesoreria.sender-service)
- [Wiki](https://github.com/UM-services/UM.tesoreria.sender-service/wiki)
- [Spring Mail](https://docs.spring.io/spring-framework/reference/integration/email.html)
- [RabbitMQ](https://www.rabbitmq.com/documentation.html)
- [OpenAPI UI](http://localhost:8080/swagger-ui.html)

## Estado del Proyecto

Para ver el estado actual del proyecto, issues y milestones, visita la [página de documentación](https://um-services.github.io/UM.tesoreria.sender-service/project-documentation.html).

## Changelog

Para ver los cambios detallados en cada versión, consulta el [CHANGELOG.md](CHANGELOG.md).
