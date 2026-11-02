package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;

public interface ObtenerChequeraFacturacionElectronicaUseCase {

    Optional<ChequeraFacturacionElectronica> obtenerPorChequeraId(Long chequeraId);

}
