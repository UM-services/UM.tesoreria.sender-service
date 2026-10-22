package um.tesoreria.sender.hexagonal.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static um.tesoreria.sender.hexagonal.integration.IntegracionSoporte.CORE;

/**
 * Stubs de los servicios externos (tesoreria-core y tesoreria-mercadopago) usados
 * por los tests de integración. Los payloads incluyen todos los campos primitivos
 * porque el decoder de Feign (Jackson 3) falla si un primitivo llega nulo.
 */
public final class StubsCore {

    public static final String CHEQUERA_SERIE = "/api/tesoreria/core/chequeraSerie/unique/1/2/3";
    public static final String CHEQUERA_SERIE_MARK = "/api/tesoreria/core/chequeraSerie/mark/sent/1/2/3";
    public static final String CHEQUERA_CUOTA_UNIQUE =
            "/api/tesoreria/core/chequeraCuota/unique/1/2/3/4/1/1";
    public static final String CHEQUERA_CUOTA_ID = "/api/tesoreria/core/chequeraCuota/9";
    public static final String CHEQUERA_PAGO = "/api/tesoreria/core/chequeraPago/7";
    public static final String FACTURACION = "/api/tesoreria/core/facturacionElectronica/5";
    public static final String FACTURACION_NEXT = "/api/tesoreria/core/facturacionElectronica/nextPendiente";
    public static final String COMPROBANTE = "/api/tesoreria/core/comprobante/1";
    public static final String CHEQUERA_FACTURACION =
            "/api/tesoreria/core/chequeraFacturacionElectronica/chequera/10";
    public static final String INSCRIPCION =
            "/api/tesoreria/core/persona/inscripcion/full/1/1/123/2026";
    public static final String PERSONA = "/api/tesoreria/core/persona/unique/1/123";
    public static final String SINCRONIZE = "/api/tesoreria/core/sincronize/carreraalumno/1/1/123";
    public static final String LEGAJO = "/api/tesoreria/core/legajo/unique/1/1/123";
    public static final String CARRERA = "/api/tesoreria/core/carrera/unique/1/1/1";
    public static final String ALTERNATIVAS = "/api/tesoreria/core/lectivoAlternativa/tipo/1/2026/2/1";
    public static final String PREFERENCIAS = "/api/tesoreria/mercadopago/chequera/create/context/1/2/3/1";
    public static final String PREFERENCIAS_BULK =
            "/api/tesoreria/mercadopago/chequera/create/context/bulk/1/2/3/1";
    public static final String CONTEXTO_MP = "/api/tesoreria/core/mercadoPagoContext/cuota/activo/9";
    public static final String UPDATE_BARRAS = "/api/tesoreria/core/chequeraCuota/updateBarras/1/2/3";
    public static final String CHEQUERA_CHECK = "/api/tesoreria/core/chequeraMessageCheck/";
    public static final String RECIBO_CHECK = "/api/tesoreria/core/reciboMessageCheck/";
    public static final String MAIL_VALIDATE = "/api/tesoreria/core/tool/mailvalidate";

    private static final String GEOGRAFICA =
            "{\"geograficaId\":1,\"nombre\":\"Sede Centro\",\"sinChequera\":0}";
    private static final String FACULTAD =
            "{\"facultadId\":1,\"nombre\":\"Facultad de Prueba\",\"apiport\":0}";
    private static final String TIPO_CHEQUERA =
            "{\"tipoChequeraId\":2,\"nombre\":\"Chequera\",\"emailCopia\":\"copias@example.com\","
                    + "\"geograficaId\":1,\"claseChequeraId\":2,\"imprimir\":0,\"contado\":0,"
                    + "\"multiple\":0,\"geografica\":" + GEOGRAFICA + "}";
    private static final String ARANCEL_TIPO =
            "{\"arancelTipoId\":1,\"descripcion\":\"Arancel Completo\",\"medioArancel\":0}";

    private StubsCore() {
    }

    public static void chequeraCompleta(boolean conCuotasPendientes) {
        stubGet(CHEQUERA_SERIE, serieJson(true, "ana@example.com", ""));
        stubGet(UPDATE_BARRAS, "[]");
        stubPost(CHEQUERA_SERIE_MARK, serieJson(true, "ana@example.com", ""));
        stubPost(CHEQUERA_CHECK, CHEQUERA_CHECK_JSON);
        stubGet(INSCRIPCION, "{}");
        stubGet(PERSONA, PERSONA_JSON);
        stubSinCuerpo(SINCRONIZE);
        stubGet(LEGAJO, LEGAJO_JSON);
        stubGet(CARRERA, CARRERA_JSON);
        stubGet(ALTERNATIVAS, ALTERNATIVAS_JSON);
        stubGet(PREFERENCIAS, preferenciasJson(conCuotasPendientes));
        stubGet(PREFERENCIAS_BULK, preferenciasJson(conCuotasPendientes));
    }

