package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendChequeraEvent implements Serializable {

    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer alternativaId;
    private Boolean copiaInformes;
    private Boolean incluyeMatricula;
    private Boolean codigoBarras;
    /**
     * Campos aditivos (opcionales). Core publica sin ellos y sigue haciendo el trabajo previo
     * por su cuenta; el shim REST del sender los usa para preservar el comportamiento legacy.
     */
    private Boolean bulk;
    private Boolean actualizarCodigosBarras;

}
