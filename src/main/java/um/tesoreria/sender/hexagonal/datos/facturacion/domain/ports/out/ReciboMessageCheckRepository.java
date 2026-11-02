package um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out;

import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;

public interface ReciboMessageCheckRepository {

    ReciboMessageCheck registrar(ReciboMessageCheck messageCheck);

}
