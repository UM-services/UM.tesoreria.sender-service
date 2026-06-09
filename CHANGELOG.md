# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [2.0.0] - 2026-06-09

### Added
- Nueva propiedad configurable `app.mail.fromrecibo` para definir el remitente en envíos de recibos ([src/main/java/um/tesoreria/sender/service/ReciboService.java], [src/main/resources/bootstrap.yml])
- Validación null-safety con `Objects.requireNonNull()` en MercadoPagoService para evitar NullPointerException ([src/main/java/um/tesoreria/sender/service/MercadoPagoService.java])

### Changed
- Refactorizado MercadoPagoService usando `@RequiredArgsConstructor`, eliminando el constructor explícito ([src/main/java/um/tesoreria/sender/service/MercadoPagoService.java])
- Reemplazado `addresses.toArray(new String[addresses.size()])` por `addresses.toArray(new String[0])` en MercadoPagoService ([src/main/java/um/tesoreria/sender/service/MercadoPagoService.java])
- Actualizado diagrama de secuencia de envío de recibos con participantes detallados (ReciboController, FacturacionElectronicaClient, JavaMailSender, MimeMessageHelper, ReciboMessageCheckClient) y bloques activate/deactivate ([docs/diagrams/flujo-envio-recibo.mmd])

### Fixed
- Alineada configuración de test `src/test/resources/bootstrap.yml` con el nuevo esquema `app.mail.fromchequera` / `app.mail.fromrecibo`, reemplazando la propiedad antigua `app.mail.from` ([src/test/resources/bootstrap.yml])

### Removed
- Eliminada propiedad `app.mail.from` — reemplazada por `app.mail.fromchequera` y `app.mail.fromrecibo` ([src/main/java/um/tesoreria/sender/service/ChequeraService.java], [src/main/resources/bootstrap.yml])

## [1.9.0] - 2026-06-09

### Added
- Nueva propiedad configurable `app.mail.from` para definir el remitente en envíos de chequeras ([src/main/java/um/tesoreria/sender/service/ChequeraService.java], [src/main/resources/bootstrap.yml])
- Configuración externalizada para `spring.mail.host` mediante `${app.mail.host}` ([src/main/resources/bootstrap.yml])

### Changed
- Actualizado Spring Boot a versión 4.0.6 (desde 4.0.5) ([pom.xml])

## [1.8.0] - 2026-04-04

### Changed
- Actualizado Spring Boot a versión 4.0.5 (desde 4.0.2) ([pom.xml])
- Actualizado Kotlin a versión 2.3.20 (desde 2.3.0) ([pom.xml])
- Actualizado Springdoc OpenAPI a versión 3.0.2 (desde 3.0.1) ([pom.xml])
- Actualizado OpenPDF a versión 3.0.3 (desde 3.0.1) ([pom.xml])
- Actualizado GitHub Actions: checkout@v6, setup-java@v5, cache@v5, upload-pages-artifact@v4, deploy-pages@v5, docker/login-action@v4, docker/metadata-action@v6, docker/setup-buildx-action@v4, docker/build-push-action@v7 ([.github/workflows/])
- Actualizado JDK a versión 25 (desde 24) ([.github/workflows/maven.yml], [.github/workflows/generate-docs.yml])

### Added
- Nueva dependencia commons-fileupload 1.6.0 ([pom.xml])

### Fixed
- Eliminado import no utilizado (HttpStatus) en ChequeraController.java ([src/main/java/um/tesoreria/sender/controller/ChequeraController.java])

## [1.7.0] - 2026-03-14

### Added
- feat: Nuevos campos de beca en ChequeraSerieDto (hpum, becaPorcentaje, becaResolucion, becaFecha, becaUserId) ([src/main/java/um/tesoreria/sender/kotlin/dto/tesoreria/core/ChequeraSerieDto.kt])

### Changed
- Actualizado Kafka consumer:迁移 de JsonDeserializer a JacksonJsonDeserializer para mejor rendimiento ([src/main/java/um/tesoreria/sender/configuration/KafkaConsumerConfig.java])
- Migración de FacturacionElectronicaDto de Kotlin a Java para mejor interoperabilidad ([src/main/java/um/tesoreria/sender/domain/dto/tesoreria/core/FacturacionElectronicaDto.java])
- Refactorizado ReciboController usando @RequiredArgsConstructor y ResponseEntityBuilder ([src/main/java/um/tesoreria/sender/controller/ReciboController.java])
- Refactorizado logging en ReciboService usando Jsonifier para mejor legibilidad y rendimiento ([src/main/java/um/tesoreria/sender/service/ReciboService.java])
- Mejorado null safety en ReciboService con Objects.requireNonNull ([src/main/java/um/tesoreria/sender/service/ReciboService.java])
- Añadido método jsonify() a DTOs (ChequeraFacturacionElectronicaDto, ChequeraPagoDto, ReciboMessageCheckDto) para serialización JSON uniforme

