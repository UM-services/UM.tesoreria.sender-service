package um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.in;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;

public interface ObtenerLectivoAlternativasUseCase {

    List<LectivoAlternativa> obtener(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                     Integer alternativaId);

}