    public static void reciboCompleto(boolean conDomicilio, boolean conEmails) {
        stubGet(FACTURACION, FACTURACION_JSON);
        stubGet(FACTURACION_NEXT, FACTURACION_JSON);
        stubGet(CHEQUERA_PAGO, CHEQUERA_PAGO_JSON);
        stubGet(CHEQUERA_CUOTA_UNIQUE, cuotaJson(true));
        stubGet(CHEQUERA_SERIE, serieJson(conDomicilio,
                conEmails ? "ana@example.com" : "",
                conEmails ? "ana.institucional@example.com" : ""));
        stubGet(CHEQUERA_FACTURACION, null);
        stubGet(COMPROBANTE, COMPROBANTE_JSON);
        stubPut(FACTURACION, "{}");
        stubPost(MAIL_VALIDATE, "true");
        stubPost(RECIBO_CHECK, RECIBO_CHECK_JSON);
    }

    public static void cuotaPorClave() {
        stubGet(CHEQUERA_CUOTA_UNIQUE, cuotaJson(true));
    }

    public static void enlacePago(boolean conEmails) {
        stubGet(CHEQUERA_CUOTA_ID, cuotaConSerieJson(
                conEmails ? "ana@example.com" : "",
                conEmails ? "ana.institucional@example.com" : ""));
        stubGet(CONTEXTO_MP, CONTEXTO_MP_JSON);
    }

    public static String serieJson(boolean conDomicilio, String emailPersonal, String emailInstitucional) {
        String domicilio = conDomicilio
                ? "\"domicilio\":{\"calle\":\"Calle\",\"puerta\":\"123\",\"observaciones\":\"\","
                + "\"emailPersonal\":\"" + emailPersonal + "\","
                + "\"emailInstitucional\":\"" + emailInstitucional + "\"},"
                : "";
        return "{"
                + "\"chequeraId\":10,"
                + "\"facultadId\":1,"
                + "\"tipoChequeraId\":2,"
                + "\"chequeraSerieId\":3,"
                + "\"personaId\":1,"
                + "\"documentoId\":123,"
                + "\"lectivoId\":2026,"
                + "\"arancelTipoId\":1,"
                + "\"alternativaId\":1,"
                + "\"becaPorcentaje\":0,"
                + "\"flagPayperTic\":0,"
                + "\"enviado\":0,"
                + "\"retenida\":0,"
                + "\"hpum\":0,"
                + "\"cuotasDeuda\":0,"
                + "\"persona\":{\"personaId\":1,\"documentoId\":123,\"apellido\":\"Perez\",\"nombre\":\"Ana\"},"
                + domicilio
                + "\"facultad\":" + FACULTAD + ","
                + "\"tipoChequera\":" + TIPO_CHEQUERA + ","
                + "\"lectivo\":{\"lectivoId\":2026,\"nombre\":\"2026\"},"
                + "\"arancelTipo\":" + ARANCEL_TIPO + ","
                + "\"geografica\":" + GEOGRAFICA
                + "}";
    }

    public static String cuotaJson(boolean pendiente) {
        return "{"
                + "\"chequeraCuotaId\":9,"
                + "\"chequeraId\":10,"
                + "\"facultadId\":1,"
                + "\"tipoChequeraId\":2,"
                + "\"chequeraSerieId\":3,"
                + "\"productoId\":4,"
                + "\"alternativaId\":1,"
                + "\"cuotaId\":1,"
                + "\"mes\":11,"
                + "\"anho\":2026,"
                + "\"vencimiento1\":\"2026-11-10T00:00:00+0000\","
                + "\"importe1\":100.00,"
                + "\"vencimiento2\":\"2026-11-20T00:00:00+0000\","
                + "\"importe2\":110.00,"
                + "\"vencimiento3\":\"2026-11-30T00:00:00+0000\","
                + "\"importe3\":120.00,"
                + "\"codigoBarras\":\"1234567890\","
                + "\"pagado\":" + (pendiente ? 0 : 1) + ","
                + "\"baja\":0,"
                + "\"manual\":0,"
                + "\"compensada\":0,"
                + "\"tramoId\":0,"
                + "\"facultad\":" + FACULTAD + ","
                + "\"tipoChequera\":" + TIPO_CHEQUERA + ","
                + "\"producto\":{\"productoId\":4,\"nombre\":\"Matrícula\"}"
                + "}";
    }

    private static String cuotaConSerieJson(String emailPersonal, String emailInstitucional) {
        String cuota = cuotaJson(true);
        return cuota.substring(0, cuota.length() - 1)
                + ",\"chequeraSerie\":" + serieJson(true, emailPersonal, emailInstitucional)
                + "}";
    }

    private static String preferenciasJson(boolean pendiente) {
        return "[{\"mercadoPagoContext\":{"
                + "\"mercadoPagoContextId\":1,"
                + "\"chequeraCuotaId\":9,"
                + "\"initPoint\":\"https://mp/pagar\","
                + "\"fechaVencimiento\":\"2026-11-10T00:00:00+0000\","
                + "\"importe\":100.00,"
                + "\"activo\":1"
                + "},\"chequeraCuota\":" + cuotaJson(pendiente) + "}]";
    }

