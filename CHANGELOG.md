# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Implementación de sistema de deduplicación de mensajes con UUID tracking
- Optimización de consumo de recursos (CPU/RAM)
- Configuración de prefetch count y concurrencia para RabbitMQ
- Manejo transaccional de mensajes
- Message deduplication system for chequera and recibo processing
- New `ChequeraMessageCheckClient` and `ReciboMessageCheckClient` for message tracking
- UUID-based message tracking in DTOs
- Manual acknowledgment mode for RabbitMQ consumers
- Enhanced logging for message processing

### Changed
- Actualizado Spring Boot a versión 3.4.4
- Actualizado Kotlin a versión 2.1.20
- Actualizado Spring Cloud a versión 2024.0.1
- Actualizado Guava a versión 33.4.7-jre
- Actualizado Springdoc OpenAPI a versión 2.8.6
- Mejorado el manejo de errores en el procesamiento de mensajes
- Optimizado el consumo de memoria en el procesamiento de mensajes
- Improved error handling in message consumers
- Enhanced email address handling with testing mode

### Removed
- Eliminado RabbitMQConfig.java
- Eliminado ChequeraConsumer.java
- Eliminado ReciboConsumer.java
- Eliminado ChequeraMessageDto.kt
- Eliminado ReciboMessageDto.kt
- Eliminada configuración redundante de RabbitMQ
- Removed tester queue and related functionality
- Removed redundant logging statements

### Fixed
- Corregido problema de alta utilización de CPU
- Corregido problema de alta utilización de RAM
- Mejorado el manejo de transacciones para evitar pérdida de mensajes
- Optimizado el procesamiento de mensajes para mejor rendimiento
- Fixed potential null pointer issues in message processing
- Fixed email address handling in testing mode

### Security
- Added UUID-based message tracking for better security
- Improved error handling to prevent message loss

## [Previous Versions]

[Previous version history will be added here as new versions are released] 