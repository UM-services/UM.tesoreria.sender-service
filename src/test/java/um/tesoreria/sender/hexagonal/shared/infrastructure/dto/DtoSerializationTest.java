package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Protege la paridad de los DTOs migrados de Kotlin a Java: defaults, nombres de
 * propiedades JSON, propiedades calculadas, builders y formato de fechas.
 */
class DtoSerializationTest {

    private static final OffsetDateTime FECHA_UTC =
            OffsetDateTime.of(2026, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);

    private static final OffsetDateTime FECHA_CON_OFFSET =
            OffsetDateTime.of(2026, 1, 2, 3, 4, 5, 123_000_000, ZoneOffset.ofHours(-3));

    private final tools.jackson.databind.ObjectMapper jackson3 =
            tools.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();

    private final ObjectMapper jackson2 = JsonMapper.builder().findAndAddModules().build();

    @Test
    @DisplayName("Los defaults de los DTOs se preservan en el constructor sin argumentos")
    void defaultsPreserved() {
        ArancelTipoDto arancelTipo = new ArancelTipoDto();
        assertEquals("", arancelTipo.getDescripcion());
        assertEquals((byte) 0, arancelTipo.getMedioArancel());
        assertNull(arancelTipo.getArancelTipoId());

        CarreraDto carrera = new CarreraDto();
        assertEquals("", carrera.getNombre());
        assertEquals(0, carrera.getObligatorias());
        assertEquals((byte) 0, carrera.getVigente());

        ChequeraCuotaDto cuota = new ChequeraCuotaDto();
        assertEquals("", cuota.getCodigoBarras());
        assertEquals("", cuota.getI2Of5());
        assertEquals(BigDecimal.ZERO, cuota.getImporte1());
        assertEquals(BigDecimal.ZERO, cuota.getImporte1Original());
        assertEquals(0, cuota.getTramoId());

        ChequeraCuotaReemplazoDto cuotaReemplazo = new ChequeraCuotaReemplazoDto();
        assertNull(cuotaReemplazo.getTramoId());
        assertEquals(BigDecimal.ZERO, cuotaReemplazo.getImporte1());

        ChequeraSerieDto serie = new ChequeraSerieDto();
        assertEquals(BigDecimal.ZERO, serie.getBecaPorcentaje());
        assertEquals(BigDecimal.ZERO, serie.getImporteDeuda());
        assertEquals(0, serie.getCuotasDeuda());
        assertEquals((byte) 0, serie.getHpum());

        ChequeraSerieReemplazoDto serieReemplazo = new ChequeraSerieReemplazoDto();
        assertEquals(1, serieReemplazo.getGeograficaId().intValue());
        assertEquals(1, serieReemplazo.getAlternativaId().intValue());
        assertEquals(0, serieReemplazo.getAsentado());
        assertEquals("", serieReemplazo.getObservaciones());

        DomicilioDto domicilio = new DomicilioDto();
        assertNotNull(domicilio.getFecha(), "DomicilioDto.fecha se inicializa con now()");
        assertEquals("", domicilio.getCalle());
        assertEquals("", domicilio.getEmailPersonal());

        FacultadDto facultad = new FacultadDto();
        assertEquals(BigDecimal.ZERO, facultad.getCuentacontable());
        assertEquals(0L, facultad.getApiport());

        TipoChequeraDto tipoChequera = new TipoChequeraDto();
        assertEquals(1, tipoChequera.getGeograficaId());
        assertEquals(2, tipoChequera.getClaseChequeraId());

        PersonaDto persona = new PersonaDto();
        assertEquals("", persona.getCuit());
        assertEquals("", persona.getCbu());
        assertEquals((byte) 0, persona.getPrimero().byteValue());

        ChequeraMessageCheckDto messageCheck = ChequeraMessageCheckDto.builder().build();
        assertEquals("", messageCheck.getPayload());
        assertNull(messageCheck.getChequeraMessageCheckId());

        ReciboMessageCheckDto reciboCheck = ReciboMessageCheckDto.builder().build();
        assertEquals("", reciboCheck.getPayload());
        assertNull(reciboCheck.getReciboMessageCheckId());
    }

