package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;

public interface ObtenerChequeraPagoUseCase {

    ChequeraPago obtener(Long chequeraPagoId);

}
