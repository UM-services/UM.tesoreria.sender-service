package um.tesoreria.sender.hexagonal.procesos.chequera.application.exception;

/**
 * El borrador no se puede imprimir como borrador (inexistente, emitido o descartado).
 */
public class BorradorNoImprimibleException extends RuntimeException {

    public BorradorNoImprimibleException(String mensaje) {
        super(mensaje);
    }
}
