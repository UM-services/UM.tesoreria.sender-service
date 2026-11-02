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
public class Carrera {

    private Integer facultadId;
    private Integer planId;
    private Integer carreraId;
    private String nombre;

}
