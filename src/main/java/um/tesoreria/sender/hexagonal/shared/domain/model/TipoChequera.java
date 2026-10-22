package um.tesoreria.sender.hexagonal.shared.domain.model;

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
public class TipoChequera {

    private Integer tipoChequeraId;
    private String nombre;
    private String emailCopia;
    private Geografica geografica;

}
