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
public class LegajoDto implements Jsonifyable {

    private Long legajoId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer facultadId;
    private long numeroLegajo = 0L;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    private Integer lectivoId;
    private Integer planId;
    private Integer carreraId;
    private byte tieneCarrera = 0;
    private Integer geograficaId;
    private String contrasenha;
    private byte intercambio = 0;

}
