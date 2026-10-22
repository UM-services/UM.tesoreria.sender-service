package um.tesoreria.sender.hexagonal.datos.catalogo.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.catalogo.application.exception.CatalogoDatosException;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.in.ObtenerLectivoAlternativaUseCase;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.out.LectivoAlternativaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;

@Component
@RequiredArgsConstructor
public class ObtenerLectivoAlternativaUseCaseImpl implements ObtenerLectivoAlternativaUseCase {

    private final LectivoAlternativaRepository lectivoAlternativaRepository;

    @Override
    public LectivoAlternativa obtener(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                      Integer productoId, Integer alternativaId) {
        return lectivoAlternativaRepository.findByUnique(facultadId, lectivoId, tipoChequeraId, productoId,
                        alternativaId)
                .orElseThrow(() -> new CatalogoDatosException("No se encontró la alternativa del producto "
                        + productoId));
    }

}
