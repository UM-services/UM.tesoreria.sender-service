# UM.tesoreria.sender-service

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-blue.svg)](https://spring.io/projects/spring-cloud)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple.svg)](https://kotlinlang.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-2.8.8-lightblue.svg)](https://www.openapis.org/)
[![Guava](https://img.shields.io/badge/Guava-33.4.7-jre-orange.svg)](https://github.com/google/guava)
[![OpenPDF](https://img.shields.io/badge/OpenPDF-2.0.4-yellow.svg)](https://github.com/LibrePDF/OpenPDF)

Servicio encargado del envío de recibos y formularios de pago para la Tesorería de la Universidad de Mendoza.

## Características

- Generación y envío de recibos de pago
- Generación de formularios de pago (RapiPago/MercadoPago)
- Validación de emails antes del envío
- Soporte para múltiples tipos de chequeras
- Integración con servicios de email
- Generación de PDFs con logos institucionales

## Requisitos

- Java 21
- Maven 3.9+
- Docker (opcional)

## Configuración

El servicio requiere las siguientes variables de entorno:

```properties
# Configuración de la aplicación
spring.application.name=um.tesoreria.sender-service
server.port=8080

# Configuración de email
spring.mail.host=smtp.um.edu.ar
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Configuración de servicios
um.tesoreria.core-service.url=http://core-service:8080
um.tesoreria.tool-service.url=http://tool-service:8080
```

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
java -jar target/sender-service.jar

# Ejecutar con Docker
docker run -p 8080:8080 um.tesoreria.sender-service
```

## Documentación

La documentación de la API está disponible en:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── um/
│   │       └── tesoreria/
│   │           └── sender/
│   │               ├── configuration/    # Configuraciones
│   │               ├── controller/       # Controladores REST
│   │               ├── dto/             # Objetos de transferencia
│   │               ├── exception/        # Manejo de excepciones
│   │               ├── kotlin/          # Código Kotlin
│   │               ├── service/          # Lógica de negocio
│   │               └── util/             # Utilidades
│   └── resources/
│       └── application.yml              # Configuración
└── test/                               # Pruebas
```

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

## Descripción

Servicio de envío de correos electrónicos y notificaciones para UM Tesorería. Maneja el envío de chequeras, comprobantes y notificaciones transaccionales mediante RabbitMQ.

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