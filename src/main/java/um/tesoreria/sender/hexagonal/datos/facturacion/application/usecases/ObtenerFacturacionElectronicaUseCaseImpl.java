package um.tesoreria.sender.hexagonal.datos.facturacion.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.exception.FacturacionDatosException;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ObtenerFacturacionElectronicaUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.FacturacionElectronicaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

@Component
@RequiredArgsConstructor
public class ObtenerFacturacionElectronicaUseCaseImpl implements ObtenerFacturacionElectronicaUseCase {

    private final FacturacionElectronicaRepository facturacionElectronicaRepository;

    @Override
    public FacturacionElectronica obtener(Long facturacionElectronicaId) {
        return facturacionElectronicaRepository.findById(facturacionElectronicaId)
                .orElseThrow(() -> new FacturacionDatosException(
                        "No se encontró la facturación electrónica " + facturacionElectronicaId));
    }

}
