package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.persona.application.service.PersonaDatosService;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service.PreferenciaPagoDatosService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraCorreoFactory;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraEmailComposer;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.DestinatariosResolver;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.EnviarChequeraUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviarChequeraUseCaseImpl implements EnviarChequeraUseCase {

    private static final String ASUNTO = "Recordatorio - Pago cuota UM -> ";
    private static final String SIN_CUOTAS = "\n\nERROR: Sin CUOTAS para ENVIAR\n\n";
    private static final String ENVIO_OK = "\n\nEnvío de Chequera Ok!!!\n\n";

    private final ChequeraDatosService chequeraDatosService;
    private final PersonaDatosService personaDatosService;
    private final PreferenciaPagoDatosService preferenciaPagoDatosService;
    private final ChequeraDatosAssembler chequeraDatosAssembler;
    private final ChequeraPdfGenerator chequeraPdfGenerator;
    private final ChequeraEmailComposer chequeraEmailComposer;
    private final DestinatariosResolver destinatariosResolver;
    private final ChequeraCorreoFactory chequeraCorreoFactory;
    private final CorreoService correoService;
    private final SenderSettings senderSettings;

    @Override
    public String enviar(SolicitudEnvioChequera solicitud) {
        log.debug("\n\nSending chequera for facultadId: {}, tipoChequeraId: {}, chequeraSerieId: {}, "
                        + "alternativaId: {}, copiaInformes: {}, codigoBarras: {}, incluyeMatricula: {}\n\n",
                solicitud.facultadId(), solicitud.tipoChequeraId(), solicitud.chequeraSerieId(),
                solicitud.alternativaId(), solicitud.copiaInformes(), solicitud.codigoBarras(),
                solicitud.incluyeMatricula());

        if (solicitud.actualizarCodigosBarras()) {
            chequeraDatosService.actualizarCodigosBarras(solicitud.facultadId(), solicitud.tipoChequeraId(),
                    solicitud.chequeraSerieId());
        }
        var serie = chequeraDatosService.obtenerSerie(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId());
        var inscripcionFull = personaDatosService.obtenerInscripcionFull(solicitud.facultadId(),
                serie.getPersonaId(), serie.getDocumentoId(), serie.getLectivoId());
        List<PreferenciaPago> preferencias = solicitud.bulk()
                ? preferenciaPagoDatosService.crearContextoBulk(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId(), solicitud.alternativaId())
                : preferenciaPagoDatosService.crearContexto(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId(), solicitud.alternativaId());

        boolean hayCuotas = preferencias.stream()
                .map(PreferenciaPago::getChequeraCuota)
                .filter(cuota -> cuota != null)
                .anyMatch(ChequeraCuota::pendienteDePago);
        if (!hayCuotas) {
            return SIN_CUOTAS;
        }

        var datos = chequeraDatosAssembler.armar(serie, preferencias);
        var documento = chequeraPdfGenerator.generarChequera(datos, solicitud.codigoBarras(), false);
        log.debug("Chequera enviada - filenameChequera -> {}", documento.ruta());
        if (documento.estaVacio()) {
            return SIN_CUOTAS;
        }

        String nombreAlumno = Objects.requireNonNull(serie.getPersona()).apellidoNombre();
        String cuerpo = chequeraEmailComposer.cuerpoChequera(nombreAlumno, preferencias, serie);
        var destinatarios = destinatariosResolver.resolver(serie, inscripcionFull, solicitud.copiaInformes());

        correoService.enviar(chequeraCorreoFactory.crear(senderSettings.fromChequera(), destinatarios,
                ASUNTO + nombreAlumno, cuerpo, documento.ruta(), solicitud.facultadId()));

        marcarEnviada(solicitud);
        return ENVIO_OK;
    }

    private void marcarEnviada(SolicitudEnvioChequera solicitud) {
        var serie = chequeraDatosService.marcarEnviada(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId());
        chequeraDatosService.registrarMessageCheck(ChequeraMessageCheck.builder()
                .chequeraMessageCheckId(UUID.randomUUID())
                .facultadId(serie.getFacultadId())
                .tipoChequeraId(serie.getTipoChequeraId())
                .chequeraSerieId(serie.getChequeraSerieId())
                .build());
    }

}
