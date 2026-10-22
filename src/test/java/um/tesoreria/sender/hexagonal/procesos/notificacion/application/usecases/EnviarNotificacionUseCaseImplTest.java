package um.tesoreria.sender.hexagonal.procesos.notificacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.procesos.notificacion.application.exception.NotificacionException;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoPayload;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.Remitente;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

class EnviarNotificacionUseCaseImplTest {

    private SenderSettings settings;
    private CorreoService correoService;
    private EnviarNotificacionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        settings = Mockito.mock(SenderSettings.class);
        correoService = Mockito.mock(CorreoService.class);
        useCase = new EnviarNotificacionUseCaseImpl(settings, correoService);
        when(settings.fromChequera()).thenReturn("chequeras@um.edu.ar");
        when(settings.fromRecibo()).thenReturn("recibos@um.edu.ar");
        when(settings.fromNotificacion()).thenReturn("notificaciones@um.edu.ar");
    }

    @Test
    void usaElRemitenteLogicoYNoElQueMandaElProductor() {
        useCase.enviar(payload(Remitente.CHEQUERA));

        ArgumentCaptor<Correo> captor = ArgumentCaptor.forClass(Correo.class);
        verify(correoService).enviar(captor.capture());
        var correo = captor.getValue();
        assertThatFrom(correo, "chequeras@um.edu.ar");
        org.assertj.core.api.Assertions.assertThat(correo.getPara()).containsExactly("alumno@um.edu.ar");
        org.assertj.core.api.Assertions.assertThat(correo.getReplyTo()).isEqualTo("no-reply@um.edu.ar");
    }

    @Test
    void usaElRemitenteDeNotificacionPorDefecto() {
        useCase.enviar(payload(null));

        ArgumentCaptor<Correo> captor = ArgumentCaptor.forClass(Correo.class);
        verify(correoService).enviar(captor.capture());
        assertThatFrom(captor.getValue(), "notificaciones@um.edu.ar");
    }

    @Test
    void enTestingRedirigeTodosLosDestinatarios() {
        when(settings.testing()).thenReturn(true);

        useCase.enviar(payload(Remitente.NOTIFICACION));

        ArgumentCaptor<Correo> captor = ArgumentCaptor.forClass(Correo.class);
        verify(correoService).enviar(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getPara())
                .containsExactly("daniel.quinterospinto@gmail.com");
    }

    @Test
    void rechazaUnPayloadSinDestinatarios() {
        var payload = payload(Remitente.NOTIFICACION);
        payload.setPara(List.of());

        assertThatThrownBy(() -> useCase.enviar(payload))
                .isInstanceOf(NotificacionException.class)
                .hasMessageContaining("destinatarios");
    }

    @Test
    void rechazaUnPayloadSinAsunto() {
        var payload = payload(Remitente.NOTIFICACION);
        payload.setAsunto(" ");

        assertThatThrownBy(() -> useCase.enviar(payload))
                .isInstanceOf(NotificacionException.class)
                .hasMessageContaining("asunto");
    }

    private CorreoPayload payload(Remitente remitente) {
        return CorreoPayload.builder()
                .remitente(remitente)
                .para(List.of("alumno@um.edu.ar"))
                .replyTo("no-reply@um.edu.ar")
                .asunto("Notificación")
                .cuerpo("Hola")
                .build();
    }

    private void assertThatFrom(Correo correo, String esperado) {
        org.assertj.core.api.Assertions.assertThat(correo.getFrom()).isEqualTo(esperado);
    }

}
