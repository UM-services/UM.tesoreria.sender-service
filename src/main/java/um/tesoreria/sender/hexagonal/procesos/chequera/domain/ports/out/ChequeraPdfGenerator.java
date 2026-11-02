package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out;

import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequeraReemplazo;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

public interface ChequeraPdfGenerator {

    DocumentoPdf generarChequera(DatosChequera datos, boolean codigoBarras, boolean completa);

    DocumentoPdf generarChequeraReemplazo(DatosChequeraReemplazo datos, boolean completa);

    DocumentoPdf generarCuota(DatosCuota datos);

}
