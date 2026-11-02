package um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;

public interface ObtenerContextoActivoUseCase {

    MercadoPagoContext obtener(Long chequeraCuotaId);

}
