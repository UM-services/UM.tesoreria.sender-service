# Plan de refactor — tesoreria-sender a Hexagonal + Vertical Slicing estricto

> Estado: **implementado** (2026-10-22). Fases 0–7 completadas; verificación final en fase 8.
> Base: `develop` @ `26c45d3` + working tree con la migración Kotlin→Java de DTOs.
> Referencia de convención: `um.tesoreria.mercadopago-service` (ya migrado: sólo `hexagonal/`,
> `configuration/` y la clase `@SpringBootApplication`).

## 1. Objetivo

Reorganizar `um.tesoreria.sender-service` en slices verticales con arquitectura hexagonal
estricta:

- `domain/` 100% puro (solo `java.*` + Lombok; sin Spring, Jakarta, Jackson, Feign, OpenPDF).
- `application/` importa únicamente `domain/` + anotaciones de Spring.
- `infrastructure/` es el único lugar con framework (Feign, JavaMail, OpenPDF, zXing, Kafka, Web).
- Un slice por capacidad de negocio; nada de capas horizontales globales (`service/`, `client/`,
  `dto/`, `controller/` desaparecen).
- Sin puentes de compatibilidad ni sobrecargas "legacy".

El servicio **no tiene base de datos propia**: sus adaptadores de salida son clientes REST
(Feign), correo SMTP, generación de PDF/QR en filesystem y publicación/consumo Kafka.
Por eso el patrón "repository → JPA" se traduce a "port → Feign/Mail/PDF adapter".

## 2. Estado actual (inventario)

`src/main/java/um/tesoreria/sender` — 67 archivos, ~4.620 LOC:

| Paquete actual | Archivos | Rol |
|---|---|---|
| `controller/` | 4 | HTTP inbound |
| `consumer/` | 1 | Kafka inbound (`send-chequera`) |
| `event/` | 1 | wire event Kafka |
| `service/` | 4 (+3 pdf/util) | lógica de negocio + PDF + mail + HTTP saliente, todo mezclado |
| `client/tesoreria/core` | 16 | Feign a core |
| `client/tesoreria/mercadopago` | 2 | Feign a mercadopago |
| `dto/tesoreria/core` | 23 | contrato HTTP con core (recientemente migrados de Kotlin) |
| `domain/dto` | 7 | DTOs de core + mercadopago + request |
| `configuration/` | 2 | Feign/PropertySource + Kafka |
| `util/` | 2 | Jsonifier |
| `SenderApplication` | 1 | bootstrap |

Procesos de negocio reales (entradas):

1. **Chequera** (`ChequeraController` + `ChequeraEventListener`): PDF de chequera/cuota/reemplazo,
   envío por mail con logo e imágenes inline, marcado de enviada y registro de `ChequeraMessageCheck`.
2. **Recibo** (`ReciboController`): PDF AFIP (QR + CAE, original + duplicado mergeados), envío
   asíncrono, actualización de `enviada`/`retries`, registro de `ReciboMessageCheck`, `sendNext`.
3. **Enlace de pago** (`MercadoPagoController`): mail HTML con link de MercadoPago de una cuota.

### Hallazgos relevantes

- **Código muerto confirmado** (sin referencias): `service/pdf/PdfDocumentBuilder`, `PdfConstants`
  (sólo lo usa el builder), `service/pdf/ChequeraDataProvider`, `client/.../PreferenceClient`,
  `LectivoClient`, `TipoChequeraClient`, `domain/dto/GoogleMailTestRequest`,
  `FormulariosToPdfService.generateMatriculaPdf()` y `ReciboService.sendPendientes()` (sin endpoint).
- **Feign con `name` que incluye path** (`name = "tesoreria-core-service/api/..."`) en 10 clientes;
  conviene normalizar a `name` + `path`, verificando URL resultante.
- **Round-trip obligatorio** en `FacturacionElectronicaClient.update(...)`: se envía el DTO completo;
  si el modelo de dominio no conserva todos los campos se pierden datos en el PUT.
- **BUG real** en `ReciboService.send`: resuelve `chequeraPago` localmente pero luego usa
  `facturacionElectronica.getChequeraPago()` para `findByUnique` (NPE si el DTO viene sin el anidado).
  Además `getRetries()` puede ser `null` → NPE al incrementar.
