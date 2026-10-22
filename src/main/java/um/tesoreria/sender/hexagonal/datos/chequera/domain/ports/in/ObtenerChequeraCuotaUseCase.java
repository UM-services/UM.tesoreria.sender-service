package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;

public interface ObtenerChequeraCuotaUseCase {

    ChequeraCuota obtenerPorId(Long chequeraCuotaId);

    ChequeraCuota obtenerPorClave(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                  Integer productoId, Integer alternativaId, Integer cuotaId);

}
