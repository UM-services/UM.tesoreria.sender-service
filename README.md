# UM.tesoreria.sender-service

[![Java](https://img.shields.io/badge/Java-25-red.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.4-blue.svg)](https://spring.io/projects/spring-cloud)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.1.1-lightblue.svg)](https://www.openapis.org/)
[![Guava](https://img.shields.io/badge/Guava-33.7.2--jre-orange.svg)](https://github.com/google/guava)
[![OpenPDF](https://img.shields.io/badge/OpenPDF-3.0.5-yellow.svg)](https://github.com/LibrePDF/OpenPDF)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20.svg?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Caffeine](https://img.shields.io/badge/Caffeine-grey.svg)](https://github.com/ben-manes/caffeine)
[![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JUnit 6](https://img.shields.io/badge/JUnit%206-6.0.3-25A162.svg?logo=junit5&logoColor=white)](https://junit.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![Version](https://img.shields.io/badge/Version-2.6.1-success.svg)](https://github.com/UM-services/UM.tesoreria.sender-service/releases)

Servicio encargado del envío de recibos y formularios de pago para la Tesorería de la Universidad de Mendoza.

## Características

- Generación y envío de recibos de pago (asíncrono)
- Generación de formularios de pago (RapiPago/MercadoPago)
- Validación de emails antes del envío (slice `datos/validacionEmail`, `POST /mailvalidate` de core)
- Soporte para múltiples tipos de chequeras
- Generación y envío de chequeras en lote (bulk)
- Integración con servicios de email
- Generación de PDFs con logos institucionales
- Sistema de deduplicación de mensajes con UUID tracking
- Manejo de inscripciones y pagos
- Soporte para múltiples destinatarios de email (personal, institucional, copias)
- Validación y procesamiento de emails de pago
- Validación local de direcciones de email
- Procesamiento asíncrono de chequeras y cuotas vía Kafka (`send-chequera`, `send-cuota`)
- Envío de notificaciones genéricas por HTTP (`POST /api/tesoreria/sender/correo/enviar`) y por el topic `send-correo`, con idempotencia por `eventId` y resultado en `notificacion-resultado`
- Creación al arrancar de los topics propios (`notificacion-resultado`) y de los DLT (`send-chequera.DLT`, `send-cuota.DLT`, `send-correo.DLT`) con particiones y replicación configurables
- Reintentos con backoff y derivación al DLT del topic cuando falla el procesamiento de un evento de Kafka
- Remitente lógico (`CHEQUERA`, `RECIBO`, `NOTIFICACION`) resuelto por el servicio a partir de `app.mail.*`

## Requisitos

- Java 25
- Maven 3.9+
- Docker (opcional)

## Configuración

El servicio requiere las siguientes variables de entorno:

```properties
# Configuración de la aplicación
spring.application.name=um.tesoreria.sender-service
server.port=8080

# Configuración de email
app.mail.fromchequera=${MAIL_FROM_CHEQUERA}
app.mail.fromrecibo=${MAIL_FROM_RECIBO}
app.mail.fromnotificacion=${MAIL_FROM_NOTIFICACION}
spring.mail.host=${MAIL_HOST}
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Configuración de servicios
um.tesoreria.core-service.url=http://core-service:8080

# Configuración de Kafka (bootstrap.yml)
spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
spring.kafka.consumer.group-id=tesoreria-sender-group
spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.admin.fail-fast=true
spring.kafka.admin.modify-topic-configs=true

# Topics propios del servicio (KafkaTopicsConfiguration)
app.kafka.topics.partitions=${APP_KAFKA_TOPIC_PARTITIONS:1}
app.kafka.topics.replicas=${APP_KAFKA_TOPIC_REPLICAS:1}
```

El consumidor arranca con `auto-offset-reset: latest`: si el grupo no tiene offsets (nuevo o
expirados) no se reenvían correos históricos. Los tests lo sobreescriben a `earliest` en
`src/test/resources/bootstrap.yml` para no perder el evento publicado al arrancar el contexto.

El servicio declara los topics de los que es dueño y los crea al arrancar con el `KafkaAdmin`
auto-configurado: `notificacion-resultado` y los DLT `send-chequera.DLT`, `send-cuota.DLT` y
`send-correo.DLT` (retención de 14 días). Los topics principales (`send-chequera`, `send-cuota`,
`send-correo`) los declara `tesoreria-core`. `app.kafka.topics.partitions` y
`app.kafka.topics.replicas` deben coincidir con las particiones del topic origen de cada DLT,
porque el `DeadLetterPublishingRecoverer` publica en la misma partición del registro original.
Con `spring.kafka.admin.fail-fast: true` el servicio no arranca si no puede crear o actualizar
sus topics.

Si el procesamiento de un evento falla, la excepción se propaga al `DefaultErrorHandler` de la
factory del listener: el registro se reintenta con backoff y, agotados los intentos, se publica
en el DLT de su topic (`send-chequera.DLT`, `send-cuota.DLT`, `send-correo.DLT`).

El descubrimiento por Consul usa `spring.cloud.consul.discovery.query-passing: true`: los clientes
Feign/LoadBalancer sólo resuelven instancias cuyos health checks están en `passing`.

## Construcción

```bash
# Construir con Maven
mvn clean package

# Construir con Docker
docker build -t um.tesoreria.sender-service .
```

## Ejecución

```bash
# Ejecutar con Java
java -jar target/um.tesoreria.sender-service.jar

# Ejecutar con Docker
docker run -p 8080:8080 um.tesoreria.sender-service
```

## Tests

### Unitarios (Surefire)

```bash
mvn test
```

Incluye tests de casos de uso con Mockito, serialización de DTOs y las reglas de arquitectura
(ArchUnit). No levantan infraestructura externa.

### Integración (Failsafe)

```bash
mvn verify
```

Corre los `*IT` con infraestructura en proceso (sin Docker ni base de datos):

| Componente | Reemplazo en tests |
|---|---|
| `tesoreria-core-service` / `mercadopago` | **WireMock** (override de `spring.cloud.openfeign.client.config.<contextId>.url`) |
| SMTP | **GreenMail** (servidor SMTP en memoria) |
| Kafka | **`@EmbeddedKafka`** (topics `send-chequera`, `send-cuota`, `send-correo`, `notificacion-resultado`) |
| PDF/QR | directorio temporal + `PdfReader`/`PdfTextExtractor` de OpenPDF |

Cobertura funcional:

- `ChequeraIntegracionIT`: envío completo (PDF, adjunto, logo e imágenes inline, `mark/sent`,
  `ChequeraMessageCheck`), sin cuotas pendientes y descarga de PDF por HTTP.
- `ReciboIntegracionIT`: PDF ORIGINAL + DUPLICADO con QR AFIP, envío asíncrono, round-trip del PUT
  de facturación (conserva campos no modificados) y reintentos sin e-mails.
- `EnlacePagoIntegracionIT`: enlace de MercadoPago en el cuerpo HTML y caso sin e-mails.
- `KafkaChequeraIntegracionIT`: evento publicado al topic **sin `__TypeId__`** (contrato JSON,
  tipo target) y un caso legacy con el header que emitía core; ambos disparan el envío.
- `CuotaIntegracionIT`: recordatorio de cuota por el topic `send-cuota`, con una copia por
  destinatario en GreenMail (personal e institucional) y un único `Message-ID` por envío.
- `NotificacionIntegracionIT`: consumo de `send-correo`, idempotencia por `eventId`, publicación
  del resultado en `notificacion-resultado` y envío directo por `POST /correo/enviar`.

Para correr un IT puntual:

```bash
mvn -Dit.test=ReciboIntegracionIT verify
```

## Documentación

La documentación de la API está disponible en:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

### Diagramas de Arquitectura

Los diagramas del proyecto se encuentran en `docs/diagrams/` e incluyen:
- **Arquitectura General** — Componentes del servicio y sus interacciones
- **Diagrama de Secuencia: Envío de Recibo** — Flujo de envío de recibos
- **Diagrama de Secuencia: Procesamiento de Chequera** — Flujo de procesamiento por Kafka
- **Diagrama de Secuencia: Chequera en Lote** — Flujo de generación y envío bulk de chequeras
- **Diagrama de Secuencia: Notificación** — Correo genérico por HTTP y por el topic `send-correo`
- **Diagrama de Despliegue** — Infraestructura y sistemas externos
- **Modelo Entidad-Relación** — Entidades principales del dominio

Estos diagramas se renderizan automáticamente en la documentación generada mediante GitHub Pages y Mermaid.

## Estructura del Proyecto

El servicio sigue **Arquitectura Hexagonal con Vertical Slicing estricto**: un paquete por
capacidad de negocio, con `domain/` puro, `application/` como orquestación y `infrastructure/`
como único lugar con framework.

```
src/
├── main/java/um/tesoreria/sender/
│   ├── SenderApplication.java
│   ├── configuration/                     # Feign + reports.properties + Kafka producer/topics
│   └── hexagonal/
│       ├── shared/                        # Kernel compartido
│       │   ├── domain/model/              # POJOs Lombok (sin frameworks)
│       │   ├── domain/ports/out/          # SenderSettings, DireccionCorreoValidador
│       │   └── infrastructure/            # DTOs de core, mappers, JavaMail local, Ping
│       ├── datos/                         # Adaptadores contra otros servicios
│       │   ├── chequera/                  # ports + ChequeraDatosService + Feign adapters
│       │   ├── facturacion/
│       │   ├── persona/
│       │   ├── catalogo/
│       │   ├── preferenciaPago/
│       │   └── validacionEmail/
│       ├── gateways/
│       │   └── correo/                    # Puerto/adaptador único de envío SMTP
│       └── procesos/                      # Casos de uso de negocio
│           ├── chequera/                  # PDFs + envío + consumidores Kafka (send-chequera, send-cuota)
│           ├── recibo/                    # PDF AFIP + envío asíncrono
│           ├── enlacePago/                # Enlace MercadoPago por cuota
│           └── notificacion/              # Correo genérico (HTTP + send-correo → notificacion-resultado)
└── test/java/um/tesoreria/sender/
    ├── hexagonal/ArquitecturaHexagonalTest.java
    ├── hexagonal/procesos/…               # Tests de casos de uso (Mockito)
    └── hexagonal/shared/…/DtoSerializationTest.java
```

### Reglas de dependencia

```
infrastructure → application → domain
        todos → shared.domain.model / shared.domain.ports.out
procesos → datos.application.service / gateways.application.service
```

- `domain/` sólo usa `java.*` y Lombok.
- `application/` no importa `infrastructure/`.
- `datos/` no conoce `procesos/` ni `gateways/`; `gateways/` no conoce `datos/` ni `procesos/`.
- `shared/` no conoce ningún slice.
- Estas reglas se verifican automáticamente con **ArchUnit** (`ArquitecturaHexagonalTest`).

Los paths HTTP, los paths de Feign, los correos, los PDFs y la configuración (`path.reports`,
`app.testing`, `app.mail.*`) se mantienen. Los endpoints de envío responden `202 Accepted` y
publican el evento (`send-chequera` / `send-cuota`) en lugar de enviar en el hilo HTTP.


## Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'feat: add some amazing feature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto es privado y confidencial. Todos los derechos reservados.

## Autor ✒️

* **Daniel Quinteros** - *Desarrollo y Documentación*

## Estado de Integración Continua

[![UM.tesoreria.sender-service CI](https://github.com/UM-services/UM.tesoreria.sender-service/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/UM-services/UM.tesoreria.sender-service/actions/workflows/maven.yml)

## Tecnologías

- Java 25
- Spring Boot 4.1.2
- Spring Cloud 2025.1.4
- Kafka
- Spring Mail
- Spring Cloud Consul Discovery
- OpenAPI (Springdoc) 3.1.1
- Guava 33.7.2-jre
- OpenPDF 3.0.5
- Caffeine (idempotencia de eventos)


```