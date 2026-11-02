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
public class Legajo {

    private Long legajoId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer facultadId;
    private Integer lectivoId;
    private Integer planId;
    private Integer carreraId;

}
