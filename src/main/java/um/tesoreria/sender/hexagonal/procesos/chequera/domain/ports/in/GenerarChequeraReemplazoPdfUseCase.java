package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

public interface GenerarChequeraReemplazoPdfUseCase {

    DocumentoPdf generar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                         boolean completa);

}
