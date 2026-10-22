package um.tesoreria.sender.hexagonal.procesos.enlacePago.application.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service.PreferenciaPagoDatosService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.exception.CorreoException;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.procesos.enlacePago.application.service.EnlacePagoEmailComposer;
import um.tesoreria.sender.hexagonal.procesos.enlacePago.domain.ports.in.EnviarEnlacePagoUseCase;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviarEnlacePagoUseCaseImpl implements EnviarEnlacePagoUseCase {

    private static final String REPLY_TO = "no-reply@um.edu.ar";
    private static final String SIN_EMAIL = "ERROR: Sin datos de e-mail para ENVIAR";

    private final ChequeraDatosService chequeraDatosService;
    private final PreferenciaPagoDatosService preferenciaPagoDatosService;
    private final EnlacePagoEmailComposer enlacePagoEmailComposer;
    private final CorreoService correoService;

    @Override
    public String enviar(Long chequeraCuotaId) {
        log.debug("Processing EnviarEnlacePagoUseCaseImpl.enviar({})", chequeraCuotaId);
        var chequeraCuota = chequeraDatosService.obtenerCuotaPorId(chequeraCuotaId);
        var contexto = preferenciaPagoDatosService.obtenerContextoActivo(chequeraCuotaId);
        var serie = chequeraCuota.getChequeraSerie();
        if (serie == null) {
            return SIN_EMAIL;
        }
        var domicilio = serie.getDomicilio();
        if (domicilio == null) {
            return SIN_EMAIL;
        }
        if (vacio(domicilio.getEmailPersonal()) && vacio(domicilio.getEmailInstitucional())) {
            return SIN_EMAIL;
        }

        String nombreAlumno = Objects.requireNonNull(serie.getPersona()).apellidoNombre();
        String cuerpo = enlacePagoEmailComposer.cuerpo(nombreAlumno, chequeraCuota, contexto);

        List<String> addresses = new ArrayList<>();
        if (!vacio(domicilio.getEmailPersonal())) {
            addresses.add(domicilio.getEmailPersonal());
        }
        if (!vacio(domicilio.getEmailInstitucional())) {
            addresses.add(domicilio.getEmailInstitucional());
        }

        Correo correo = Correo.builder()
                .para(addresses)
                .replyTo(REPLY_TO)
                .asunto("Envío de Enlace de MercadoPago: " + nombreAlumno)
                .cuerpo(cuerpo)
                .html(true)
                .build();

        try {
            correoService.enviar(correo);
        } catch (CorreoException e) {
            if (e.getTipo() == CorreoException.Tipo.MENSAJE) {
                return "Enlace NO pudo enviarse";
            }
            throw e;
        }
        return "Envío de Correo Ok";
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isEmpty();
    }

}
