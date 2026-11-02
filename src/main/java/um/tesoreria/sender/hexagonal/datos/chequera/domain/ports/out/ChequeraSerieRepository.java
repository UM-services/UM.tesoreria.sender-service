package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;

public interface ChequeraSerieRepository {

    Optional<ChequeraSerie> findByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

    ChequeraSerie markSent(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

}
