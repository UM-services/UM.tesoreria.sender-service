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
public class ChequeraSerieReemplazoDto implements Jsonifyable {

    private Long chequeraReemplazoId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer lectivoId;
    private Long arancelTipoId;
    private Integer cursoId;
    private int asentado = 0;
    private Integer geograficaId = 1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    private int cuotasPagadas = 0;
    private String observaciones = "";
    private Integer alternativaId = 1;
    private byte algoPagado = 0;
    private Integer tipoImpresionId;
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private PersonaDto persona;
    private DomicilioDto domicilio;
    private LectivoDto lectivo;
    private ArancelTipoDto arancelTipo;
    private GeograficaDto geografica;

}
