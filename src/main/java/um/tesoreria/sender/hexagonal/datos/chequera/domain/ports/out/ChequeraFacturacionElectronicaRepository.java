package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;

public interface ChequeraFacturacionElectronicaRepository {

    Optional<ChequeraFacturacionElectronica> findByChequeraId(Long chequeraId);

}
