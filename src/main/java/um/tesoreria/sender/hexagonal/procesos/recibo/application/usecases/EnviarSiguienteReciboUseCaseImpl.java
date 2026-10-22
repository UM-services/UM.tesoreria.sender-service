package um.tesoreria.sender.hexagonal.procesos.recibo.application.usecases;

import java.text.MessageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.service.FacturacionDatosService;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.EnviarReciboUseCase;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.EnviarSiguienteReciboUseCase;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviarSiguienteReciboUseCaseImpl implements EnviarSiguienteReciboUseCase {

    private final FacturacionDatosService facturacionDatosService;
    private final EnviarReciboUseCase enviarReciboUseCase;

    @Override
    public String enviar() {
        log.debug("Processing EnviarSiguienteReciboUseCaseImpl.enviar()");
        var facturacionElectronica = facturacionDatosService.obtenerSiguientePendiente();
        log.debug("FacturacionElectronica -> {}", facturacionElectronica);
        try {
            return enviarReciboUseCase.enviar(facturacionElectronica.getFacturacionElectronicaId());
        } catch (Exception e) {
            facturacionElectronica.incrementarReintentos();
            facturacionDatosService.actualizar(facturacionElectronica);
            log.info("SendError: {}", facturacionElectronica);
            var chequeraPago = facturacionElectronica.getChequeraPago();
            if (chequeraPago == null) {
                return "ERROR: Problemas de Envío";
            }
            return MessageFormat.format("ERROR: Cuota {0}/{1}/{2}/{3}/{4}/{5} Problemas de Envío",
                    chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(),
                    chequeraPago.getChequeraSerieId(), chequeraPago.getAlternativaId(),
                    chequeraPago.getProductoId(), chequeraPago.getCuotaId());
        }
    }

}
