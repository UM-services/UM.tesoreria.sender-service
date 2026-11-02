package um.tesoreria.sender.hexagonal.datos.facturacion.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ActualizarFacturacionElectronicaUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.FacturacionElectronicaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

@Component
@RequiredArgsConstructor
public class ActualizarFacturacionElectronicaUseCaseImpl implements ActualizarFacturacionElectronicaUseCase {

    private final FacturacionElectronicaRepository facturacionElectronicaRepository;

    @Override
    public FacturacionElectronica actualizar(FacturacionElectronica facturacionElectronica) {
        return facturacionElectronicaRepository.update(facturacionElectronica);
    }

}
