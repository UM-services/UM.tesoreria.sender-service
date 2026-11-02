package um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;

public interface ObtenerLectivoAlternativaUseCase {

    LectivoAlternativa obtener(Integer facultadId, Integer lectivoId, Integer tipoChequeraId, Integer productoId,
                               Integer alternativaId);

}
