package um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.in;

import um.tesoreria.sender.hexagonal.datos.borrador.domain.model.BorradorImpresion;

public interface ObtenerBorradorImpresionUseCase {

    BorradorImpresion obtener(Long borradorId);
}
