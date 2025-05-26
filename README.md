# UM.tesoreria.sender-service

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-blue.svg)](https://spring.io/projects/spring-cloud)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple.svg)](https://kotlinlang.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-2.8.8-lightblue.svg)](https://www.openapis.org/)
[![Guava](https://img.shields.io/badge/Guava-33.4.7-jre-orange.svg)](https://github.com/google/guava)
[![OpenPDF](https://img.shields.io/badge/OpenPDF-2.0.4-yellow.svg)](https://github.com/LibrePDF/OpenPDF)

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
- Sistema de deduplicación de mensajes con UUID tracking
- Soporte para múltiples colas (recibo_queue, chequera_queue)
- Generación automática de documentación y wiki
- Validación de direcciones de email antes del envío
- Manejo de casos donde no hay emails válidos para enviar

## Tecnologías

- Java 21
- Spring Boot 3.4.5
- Spring Cloud 2024.0.1
- RabbitMQ
- Spring Mail
- Spring Cloud Netflix Eureka Client
- OpenAPI (Springdoc) 2.8.8
- Kotlin 2.1.20
- Guava 33.4.7-jre
- OpenPDF 2.0.4

## Configuración

### RabbitMQ

```