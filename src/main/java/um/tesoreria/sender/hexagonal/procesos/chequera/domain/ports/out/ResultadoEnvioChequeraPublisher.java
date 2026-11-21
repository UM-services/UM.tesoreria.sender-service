package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out;

/**
 * Publica el resultado del envío de una chequera para que core cierre la tarea del envío masivo
 * ({@code frmCheqMail}). Sin esto, el job quedaría en {@code ENCOLADA} y el operador no tendría el
 * «OK/ERROR» por fila que sí daba el legacy.
 */
public interface ResultadoEnvioChequeraPublisher {

    void publicar(Long chequeraId, boolean enviado, String error);
}