- `incluyeMatricula` viaja en el evento y en el endpoint pero no se usa.
- `ReciboController.makePdf` no valida PDF vacío → `FileNotFoundException` (500) en lugar de 204.
- `MercadoPagoService` no setea `From`; `ChequeraService.sendCuota` envía sin adjunto (comportamiento actual).
- Consumidores externos ya desalineados (**fuera de alcance, no tocar**): core llama
  `/generatePdf/{fac}/{tipo}/{serie}/{alt}` sin `codigoBarras` y `/sendCuota/{...}/{copiaInformes}`;
  facturador llama `/recibo/send/{id}` (correcto). Kafka: core publica
  `um.tesoreria.core.event.SendChequeraEvent` al topic `send-chequera` (mapeo FQCN existente).

## 3. Estructura objetivo

```
um.tesoreria.sender
├── SenderApplication.java
├── configuration/
│   └── SenderConfiguration.java          # @EnableFeignClients(basePackages = "...sender.hexagonal")
└── hexagonal/
    ├── shared/                            # kernel compartido (sólo lo usado por >1 slice)
    │   ├── domain/
    │   │   ├── model/                     # POJOs Lombok, sin anotaciones de framework
    │   │   │   ├── ChequeraSerie, ChequeraCuota, ChequeraPago,
    │   │   │   ├── ChequeraFacturacionElectronica, ChequeraSerieReemplazo, ChequeraCuotaReemplazo
    │   │   │   ├── FacturacionElectronica, Comprobante, ComprobanteAfip
    │   │   │   ├── Persona, Domicilio, Legajo, Carrera, InscripcionFull
    │   │   │   ├── Facultad, TipoChequera, Geografica, Lectivo, ArancelTipo, Producto,
    │   │   │   ├── LectivoAlternativa, PreferenciaPago
    │   │   │   └── DocumentoPdf (ruta del archivo generado)
    │   │   └── ports/out/
    │   │       └── SenderSettings (testing, fromChequera, fromRecibo)
    │   └── infrastructure/
    │       ├── config/SenderSettingsAdapter
    │       ├── dto/                       # los 30 DTOs actuales, movidos tal cual
    │       ├── mapper/                    # DTO ↔ dominio (1 por agregado)
    │       ├── support/{Jsonifier,Jsonifyable}
    │       └── web/controller/PingController
    ├── datos/                             # adaptadores de lectura/escritura contra otros servicios
    │   ├── chequera/       ports/out + application/service/ChequeraDatosService
    │   │                   infrastructure/client/{6 Feign} + adapter/{6} + mapper
    │   ├── facturacion/    ports/out + FacturacionDatosService + client/adapter/mapper
    │   ├── persona/        ports/out + PersonaDatosService + client/adapter/mapper
    │   ├── catalogo/       ports/out + CatalogoDatosService + client/adapter/mapper
    │   ├── preferenciaPago/ports/out + PreferenciaPagoDatosService + client/adapter/mapper
    │   └── validacionEmail/ports/out + ValidacionEmailService + client/adapter
    ├── gateways/                          # capacidades técnicas compartidas de salida
    │   └── correo/
    │       ├── domain/model/{Correo, AdjuntoCorreo, ImagenEnLinea}
    │       ├── domain/ports/in/EnviarCorreoUseCase
    │       ├── domain/ports/out/CorreoEnviador
    │       ├── application/service/CorreoService
    │       ├── application/usecases/EnviarCorreoUseCaseImpl
    │       └── infrastructure/mail/JavaMailCorreoEnviadorAdapter
    └── procesos/                          # casos de uso de negocio (inbound ports)
        ├── chequera/
        │   ├── domain/model/{ComandoChequera, ComandoEnvioChequera}
        │   ├── domain/ports/in/{GenerarChequeraPdf, GenerarChequeraReemplazoPdf,
        │   │                    GenerarCuotaPdf, EnviarChequera, EnviarCuota}UseCase
        │   ├── domain/ports/out/ChequeraPdfGenerator
        │   ├── application/usecases/…UseCaseImpl (5)
        │   ├── application/service/{ChequeraService, ChequeraEmailComposer,
        │   │                          ChequeraDatosAssembler, DestinatariosResolver}
        │   ├── application/exception/ChequeraException
        │   └── infrastructure/
        │       ├── pdf/{OpenPdfChequeraGenerator, ChequeraPdfLayout, ChequeraPdfData}
        │       ├── web/controller/ChequeraController
        │       └── kafka/{ChequeraEventListener, event/SendChequeraEvent,
        │                  mapper/SendChequeraEventMapper, config/ChequeraKafkaConfig}
        ├── recibo/  (misma forma; infrastructure/pdf/{OpenPdfReciboGenerator, QrAfipGenerator})
        └── enlacePago/  (EnviarEnlacePagoUseCase + EnlacePagoEmailComposer + controller MP)
```

