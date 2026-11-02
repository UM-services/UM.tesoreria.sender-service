package um.tesoreria.sender.hexagonal.datos.catalogo.application.usecases;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.in.ObtenerLectivoAlternativasUseCase;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.out.LectivoAlternativaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;

@Component
@RequiredArgsConstructor
public class ObtenerLectivoAlternativasUseCaseImpl implements ObtenerLectivoAlternativasUseCase {

    private final LectivoAlternativaRepository lectivoAlternativaRepository;

    @Override
    public List<LectivoAlternativa> obtener(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                            Integer alternativaId) {
        return lectivoAlternativaRepository.findAllByTipo(facultadId, lectivoId, tipoChequeraId, alternativaId);
    }

}
