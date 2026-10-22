package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

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
public class LectivoAlternativaDto implements Jsonifyable {

    private Long lectivoAlternativaId;
    private Integer facultadId;
    private Integer lectivoId;
    private Integer tipoChequeraId;
    private Integer productoId;
    private Integer alternativaId;
    private String titulo = "";
    private Integer cuotas = 0;
    private Integer descuentoPagoTermino = 0;

}