### Regla de dependencias entre slices

```
procesos/* ──► datos/*.application.service        (fachada pública del slice de datos)
procesos/* ──► gateways/correo.application.service (fachada pública del gateway)
todos       ──► shared.domain.model + shared.domain.ports.out
nadie       ──► datos/*.domain.ports.out  ó  datos/*.infrastructure.*  (salvo el propio slice)
```

Es la misma relación que hoy usa `mercadopago`: `procesos/chequera` importa
`datos.chequeraCuota.application.service.ChequeraCuotaService` y `gateways.mensajeria.MensajeriaService`.

## 4. Inventario de slices

### `datos/*`

| Slice | Contenido movido | Ports out |
|---|---|---|
| `datos/chequera` | `ChequeraSerieClient`, `ChequeraCuotaClient`, `ChequeraPagoClient`, `ChequeraFacturacionElectronicaClient`, `ChequeraSerieReemplazoClient`, `ChequeraCuotaReemplazoClient`, `ChequeraMessageCheckClient` | `ChequeraSerieRepository` (findByUnique, markSent), `ChequeraCuotaRepository` (findByChequeraCuotaId, findByUnique, actualizarCodigosBarras), `ChequeraPagoRepository`, `ChequeraFacturacionElectronicaRepository`, `ChequeraReemplazoRepository`, `ChequeraMessageCheckRepository` |
| `datos/facturacion` | `FacturacionElectronicaClient`, `ComprobanteClient`, `ReciboMessageCheckClient` | `FacturacionElectronicaRepository` (findById, findNextPendiente, update), `ComprobanteRepository`, `ReciboMessageCheckRepository` |
| `datos/persona` | `PersonaClient`, `LegajoClient`, `CarreraClient`, `SincronizeClient` | `PersonaRepository` (findByUnique, findInscripcionFull), `LegajoRepository`, `CarreraRepository`, `CarreraAlumnoSincronizacion` |
| `datos/catalogo` | `LectivoAlternativaClient` (+ `FacultadClient` sólo si sobrevive `generateMatriculaPdf`) | `LectivoAlternativaRepository` |
| `datos/preferenciaPago` | `client/tesoreria/mercadopago/ChequeraClient` | `PreferenciaPagoRepository` (crearContexto, crearContextoBulk) |
| `datos/validacionEmail` | `client/.../facade/ToolClient` | `ValidadorEmail` (validar) |

> `Tool.convertStringToList` desaparece: es `List.of(email)`.

### `procesos/*`

| Slice | Inbound ports (use cases) | Endpoints/entradas |
|---|---|---|
| `chequera` | `GenerarChequeraPdfUseCase`, `GenerarChequeraReemplazoPdfUseCase`, `GenerarCuotaPdfUseCase`, `EnviarChequeraUseCase` (comando con `bulk`), `EnviarCuotaUseCase` | 4 GET `generatePdf*`, `generatePdf/reemplazo/completa`, `generateCuotaPdf`, `sendChequera`, `sendChequera/bulk`, `sendCuota/{id}`, topic `send-chequera` |
| `recibo` | `GenerarReciboPdfUseCase`, `EnviarReciboUseCase`, `EnviarSiguienteReciboUseCase` | `recibo/pdf/{id}`, `recibo/send/{id}` (@Async → 202), `recibo/sendNext` |
| `enlacePago` | `EnviarEnlacePagoUseCase` | `mercadopago/cuota/{chequeraCuotaId}` |
| `notificacion` | `EnviarNotificacionUseCase`, `ProcesarCorreoEventUseCase` | `POST /correo/enviar`, topic `send-correo` (consume) y `notificacion-resultado` (publica) |

### `gateways/correo`

