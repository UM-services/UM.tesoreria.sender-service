package um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;

public interface ObtenerComprobanteUseCase {

    Comprobante obtener(Integer comprobanteId);

}
