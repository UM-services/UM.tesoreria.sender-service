package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;

public interface ChequeraPagoRepository {

    Optional<ChequeraPago> findByChequeraPagoId(Long chequeraPagoId);

}
