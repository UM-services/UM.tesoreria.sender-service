package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifyable;

/**
 * Snapshot de impresión de un borrador de chequera (core:
 * {@code GET /emision/chequera/borradores/{id}/impresion}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BorradorImpresionDto implements Jsonifyable {

    private Long borradorId;
    private String estado;
    private BigDecimal personaId;
    private Integer documentoId;
    private String apellido;
    private String nombre;
    private Integer facultadId;
    private String facultadNombre;
    private Integer tipoChequeraId;
    private String tipoChequeraNombre;
    private Integer lectivoId;
    private String lectivoNombre;
    private Integer arancelTipoId;
    private String arancelTipoDescripcion;
    private Integer alternativaId;
    private String alternativaTitulo;
    private BigDecimal becaPorcentaje;
    private Byte hpum;
    /**
     * Sin patrón fijo: el core serializa ISO-8601 y puede traer fracciones de segundo
     * ({@code 2026-11-18T12:05:13.692435Z}), que un patrón {@code ss} rechaza.
     */
    private OffsetDateTime fecha;
    private String carrera;
    private BigDecimal total;
    private List<CuotaDto> cuotas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CuotaDto implements Jsonifyable {

        private Integer productoId;
        private String productoNombre;
        private Integer alternativaId;
        private Integer cuotaId;
        private Integer mes;
        private Integer anio;
        private OffsetDateTime vencimiento1;
        private OffsetDateTime vencimiento2;
        private OffsetDateTime vencimiento3;
        private BigDecimal importe1;
        private BigDecimal importe2;
        private BigDecimal importe3;
        private Byte pagado;
        private Byte baja;
        private Byte manual;
        private Byte compensada;
    }
}
