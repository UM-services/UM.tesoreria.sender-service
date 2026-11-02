package um.tesoreria.sender.hexagonal.gateways.correo.application.exception;

public class CorreoException extends RuntimeException {

    public enum Tipo {
        DIRECCION_INVALIDA,
        MENSAJE
    }

    private final Tipo tipo;

    public CorreoException(Tipo tipo, String message, Throwable cause) {
        super(message, cause);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

}
