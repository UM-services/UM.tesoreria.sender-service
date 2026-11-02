package um.tesoreria.sender.hexagonal.procesos.notificacion.application.exception;

public class NotificacionException extends RuntimeException {

    public enum Tipo {
        EVENTO_INVALIDO, PAYLOAD_INVALIDO, REMITENTE_DESCONOCIDO
    }

    private final Tipo tipo;

    public NotificacionException(Tipo tipo, String message) {
        super(message);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

}
