package um.tesoreria.sender.hexagonal.shared.domain.model;

import java.util.UUID;
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
public class ChequeraMessageCheck {

    private UUID chequeraMessageCheckId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;

    @Builder.Default
    private String payload = "";

}
