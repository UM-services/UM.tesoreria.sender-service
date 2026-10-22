package um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;

public interface RegistrarReciboMessageCheckUseCase {

    ReciboMessageCheck registrar(ReciboMessageCheck messageCheck);

}
