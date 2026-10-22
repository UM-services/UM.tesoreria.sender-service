package um.tesoreria.sender.hexagonal.datos.facturacion.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.exception.FacturacionDatosException;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ObtenerSiguienteFacturacionPendienteUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.FacturacionElectronicaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

@Component
@RequiredArgsConstructor
public class ObtenerSiguienteFacturacionPendienteUseCaseImpl implements ObtenerSiguienteFacturacionPendienteUseCase {

    private final FacturacionElectronicaRepository facturacionElectronicaRepository;

    @Override
    public FacturacionElectronica obtener() {
        return facturacionElectronicaRepository.findNextPendiente()
                .orElseThrow(() -> new FacturacionDatosException("No hay facturaciones pendientes de envío"));
    }

}
