package um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

public interface GenerarReciboPdfUseCase {

    DocumentoPdf generar(Long facturacionElectronicaId);

}
