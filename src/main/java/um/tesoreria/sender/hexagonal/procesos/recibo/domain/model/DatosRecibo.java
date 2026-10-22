package um.tesoreria.sender.hexagonal.procesos.recibo.domain.model;

import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

public record DatosRecibo(FacturacionElectronica facturacionElectronica,
                          Comprobante comprobante,
                          ChequeraPago chequeraPago,
                          ChequeraCuota chequeraCuota,
                          ChequeraSerie chequeraSerie,
                          ChequeraFacturacionElectronica chequeraFacturacionElectronica) {
}
