package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;

public interface ObtenerChequeraSerieUseCase {

    ChequeraSerie obtener(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

}
