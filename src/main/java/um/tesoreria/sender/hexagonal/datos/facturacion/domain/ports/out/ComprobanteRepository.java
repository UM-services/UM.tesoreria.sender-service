package um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;

public interface ComprobanteRepository {

    Optional<Comprobante> findByComprobanteId(Integer comprobanteId);

}
