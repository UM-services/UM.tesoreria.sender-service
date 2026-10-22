package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifyable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChequeraSerieDto implements Jsonifyable {

    private Long chequeraId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer lectivoId;
    private Integer arancelTipoId;
    private Integer cursoId;
    private Byte asentado;
    private Integer geograficaId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    private Integer cuotasPagadas;
    private String observaciones;
    private Integer alternativaId;
    private Byte algoPagado;
    private Integer tipoImpresionId;
    private byte flagPayperTic = 0;
    private String usuarioId;
    private byte enviado = 0;
    private byte retenida = 0;
    private Long version;
    private byte hpum = 0;
    private BigDecimal becaPorcentaje = BigDecimal.ZERO;
    private String becaResolucion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime becaFecha;
    private Long becaUserId;
    private int cuotasDeuda = 0;
    private BigDecimal importeDeuda = BigDecimal.ZERO;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime ultimoEnvio;
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private PersonaDto persona;
    private DomicilioDto domicilio;
    private LectivoDto lectivo;
    private ArancelTipoDto arancelTipo;
    private GeograficaDto geografica;

}