    @Test
    @DisplayName("Los nombres de propiedades JSON y la propiedad calculada apellidoNombre no cambian")
    void jsonPropertyNames() throws Exception {
        ChequeraCuotaDto cuota = new ChequeraCuotaDto();
        cuota.setI2Of5("A12345678901234567890");

        String json = jackson3.writeValueAsString(cuota);

        assertTrue(json.contains("\"i2Of5\":\"A12345678901234567890\""), json);

        PersonaDto persona = new PersonaDto();
        persona.setApellido("PEREZ");
        persona.setNombre("JUAN");

        assertEquals("PEREZ, JUAN", persona.getApellidoNombre());
        assertTrue(jackson3.writeValueAsString(persona).contains("\"apellidoNombre\":\"PEREZ, JUAN\""));
        assertTrue(jackson2.writeValueAsString(persona).contains("\"apellidoNombre\":\"PEREZ, JUAN\""));
        assertTrue(persona.jsonify().contains("\"apellidoNombre\""));
    }

    @Test
    @DisplayName("Las fechas respetan el patron yyyy-MM-dd'T'HH:mm:ssXX en UTC")
    void dateFormat() {
        ChequeraCuotaDto cuota = new ChequeraCuotaDto();
        cuota.setVencimiento1(FECHA_CON_OFFSET);

        String json3 = jackson3.writeValueAsString(cuota);
        assertTrue(json3.contains("\"vencimiento1\":\"2026-01-02T06:04:05Z\""), json3);

        String json2 = jsonifyAsString(cuota);
        assertTrue(json2.contains("\"vencimiento1\" : \"2026-01-02T06:04:05Z\""), json2);
    }

    @Test
    @DisplayName("Los builders generan los mismos valores que los builders Kotlin")
    void builders() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

        ChequeraMessageCheckDto messageCheck = new ChequeraMessageCheckDto.Builder()
                .chequeraMessageCheckId(id)
                .facultadId(3)
                .tipoChequeraId(2)
                .chequeraSerieId(99L)
                .payload("{\"a\":1}")
                .build();

        assertEquals(id, messageCheck.getChequeraMessageCheckId());
        assertEquals(3, messageCheck.getFacultadId().intValue());
        assertEquals(2, messageCheck.getTipoChequeraId().intValue());
        assertEquals(99L, messageCheck.getChequeraSerieId().longValue());
        assertEquals("{\"a\":1}", messageCheck.getPayload());

        ReciboMessageCheckDto recibo = new ReciboMessageCheckDto.Builder()
                .reciboMessageCheckId(id)
                .facturacionElectronicaId(1L)
                .chequeraPagoId(2L)
                .facultadId(3)
                .tipoChequeraId(2)
                .chequeraSerieId(99L)
                .productoId(1)
                .alternativaId(2)
                .cuotaId(5)
                .payload("{\"b\":2}")
                .build();

        assertEquals(id, recibo.getReciboMessageCheckId());
        assertEquals(1L, recibo.getFacturacionElectronicaId().longValue());
        assertEquals(2L, recibo.getChequeraPagoId().longValue());
        assertEquals(3, recibo.getFacultadId().intValue());
        assertEquals(2, recibo.getTipoChequeraId().intValue());
        assertEquals(99L, recibo.getChequeraSerieId().longValue());
        assertEquals(1, recibo.getProductoId().intValue());
        assertEquals(2, recibo.getAlternativaId().intValue());
        assertEquals(5, recibo.getCuotaId().intValue());
        assertEquals("{\"b\":2}", recibo.getPayload());
    }

    @Test
    @DisplayName("Round-trip JSON de un DTO con anidados y fechas")
    void roundTrip() throws Exception {
        PersonaDto persona = new PersonaDto();
        persona.setPersonaId(new BigDecimal("100.5"));
        persona.setDocumentoId(12345678);
        persona.setApellido("PEREZ");
        persona.setNombre("JUAN");

        ChequeraSerieDto serie = new ChequeraSerieDto();
        serie.setChequeraId(1L);
        serie.setPersona(persona);
        serie.setFecha(FECHA_UTC);
        serie.setBecaPorcentaje(new BigDecimal("15.00"));

        ChequeraCuotaDto cuota = new ChequeraCuotaDto();
        cuota.setChequeraCuotaId(10L);
        cuota.setI2Of5("A12345678901234567890");
        cuota.setVencimiento1(FECHA_UTC);
        cuota.setChequeraSerie(serie);

        String json = jackson3.writeValueAsString(cuota);
        ChequeraCuotaDto deserializado = jackson3.readValue(json, ChequeraCuotaDto.class);

        assertEquals(cuota, deserializado);
        assertEquals("PEREZ, JUAN", deserializado.getChequeraSerie().getPersona().getApellidoNombre());

        String json2 = jackson2.writeValueAsString(cuota);
        ChequeraCuotaDto deserializado2 = jackson2.readValue(json2, ChequeraCuotaDto.class);
        assertEquals(cuota, deserializado2);
    }

    private String jsonifyAsString(Object object) {
        try {
            return jackson2.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
