package um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

public interface ActualizarFacturacionElectronicaUseCase {

    FacturacionElectronica actualizar(FacturacionElectronica facturacionElectronica);

}
