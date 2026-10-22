package um.tesoreria.sender.hexagonal.procesos.recibo.application.usecases;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.service.FacturacionDatosService;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.application.service.ValidacionEmailService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.exception.CorreoException;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.AdjuntoCorreo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.procesos.recibo.application.service.ReciboDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.model.DatosRecibo;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.EnviarReciboUseCase;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.out.ReciboPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviarReciboUseCaseImpl implements EnviarReciboUseCase {

    private static final String TESTING_EMAIL = "daniel.quinterospinto@gmail.com";
    private static final String REPLY_TO = "no-reply@um.edu.ar";
    private static final String ASUNTO = "Envío Automático de Recibo -> ";

    private final ReciboDatosAssembler reciboDatosAssembler;
    private final ReciboPdfGenerator reciboPdfGenerator;
    private final FacturacionDatosService facturacionDatosService;
    private final ValidacionEmailService validacionEmailService;
    private final CorreoService correoService;
    private final SenderSettings senderSettings;

    @Override
    public String enviar(Long facturacionElectronicaId) {
        log.debug("\n\nProcessing EnviarReciboUseCaseImpl.enviar\n\n");
        var datos = reciboDatosAssembler.armar(facturacionElectronicaId, null);
        var facturacionElectronica = datos.facturacionElectronica();
        String cuotaString = cuotaString(datos);

        var documento = reciboPdfGenerator.generar(facturacionElectronicaId, datos);
        log.info("\n\nFilename_recibo -> {}\n\n", documento.ruta());
        if (documento.estaVacio()) {
            log.debug("Sin Recibo para ENVIAR");
            return errorConReintento(datos, cuotaString, "Sin Recibo para ENVIAR");
        }

        Domicilio domicilio = datos.chequeraSerie().getDomicilio();
        if (domicilio == null) {
            log.debug("Sin Domicilio para ENVIAR");
            return errorConReintento(datos, cuotaString, "Sin Recibo para ENVIAR");
        }
        if (vacio(domicilio.getEmailPersonal()) && vacio(domicilio.getEmailInstitucional())) {
            log.debug("Sin e-mails para ENVIAR");
            return errorConReintento(datos, cuotaString, "Sin e-mails para ENVIAR");
        }

        String cuerpo = "Estimad@ Estudiante:" + (char) 10
                + (char) 10
                + "Le enviamos como archivo adjunto su recibo." + (char) 10
                + (char) 10
                + "Atentamente." + (char) 10
                + (char) 10
                + "Universidad de Mendoza" + (char) 10
                + (char) 10
                + (char) 10
                + "Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída."
                + (char) 10;

        List<String> destinatarios = destinatarios(datos, domicilio);
        if (destinatarios.isEmpty()) {
            log.debug("Sin e-mails para ENVIAR");
            return "ERROR: Sin e-mails para ENVIAR";
        }

        Correo correo = Correo.builder()
                .from(senderSettings.fromRecibo())
                .para(destinatarios)
                .replyTo(REPLY_TO)
                .asunto(ASUNTO + documento.ruta())
                .cuerpo(cuerpo)
                .html(false)
                .adjuntos(List.of(new AdjuntoCorreo(documento.ruta(), documento.ruta())))
                .build();

        try {
            correoService.enviar(correo);
        } catch (CorreoException e) {
            if (e.getTipo() == CorreoException.Tipo.DIRECCION_INVALIDA) {
                log.error("Error al enviar el correo por dirección inválida: {}", e.getMessage());
                return errorConReintento(datos, cuotaString,
                        "No pudo ENVIARSE. Dirección de correo inválida.");
            }
            log.debug("No pudo ENVIARSE");
            return errorConReintento(datos, cuotaString, "No pudo ENVIARSE");
        }

        log.debug("Mail enviado");
        facturacionElectronica.marcarEnviada();
        facturacionDatosService.actualizar(facturacionElectronica);

        registrarMessageCheck(datos);
        return MessageFormat.format("{0} Envío de Correo Ok!!!", cuotaString);
    }

    private List<String> destinatarios(DatosRecibo datos, Domicilio domicilio) {
        List<String> addresses = new ArrayList<>();
        if (senderSettings.testing()) {
            log.debug("Testing -> {}", TESTING_EMAIL);
            addresses.add(TESTING_EMAIL);
            return addresses;
        }
        if (!vacio(domicilio.getEmailPersonal())
                && validacionEmailService.validar(domicilio.getEmailPersonal())) {
            addresses.add(domicilio.getEmailPersonal());
            log.debug("adding personal email -> {}", domicilio.getEmailPersonal());
        }
        if (!vacio(domicilio.getEmailInstitucional())
                && validacionEmailService.validar(domicilio.getEmailInstitucional())) {
            addresses.add(domicilio.getEmailInstitucional());
            log.debug("adding institutional email -> {}", domicilio.getEmailInstitucional());
        }
        String emailChequera = datos.chequeraFacturacionElectronica().getEmail();
        if (!vacio(emailChequera) && validacionEmailService.validar(emailChequera)) {
            addresses.add(emailChequera);
            log.debug("adding chequera email -> {}", emailChequera);
        }
        return addresses;
    }

    private String errorConReintento(DatosRecibo datos, String cuotaString, String motivo) {
        var facturacionElectronica = datos.facturacionElectronica();
        facturacionElectronica.incrementarReintentos();
        facturacionDatosService.actualizar(facturacionElectronica);
        return MessageFormat.format("ERROR: {0} " + motivo, cuotaString);
    }

    private void registrarMessageCheck(DatosRecibo datos) {
        var pago = datos.chequeraPago();
        facturacionDatosService.registrarMessageCheck(ReciboMessageCheck.builder()
                .reciboMessageCheckId(UUID.randomUUID())
                .facturacionElectronicaId(datos.facturacionElectronica().getFacturacionElectronicaId())
                .chequeraPagoId(datos.facturacionElectronica().getChequeraPagoId())
                .facultadId(pago.getFacultadId())
                .tipoChequeraId(pago.getTipoChequeraId())
                .chequeraSerieId(pago.getChequeraSerieId())
                .productoId(pago.getProductoId())
                .alternativaId(pago.getAlternativaId())
                .cuotaId(pago.getCuotaId())
                .build());
    }

    private String cuotaString(DatosRecibo datos) {
        var pago = datos.chequeraPago();
        return MessageFormat.format("Recibo de Cuota {0}/{1}/{2}/{3}/{4}/{5}", pago.getFacultadId(),
                pago.getTipoChequeraId(), pago.getChequeraSerieId(), pago.getAlternativaId(), pago.getProductoId(),
                pago.getCuotaId());
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isEmpty();
    }

}