    private static void stubGet(String path, String body) {
        if (body == null) {
            CORE.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(404)));
            return;
        }
        CORE.stubFor(get(urlEqualTo(path)).willReturn(okJson(body)));
    }

    private static void stubSinCuerpo(String path) {
        CORE.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)));
    }

    private static void stubPost(String path, String body) {
        CORE.stubFor(post(urlEqualTo(path)).willReturn(okJson(body)));
    }

    private static void stubPut(String path, String body) {
        CORE.stubFor(put(urlEqualTo(path)).willReturn(okJson(body)));
    }

    public static final String PERSONA_JSON =
            "{\"personaId\":1,\"documentoId\":123,\"apellido\":\"Perez\",\"nombre\":\"Ana\"}";

    public static final String LEGAJO_JSON =
            "{\"facultadId\":1,\"planId\":1,\"carreraId\":1,\"personaId\":1,\"documentoId\":123,"
                    + "\"numeroLegajo\":0,\"tieneCarrera\":0,\"intercambio\":0}";

    public static final String CARRERA_JSON =
            "{\"facultadId\":1,\"planId\":1,\"carreraId\":1,\"nombre\":\"Carrera de Prueba\","
                    + "\"trabajofinal\":0,\"chequeraunica\":0,\"obligatorias\":0,\"optativas\":0,"
                    + "\"vigente\":0}";

    public static final String ALTERNATIVAS_JSON =
            "[{\"lectivoAlternativaId\":1,\"facultadId\":1,\"lectivoId\":2026,\"tipoChequeraId\":2,"
                    + "\"productoId\":4,\"alternativaId\":1,\"titulo\":\"Cuota\",\"cuotas\":10}]";

    public static final String CHEQUERA_CHECK_JSON =
            "{\"chequeraMessageCheckId\":\"00000000-0000-0000-0000-000000000001\","
                    + "\"facultadId\":1,\"tipoChequeraId\":2,\"chequeraSerieId\":3}";

    public static final String RECIBO_CHECK_JSON =
            "{\"reciboMessageCheckId\":\"00000000-0000-0000-0000-000000000002\","
                    + "\"facturacionElectronicaId\":5,\"chequeraPagoId\":7,\"facultadId\":1,"
                    + "\"tipoChequeraId\":2,\"chequeraSerieId\":3,\"productoId\":4,"
                    + "\"alternativaId\":1,\"cuotaId\":1}";

    public static final String CHEQUERA_PAGO_JSON =
            "{\"chequeraPagoId\":7,\"chequeraCuotaId\":9,\"facultadId\":1,\"tipoChequeraId\":2,"
                    + "\"chequeraSerieId\":3,\"productoId\":4,\"alternativaId\":1,\"cuotaId\":1,"
                    + "\"mes\":11,\"anho\":2026,\"fecha\":\"2026-10-01T00:00:00+0000\","
                    + "\"importe\":100.00,\"observaciones\":\"\",\"verificador\":0}";

    public static final String FACTURACION_JSON =
            "{\"facturacionElectronicaId\":5,\"chequeraPagoId\":7,\"comprobanteId\":1,"
                    + "\"numeroComprobante\":1,\"personaId\":1,\"tipoDocumento\":\"DNI\","
                    + "\"apellido\":\"Perez\",\"nombre\":\"Ana\",\"cuit\":\"20111111112\","
                    + "\"condicionIva\":\"Consumidor Final\",\"importe\":100.00,"
                    + "\"cae\":\"12345678901234\",\"fechaRecibo\":\"2026-10-01T00:00:00+0000\","
                    + "\"fechaVencimientoCae\":\"2026-10-31T00:00:00+0000\","
                    + "\"enviada\":0,\"retries\":0}";

    public static final String COMPROBANTE_JSON =
            "{\"comprobanteId\":1,\"comprobanteAfipId\":1,\"puntoVenta\":1,"
                    + "\"letraComprobante\":\"B\","
                    + "\"ordenPago\":0,\"aplicaPendiente\":0,\"cuentaCorriente\":0,\"debita\":0,"
                    + "\"diasVigencia\":0,\"facturacionElectronica\":0,"
                    + "\"comprobanteAfip\":{\"comprobanteAfipId\":1,\"nombre\":\"Factura B\","
                    + "\"label\":\"FACTURA B\"}}";

    public static final String CONTEXTO_MP_JSON =
            "{\"mercadoPagoContextId\":1,\"chequeraCuotaId\":9,\"initPoint\":\"https://mp/pagar\","
                    + "\"fechaVencimiento\":\"2026-11-10T00:00:00+0000\",\"importe\":100.00,"
                    + "\"activo\":1}";

}
