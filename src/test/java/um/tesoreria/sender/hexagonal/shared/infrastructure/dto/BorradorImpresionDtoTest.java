package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

/**
 * El snapshot de impresión lo serializa el core como ISO-8601, con o sin fracciones de segundo.
 * El DTO no debe imponer un patrón que rechace {@code 2026-11-18T12:05:13.692435Z}.
 */
class BorradorImpresionDtoTest {

    private static final String JSON = """
            {
              "borradorId": 10,
              "estado": "ABIERTO",
              "personaId": 47531187,
              "documentoId": 1,
              "apellido": "Pérez",
              "nombre": "Ana",
              "facultadId": 6,
              "facultadNombre": "Rectorado",
              "tipoChequeraId": 958,
              "tipoChequeraNombre": "Arancel Gimnasio",
              "lectivoId": 37,
              "lectivoNombre": "Lectivo 2026 - 2027",
              "arancelTipoId": 1,
              "arancelTipoDescripcion": "Ciclo Completo",
              "alternativaId": 1,
              "alternativaTitulo": "Arancel Mensual",
              "becaPorcentaje": 0.0,
              "fecha": "2026-11-18T12:05:13.692435Z",
              "total": 66000.00,
              "cuotas": [
                {
                  "productoId": 3,
                  "productoNombre": "Arancel Mensual",
                  "alternativaId": 1,
                  "cuotaId": 4,
                  "mes": 6,
                  "anio": 2026,
                  "vencimiento1": "2026-06-08T00:00:00Z",
                  "vencimiento2": "2026-06-15T00:00:00Z",
                  "vencimiento3": "2026-06-30T00:00:00.123456Z",
                  "importe1": 22000.00,
                  "importe2": 22000.00,
                  "importe3": 22000.00,
                  "pagado": 0,
                  "baja": 0,
                  "manual": 0,
                  "compensada": 0
                }
              ]
            }
            """;

    private final ObjectMapper jackson2 = JsonMapper.builder().findAndAddModules().build();
    private final tools.jackson.databind.ObjectMapper jackson3 =
            tools.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();

    @Test
    void deserializaFechasConFraccionesDeSegundo() throws Exception {
        BorradorImpresionDto dto = jackson2.readValue(JSON, BorradorImpresionDto.class);

        assertNotNull(dto.getFecha());
        assertEquals(2026, dto.getFecha().getYear());
        assertEquals(1, dto.getCuotas().size());
        assertNotNull(dto.getCuotas().get(0).getVencimiento3());
    }

    @Test
    void deserializaConElMapperDeJackson3() throws Exception {
        BorradorImpresionDto dto = jackson3.readValue(JSON, BorradorImpresionDto.class);

        assertNotNull(dto.getFecha());
        assertEquals(66000.00, dto.getTotal().doubleValue());
        assertEquals("Arancel Mensual", dto.getCuotas().get(0).getProductoNombre());
    }
}
