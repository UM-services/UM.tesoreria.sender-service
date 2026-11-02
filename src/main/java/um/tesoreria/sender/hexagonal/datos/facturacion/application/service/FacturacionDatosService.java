package um.tesoreria.sender.hexagonal.datos.facturacion.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ActualizarFacturacionElectronicaUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ObtenerComprobanteUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ObtenerFacturacionElectronicaUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ObtenerSiguienteFacturacionPendienteUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.RegistrarReciboMessageCheckUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;

@Service
@RequiredArgsConstructor
public class FacturacionDatosService {

    private final ObtenerFacturacionElectronicaUseCase obtenerFacturacionElectronicaUseCase;
    private final ObtenerSiguienteFacturacionPendienteUseCase obtenerSiguienteFacturacionPendienteUseCase;
    private final ActualizarFacturacionElectronicaUseCase actualizarFacturacionElectronicaUseCase;
    private final ObtenerComprobanteUseCase obtenerComprobanteUseCase;
    private final RegistrarReciboMessageCheckUseCase registrarReciboMessageCheckUseCase;

    public FacturacionElectronica obtener(Long facturacionElectronicaId) {
        return obtenerFacturacionElectronicaUseCase.obtener(facturacionElectronicaId);
    }

    public FacturacionElectronica obtenerSiguientePendiente() {
        return obtenerSiguienteFacturacionPendienteUseCase.obtener();
    }

    public FacturacionElectronica actualizar(FacturacionElectronica facturacionElectronica) {
        return actualizarFacturacionElectronicaUseCase.actualizar(facturacionElectronica);
    }

    public Comprobante obtenerComprobante(Integer comprobanteId) {
        return obtenerComprobanteUseCase.obtener(comprobanteId);
    }

    public ReciboMessageCheck registrarMessageCheck(ReciboMessageCheck messageCheck) {
        return registrarReciboMessageCheckUseCase.registrar(messageCheck);
    }

}
