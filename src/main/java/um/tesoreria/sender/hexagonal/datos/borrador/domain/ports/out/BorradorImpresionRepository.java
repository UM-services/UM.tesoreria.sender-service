package um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.out;

import um.tesoreria.sender.hexagonal.datos.borrador.domain.model.BorradorImpresion;

public interface BorradorImpresionRepository {

    BorradorImpresion obtener(Long borradorId);
}
