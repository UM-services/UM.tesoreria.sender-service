package um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;

public interface MercadoPagoContextRepository {

    Optional<MercadoPagoContext> findActivoByChequeraCuotaId(Long chequeraCuotaId);

}
