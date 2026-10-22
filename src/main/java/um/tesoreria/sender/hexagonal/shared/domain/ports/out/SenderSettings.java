package um.tesoreria.sender.hexagonal.shared.domain.ports.out;

public interface SenderSettings {

    boolean testing();

    String fromChequera();

    String fromRecibo();

    String fromNotificacion();

}
