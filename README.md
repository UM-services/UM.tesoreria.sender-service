# UM.tesoreria.sender-service

## Estado de Integración Continua

[![UM.tesoreria.sender-service CI](https://github.com/UM-services/UM.tesoreria.sender-service/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/UM-services/UM.tesoreria.sender-service/actions/workflows/maven.yml)

## Descripción

Servicio de envío de correos electrónicos y notificaciones para UM Tesorería. Maneja el envío de chequeras, comprobantes y notificaciones transaccionales mediante RabbitMQ.

## Características

- Envío de correos electrónicos con chequeras y comprobantes
- Procesamiento asíncrono de mensajes mediante RabbitMQ
- Gestión transaccional de mensajes
- Manejo optimizado de recursos (CPU/RAM)

## Tecnologías

- Java 21
- Spring Boot 3.4.2
- Spring Cloud
- RabbitMQ
- Spring Mail

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
```

## Documentación

- [Documentación del Proyecto](https://um-services.github.io/UM.tesoreria.sender-service)
- [Wiki](https://github.com/UM-services/UM.tesoreria.sender-service/wiki)
- [Spring Mail](https://docs.spring.io/spring-framework/reference/integration/email.html)
- [RabbitMQ](https://www.rabbitmq.com/documentation.html)

## Estado del Proyecto

Para ver el estado actual del proyecto, issues y milestones, visita la [página de documentación](https://um-services.github.io/UM.tesoreria.sender-service/project-documentation.html).
