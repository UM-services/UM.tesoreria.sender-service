package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvioSolicitudClaimDto {

    private String solicitudId;
    private Boolean puedeEnviar;
    private String estado;
    private Integer intentos;
    private String motivo;

}
