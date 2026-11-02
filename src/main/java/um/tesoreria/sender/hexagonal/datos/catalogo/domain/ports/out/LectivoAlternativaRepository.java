package um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.out;

import java.util.List;
import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;

public interface LectivoAlternativaRepository {

    List<LectivoAlternativa> findAllByTipo(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                           Integer alternativaId);

    Optional<LectivoAlternativa> findByUnique(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                              Integer productoId, Integer alternativaId);

}
