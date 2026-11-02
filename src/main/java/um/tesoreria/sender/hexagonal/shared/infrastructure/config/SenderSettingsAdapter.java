package um.tesoreria.sender.hexagonal.shared.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@Component
public class SenderSettingsAdapter implements SenderSettings {

    @Value("${app.testing}")
    private Boolean testing;

    @Value("${app.mail.fromchequera}")
    private String fromChequera;

    @Value("${app.mail.fromrecibo}")
    private String fromRecibo;

    @Value("${app.mail.fromnotificacion:${app.mail.fromrecibo}}")
    private String fromNotificacion;

    @Override
    public boolean testing() {
        return Boolean.TRUE.equals(testing);
    }

    @Override
    public String fromChequera() {
        return fromChequera;
    }

    @Override
    public String fromRecibo() {
        return fromRecibo;
    }

    @Override
    public String fromNotificacion() {
        return fromNotificacion;
    }

}