Un solo adaptador JavaMail para los 4 procesos. `Correo` modela: `from`, `para`, `replyTo`,
`asunto`, `cuerpo`, `html`, `adjuntos` (ruta + nombre), `imagenesEnLinea` (cid + ruta).
El adapter setea `From` sólo si viene informado (preserva el comportamiento de `MercadoPagoService`,
que hoy no lo setea).

## 5. Contratos que NO cambian (criterio de aceptación)

1. **HTTP**: mismo path, método, `produces`, códigos (`200/202/204/500`), nombres de archivo
   (`chequera.pdf`, `cuota.pdf`, `recibo.pdf`) y cabeceras de cache.
2. **Kafka**: topic `send-chequera`, `groupId`, deserializador, `trusted.packages`, error handler
   (FixedBackOff 1000/2) y mapeo `um.tesoreria.core.event.SendChequeraEvent → SendChequeraEvent`.
3. **Feign**: mismas URLs, `contextId` únicos, timeouts 15s/300s de `bootstrap.yml`.
4. **Mail**: mismos `from`, `replyTo`, asuntos, cuerpos (texto y HTML), CIDs `logoImage`,
   `medioPago1`, `medioPago2`, logos `marca_um_65.png`/`marca_etec.png` (facultad 15) y adjuntos.
5. **PDF**: mismos nombres (`{path.reports}chequera-*.pdf`, `cuota-*.pdf`, `recibo-*.pdf`),
   mismas 2 copias ORIGINAL/DUPLICADO, QR AFIP, código de barras Inter25 y layout.
6. **Config**: `path.reports`, `app.testing`, `app.mail.fromchequera/fromrecibo`, Consul,
   springdoc y `DtoSerializationTest`.
7. **DTOs**: mismos nombres JSON, `@JsonFormat` y defaults (protegidos por test).

> **Desvíos aprobados durante la implementación.** Los endpoints de envío de chequera y cuota
> pasan a `202 Accepted` (publican en `send-chequera` / `send-cuota`); `trusted.packages` y el
> `value-deserializer` global se eliminan (factories explícitas) y el `FixedBackOff` queda en 3
> reintentos de 1s con DLT (`*.DLT`). Se agrega `notificacion` (`send-correo`,
> `notificacion-resultado`) y `app.mail.fromnotificacion`. Todo se detalla en el `CHANGELOG` 2.5.0.

## 6. Estrategia de migración por fases

Cada fase termina en `mvn -q -DskipTests compile` verde y sin paquetes viejos conviviendo
(evita `contextId` duplicados y beans ambiguos). No se commitea nada sin autorización.

| Fase | Trabajo | Resultado |
|---|---|---|
| **0** | Baseline: `mvn test` sobre el working tree actual; snapshot de `git status` | Punto de comparación |
| **1** | `shared/`: mover 30 DTOs, crear ~24 modelos + mappers + `SenderSettings` + `Jsonifier` + `PingController`; actualizar `DtoSerializationTest` | `shared` compila |
| **2** | `gateways/correo` + `datos/validacionEmail` | Correo centralizado |
| **3** | `datos/{chequera,facturacion,persona,catalogo,preferenciaPago}`; cambiar `@EnableFeignClients`; borrar `client/` | Datos accesibles por ports |
| **4** | `procesos/chequera` (use cases + PDF adapter + composer + controller + Kafka) y borrar `ChequeraService`, `FormulariosToPdfService`, `ChequeraController`, `consumer/`, `event/`, `service/pdf/` | Chequera migrada |
| **5** | `procesos/recibo` y borrar `ReciboService`, `ReciboController` | Recibo migrado |
| **6** | `procesos/enlacePago` y borrar `MercadoPagoService`, `MercadoPagoController` | Servicio 100% hexagonal |
| **7** | Tests de casos de uso (Mockito) + reglas ArchUnit, `docs/diagrams/*.mmd`, README, CHANGELOG | Calidad y documentación |
| **8** | Verificación final: `mvn test`, `mvn -DskipTests package`, grep de pureza de `domain/` | Cierre |

## 7. Decisiones aprobadas

1. **Layout y nomenclatura**: grupos `shared/datos/gateways/procesos` + nombres en español.
2. **Granularidad de `datos`**: 6 slices por agregado (`chequera`, `facturacion`, `persona`,
   `catalogo`, `preferenciaPago`, `validacionEmail`).
