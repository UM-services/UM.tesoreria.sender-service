package um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.out;

import um.tesoreria.sender.hexagonal.procesos.recibo.domain.model.DatosRecibo;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

public interface ReciboPdfGenerator {

    DocumentoPdf generar(Long facturacionElectronicaId, DatosRecibo datos);

}
