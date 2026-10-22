package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out;

import java.util.List;
import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;

public interface ChequeraReemplazoRepository {

    Optional<ChequeraSerieReemplazo> findSerieByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

    List<ChequeraCuotaReemplazo> findCuotas(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                            Integer alternativaId);

}
