package um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

public interface FacturacionElectronicaRepository {

    Optional<FacturacionElectronica> findById(Long facturacionElectronicaId);

    Optional<FacturacionElectronica> findNextPendiente();

    FacturacionElectronica update(FacturacionElectronica facturacionElectronica);

}
