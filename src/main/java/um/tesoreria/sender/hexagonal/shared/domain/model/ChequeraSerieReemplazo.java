package um.tesoreria.sender.hexagonal.shared.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChequeraSerieReemplazo {

    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer lectivoId;
    private Integer alternativaId;

    private Persona persona;
    private Domicilio domicilio;
    private Facultad facultad;
    private TipoChequera tipoChequera;
    private Lectivo lectivo;
    private ArancelTipo arancelTipo;
    private Geografica geografica;

}
