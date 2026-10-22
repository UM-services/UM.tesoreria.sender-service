package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

public interface GenerarCuotaPdfUseCase {

    DocumentoPdf generar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                         Integer productoId, Integer cuotaId);

}
