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

    /** Solicitud idempotente creada por core; nula en eventos legacy (el sender la crea). */
    private String solicitudId;
    /** Clave de chequera_serie; única para el ledger. */
    private Long chequeraId;
    /** Job de envío masivo que originó la solicitud, si aplica. */
    private Long jobId;
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
