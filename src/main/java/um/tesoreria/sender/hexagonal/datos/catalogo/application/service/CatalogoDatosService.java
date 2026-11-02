package um.tesoreria.sender.hexagonal.datos.catalogo.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.in.ObtenerLectivoAlternativaUseCase;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.in.ObtenerLectivoAlternativasUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;

@Service
@RequiredArgsConstructor
public class CatalogoDatosService {

    private final ObtenerLectivoAlternativasUseCase obtenerLectivoAlternativasUseCase;
    private final ObtenerLectivoAlternativaUseCase obtenerLectivoAlternativaUseCase;

    public List<LectivoAlternativa> obtenerAlternativas(Integer facultadId, Integer lectivoId,
                                                        Integer tipoChequeraId, Integer alternativaId) {
        return obtenerLectivoAlternativasUseCase.obtener(facultadId, lectivoId, tipoChequeraId, alternativaId);
    }

    public LectivoAlternativa obtenerAlternativa(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                                 Integer productoId, Integer alternativaId) {
        return obtenerLectivoAlternativaUseCase.obtener(facultadId, lectivoId, tipoChequeraId, productoId,
                alternativaId);
    }

}
