package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;

public interface ChequeraCuotaRepository {

    Optional<ChequeraCuota> findByChequeraCuotaId(Long chequeraCuotaId);

    Optional<ChequeraCuota> findByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                         Integer productoId, Integer alternativaId, Integer cuotaId);

    void actualizarCodigosBarras(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

}
