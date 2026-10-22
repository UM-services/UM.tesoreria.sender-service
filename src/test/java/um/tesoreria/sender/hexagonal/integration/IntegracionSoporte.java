package um.tesoreria.sender.hexagonal.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/**
 * Infraestructura compartida por los tests de integración:
 * WireMock para core/MercadoPago, GreenMail para SMTP, Kafka embebido y directorio temporal para
 * PDFs, más overrides de las URLs de cada cliente Feign.
 */
@EmbeddedKafka(partitions = 1, topics = {
        "send-chequera", "send-cuota", "send-correo", "notificacion-resultado"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration,"
                + "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration"
})
public abstract class IntegracionSoporte {

    protected static final List<String> FEIGN_CONTEXT_IDS = List.of(
            "chequeraSerieClient",
            "chequeraCuotaClient",
            "chequeraPagoClient",
            "chequeraFacturacionElectronicaClient",
            "chequeraSerieReemplazoClient",
            "chequeraCuotaReemplazoClient",
            "chequeraMessageCheckClient",
            "facturacionElectronicaClient",
            "comprobanteClient",
            "reciboMessageCheckClient",
            "personaClient",
            "legajoClient",
            "carreraClient",
            "sincronizeClient",
            "lectivoAlternativaClient",
            "chequeraPreferenciaClient",
            "mercadoPagoContextClient",
            "validacionEmailClient");

    protected static final Path REPORTES = crearDirectorioReportes();
    protected static final WireMockServer CORE = new WireMockServer(options().dynamicPort());
    protected static final GreenMail CORREO =
            new GreenMail(new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));

    static {
        CORE.start();
        CORREO.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CORE.stop();
            CORREO.stop();
        }));
    }

    @LocalServerPort
    protected int puerto;

    @Autowired
    protected EmbeddedKafkaBroker broker;

    @Autowired
    protected RestClient.Builder restClientBuilder;

    protected RestClient http() {
        return restClientBuilder.baseUrl("http://localhost:" + puerto).build();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("app.testing", () -> "false");
        registro.add("path.reports", () -> REPORTES.toAbsolutePath() + File.separator);
        registro.add("spring.mail.host", () -> "127.0.0.1");
        registro.add("spring.mail.port", () -> CORREO.getSmtp().getPort());
        registro.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registro.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        registro.add("spring.mail.properties.mail.smtp.starttls.required", () -> "false");
        FEIGN_CONTEXT_IDS.forEach(contextId -> registro.add(
                "spring.cloud.openfeign.client.config." + contextId + ".url", CORE::baseUrl));
    }

    @BeforeEach
    void limpiarEntorno() throws Exception {
        CORE.resetAll();
        CORREO.purgeEmailFromAllMailboxes();
        limpiarReportes();
    }

    protected ResponseEntity<String> get(String path) {
        return http().get().uri(path).retrieve().toEntity(String.class);
    }

    protected ResponseEntity<byte[]> getBytes(String path) {
        return http().get().uri(path).retrieve().toEntity(byte[].class);
    }

    protected Path reporte(String nombre) {
        return REPORTES.resolve(nombre);
    }

    private static Path crearDirectorioReportes() {
        try {
            return Files.createTempDirectory("sender-it-reports");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void limpiarReportes() {
        try (var archivos = Files.walk(REPORTES)) {
            archivos.filter(Files::isRegularFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(archivo -> {
                        try {
                            Files.deleteIfExists(archivo);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
