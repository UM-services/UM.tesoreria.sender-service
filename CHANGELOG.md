# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.0] - 2025-08-10

### Changed
- Actualizado Spring Boot a versión 3.5.4 (desde 3.5.3) ([pom.xml])
- Añadida configuración de health check para mail en `bootstrap.yml`
- Mejorada seguridad en Dockerfile: permisos explícitos para el usuario de la app

### Fixed
- Correcciones menores de configuración y permisos para despliegue

---

## [Unreleased]

## [1.0.0] - 2025-07-20

### Added
- Implementación de sistema de deduplicación de mensajes con UUID tracking
- Sistema completo de validación de emails usando ToolClient
- Nuevo `Tool` utility class para conversión de strings a listas
- Nuevo manejo robusto de errores en envío de emails
- Nuevo logo marca_um_65.png para formularios
- Campo emailCopia en TipoChequeraDto
- Endpoint mejorado en PersonaClient para información completa de inscripción
- Nuevos DTOs para manejo de inscripciones:
  - InscripcionFullDto
  - InscripcionDto
  - InscripcionPagoDto
- Sistema de tracking de mensajes con UUID para chequeras y recibos
- Nuevos clientes para seguimiento de mensajes:
  - ChequeraMessageCheckClient
  - ReciboMessageCheckClient
- Modo de reconocimiento manual para consumidores RabbitMQ
- Logging mejorado para procesamiento de mensajes
- Integración completa con ToolClient para validación de emails

### Changed
- Actualizado Spring Boot a versión 3.5.3 (desde 3.4.5)
- Actualizado Java a versión 24 (desde 21)
- Actualizado Kotlin a versión 2.2.0 (desde 2.1.20)
- Actualizado Spring Cloud a versión 2025.0.0 (desde 2024.0.1)
- Actualizado OpenPDF a versión 2.2.4 (desde 2.0.4)
- Actualizado Springdoc OpenAPI a versión 2.8.9 (desde 2.8.8)
- Mejorado el manejo de errores en procesamiento de mensajes
- Optimizado el consumo de memoria y CPU
- Mejorada la lógica de envío de emails para validación previa
- Optimizado el orden de envío de emails en ReciboService
- Actualizada la lógica de impresión en formularios
- Actualizado el manejo de logos institucionales
- Integrada validación completa de emails en ChequeraService

### Fixed
- Error handling en envío de emails con direcciones inválidas
- Manejo de memoria en procesamiento de mensajes
- Validación de emails antes del envío
- Orden de procesamiento de mensajes en RabbitMQ
- Mejorado el proceso de validación de emails con validación previa al envío

### Removed
- Eliminado RabbitMQConfig.java
- Eliminado ChequeraConsumer.java
- Eliminado ReciboConsumer.java
- Eliminado ChequeraMessageDto.kt
- Eliminado ReciboMessageDto.kt
- Eliminada configuración redundante de RabbitMQ
- Removed tester queue and related functionality
- Removed redundant logging statements
- Eliminados archivos no utilizados:
  - ReciboConsumer.java
  - RabbitMQConfig.java
  - TestController.java
  - DeadLetterQueueConsumer.java
  - ChequeraConsumer.java

### Fixed
- Corregido problema de alta utilización de CPU
- Corregido problema de alta utilización de RAM
- Mejorado el manejo de transacciones para evitar pérdida de mensajes
- Optimizado el procesamiento de mensajes para mejor rendimiento
- Fixed potential null pointer issues in message processing
- Fixed email address handling in testing mode
- Corregido el manejo de errores en el envío de emails moviendo el envío antes del bloque catch

### Security
- Added UUID-based message tracking for better security
- Improved error handling to prevent message loss

## [Previous Versions]

[Previous version history will be added here as new versions are released] 