package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.persona.application.service.PersonaDatosService;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service.PreferenciaPagoDatosService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.exception.ChequeraException;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraCorreoFactory;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraEmailComposer;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.DestinatariosResolver;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.EnviarCuotaUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviarCuotaUseCaseImpl implements EnviarCuotaUseCase {

    private static final String ASUNTO = "Recordatorio - Pago cuota UM -> ";
    private static final String ENVIO_OK = "\n\nEnvío de Cuota Ok!!!\n\n";

    private final ChequeraDatosService chequeraDatosService;
    private final PersonaDatosService personaDatosService;
    private final PreferenciaPagoDatosService preferenciaPagoDatosService;
    private final ChequeraEmailComposer chequeraEmailComposer;
    private final DestinatariosResolver destinatariosResolver;
    private final ChequeraCorreoFactory chequeraCorreoFactory;
    private final CorreoService correoService;
    private final SenderSettings senderSettings;

    @Override
    public String enviar(Long chequeraCuotaId) {
        log.debug("Sending cuota {}", chequeraCuotaId);
        var cuota = chequeraDatosService.obtenerCuotaPorId(chequeraCuotaId);
        var serie = cuota.getChequeraSerie();
        if (serie == null) {
            throw new ChequeraException("La cuota " + chequeraCuotaId + " no tiene chequera asociada");
        }
        var inscripcionFull = personaDatosService.obtenerInscripcionFull(serie.getFacultadId(),
                serie.getPersonaId(), serie.getDocumentoId(), serie.getLectivoId());
        var preferencias = preferenciaPagoDatosService.crearContexto(serie.getFacultadId(),
                serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId());

        String nombreAlumno = Objects.requireNonNull(serie.getPersona()).apellidoNombre();
        String cuerpo = chequeraEmailComposer.cuerpoCuota(nombreAlumno, preferencias, cuota);
        var destinatarios = destinatariosResolver.resolver(serie, inscripcionFull, false);

        correoService.enviar(chequeraCorreoFactory.crear(senderSettings.fromChequera(), destinatarios,
                ASUNTO + nombreAlumno, cuerpo, null, cuota.getFacultadId()));

        return ENVIO_OK;
    }

}