3. **Round-trip de `FacturacionElectronica`**: modelo de dominio + `Object original` para
   reconstruir el PUT completo sin perder campos.
4. **Código muerto**: se elimina y se documenta como `Removed` en el CHANGELOG.
5. **Tests**: portar los actuales + unit tests de casos de uso con Mockito + reglas ArchUnit
   (se agrega `com.tngtech.archunit:archunit-junit5` en scope `test`).

## 7.1 Correcciones mínimas incluidas

Se aplican sólo donde el refactor obliga a reescribir la línea; no se cambia ningún flujo exitoso:

- `EnviarReciboUseCase`: usar el `chequeraPago` ya resuelto (hoy se vuelve a leer del DTO y puede
  ser `null` → NPE).
- Incremento de `retries` null-safe (`Optional.ofNullable(...).orElse(0) + 1`).
- `GenerarReciboPdfUseCase` + controller: si no hay PDF, responder `204` en lugar de lanzar
  `FileNotFoundException` (mismo criterio que ya usan los endpoints de chequera). Se extendió a
  `generateCuotaPdf` y `generatePdf/reemplazo/completa`.
- `GenerarCuotaPdfUseCase`: corregido el orden `productoId`/`alternativaId` al consultar la cuota
  (el código anterior los enviaba invertidos a core).
- Se documentan en `[2.5.0] → Fixed` del CHANGELOG.

## 7.2 Resultado de la implementación

- 262 archivos Java en `main` (26 modelos de dominio, 6 slices de datos, 1 gateway, 4 procesos:
  `chequera`, `recibo`, `enlacePago` y `notificacion`).
- `mvn clean test`: contexto Spring + tests de casos de uso/DTOs + 7 reglas ArchUnit en verde (los
  tests de `notificacion` se suman a esa suite).
- `mvn -DskipTests package`: jar `target/um.tesoreria.sender-service.jar` generado.
- Sin paquetes horizontales legacy ni puentes de compatibilidad.

## 8. Tests de integración

- HTTP end-to-end con `@SpringBootTest(RANDOM_PORT)` + WireMock + GreenMail + directorio temporal:
  `ChequeraIntegracionIT`, `ReciboIntegracionIT`, `EnlacePagoIntegracionIT`.
- Kafka con `@EmbeddedKafka`: `KafkaChequeraIntegracionIT` cubre el contrato nuevo (JSON sin
  `__TypeId__`, tipo target) y un caso legacy con el header que emitía `core`. El consumidor ya
  no depende de headers de tipo; detalle en `PLANIFICACION_KAFKA_EVENTOS.md` (raíz del workspace).
- `NotificacionIntegracionIT`: consumo de `send-correo`, idempotencia por `eventId`, publicación
  del resultado en `notificacion-resultado` y envío por `POST /correo/enviar`.
- Aislados del build rápido con `maven-failsafe-plugin` (`*IT`); `mvn verify` los ejecuta y
  JaCoCo reporta en `post-integration-test`.
- Hallazgo relevante para futuros stubs: Jackson 3 respeta
  `@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXX")` y `FAIL_ON_NULL_FOR_PRIMITIVES` está activo;
  los payloads de WireMock deben usar `+0000` (no `+00:00`) e incluir todos los primitivos.

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Pérdida de campos en PUT de facturación | `Object original` + test de round-trip del mapper |
| URLs Feign alteradas al normalizar `name`+`path` | Migrar cliente por cliente y comparar URL armada; si hay duda, conservar la anotación literal |
| Regresión de layout PDF/mail | Portar los builders casi literalmente; no "mejorar" estilos en esta tarea |
| NPE preexistentes (`retries`, `chequeraPago`) | Corregir sólo con null-safety explícito y documentarlo en CHANGELOG |
| Working tree sucio (migración Kotlin) | No commitear; trabajar sobre el estado actual y reportar cualquier conflicto |
| Refactor enorme en un solo paso | Fases con compilación verde + verificación final |

## 10. Fuera de alcance

- Cambiar endpoints, topic, formato de mails/PDF o contratos Feign.
- Corregir los clientes desalineados de `core-service` (`generatePdf` sin `codigoBarras`, `sendCuota`).
- Migrar `tesoreria-report`/`facturador` ni tocar otros repos.
- Bump de versión y release: se resolvió en `2.5.0` (entradas movidas de `[Unreleased]` a `[2.5.0]`).
