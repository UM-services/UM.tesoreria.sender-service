package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import java.math.BigDecimal;
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
public class FacultadDto implements Jsonifyable {

    private Integer facultadId;
    private String nombre = "";
    private String codigoempresa = "";
    private String server = "";
    private String dbadm = "";
    private String dsn = "";
    private BigDecimal cuentacontable = BigDecimal.ZERO;
    private String apiserver = "";
    private long apiport = 0L;

}
