package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvioSolicitudDto {

    private String solicitudId;
    private Long chequeraId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer lectivoId;
    private Long jobId;
    private String estado;
    private Integer intentos;
    private String ultimoError;
    private OffsetDateTime enviadoAt;

}