## [1.6.0] - 2026-03-13

### Added
- Nuevo DTO GoogleMailTestRequest para testing de emails ([src/main/java/um/tesoreria/sender/domain/dto/GoogleMailTestRequest.java])

### Changed
- Actualizado OpenPDF a versión 3.0.1 (desde 3.0.0) ([pom.xml])
- Simplificado import de Spring Web en ChequeraController usando wildcard ([src/main/java/um/tesoreria/sender/controller/ChequeraController.java])
- Mejorado logging debug en FormulariosToPdfService para depuración de preferences ([src/main/java/um/tesoreria/sender/service/FormulariosToPdfService.java])

## [1.5.0] - 2026-02-04

### Changed
- Actualizado Spring Boot a versión 4.0.2 (desde 3.5.8) ([pom.xml])
- Actualizado Java a versión 25 (desde 24) ([pom.xml], [.github/workflows/maven.yml], [Dockerfile])
- Actualizado Kotlin a versión 2.3.0 (desde 2.2.21) ([pom.xml])
- Actualizado Spring Cloud a versión 2025.1.0 (desde 2025.0.0) ([pom.xml])
- Actualizado Springdoc OpenAPI a versión 3.0.1 (desde 2.8.10) ([pom.xml])
- Actualizado Guava a versión 33.5.0-jre (desde 33.4.8-jre) ([pom.xml])
- Actualizado ZXing Core a versión 3.5.4 (desde 3.5.3) ([pom.xml])
- Actualizado Commons Lang3 a versión 3.20.0 (desde 3.18.0) ([pom.xml])
- Mejorada configuración de Jackson ObjectMapper en SenderConfiguration para registar módulos automáticamente ([src/main/java/um/tesoreria/sender/configuration/SenderConfiguration.java])
- Corregido tipo de datos en ChequeraMessageCheckDto: cambiado de Integer a Int para mejor compatibilidad Kotlin ([src/main/java/um/tesoreria/sender/kotlin/dto/tesoreria/core/ChequeraMessageCheckDto.kt])

## [1.4.0] - 2025-12-14

### Added
- feat: Integration of Spring Kafka for asynchronous Chequera processing.
- feat: New `ChequeraEventListener` and `KafkaConsumerConfig`.

## [1.1.0] - 2025-08-10

### Changed
- Actualizado Spring Boot a versión 3.5.4 (desde 3.5.3) ([pom.xml])
- Añadida configuración de health check para mail en `bootstrap.yml`
- Mejorada seguridad en Dockerfile: permisos explícitos para el usuario de la app

### Fixed
- Correcciones menores de configuración y permisos para despliegue

---

## [1.3.0] - 2025-11-16

### Changed
- Refactorizado el servicio de generación de PDFs (FormulariosToPdfService) para mejorar la modularidad y mantenibilidad.
- Extraída lógica común de generación de PDFs en métodos privados (`createDocument`, `addHeader`, `addCommonHeaderInfo`, `addCuotaTable`, `addCuotaTableReemplazo`, `createCuotaTableStructure`, `createVencimientoParagraph`, `createImporteParagraph`, `addMercadoPagoLink`, `addBarcode`, `fetchData`).
- Mejorado el manejo de errores en la generación de PDFs.
- Añadida verificación de `cuota.getCompensada() == 0` en la lógica de impresión de cuotas.

## [1.2.0] - 2025-11-15

### Added
- Nuevo Jsonifier utility class para serialización JSON simplificada ([src/main/java/um/tesoreria/sender/util/Jsonifier.java])

### Changed
- Actualizado Spring Boot a versión 3.5.6 (desde 3.5.4) ([pom.xml])
- Actualizado Kotlin a versión 2.2.21 (desde 2.2.0) ([pom.xml])
- Actualizado Springdoc OpenAPI a versión 2.8.10 (desde 2.8.9) ([pom.xml])
- Migrado de iText a OpenPDF versión 3.0.0 para mejor compatibilidad y rendimiento ([pom.xml])
- Refactorizados Feign clients con contextId para mejor configuración ([src/main/java/um/tesoreria/sender/client/tesoreria/core/])
- Refactorizados servicios con @RequiredArgsConstructor para inyección de dependencias ([src/main/java/um/tesoreria/sender/service/])
- Mejorada lógica de envío de emails en ChequeraService con métodos separados y validación mejorada ([src/main/java/um/tesoreria/sender/service/ChequeraService.java])
- Actualizado diagrama de flujo de envío de recibos ([docs/diagrams/flujo-envio-recibo.mmd])

### Fixed
- Corregida serialización JSON en logging de servicios ([src/main/java/um/tesoreria/sender/service/])

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