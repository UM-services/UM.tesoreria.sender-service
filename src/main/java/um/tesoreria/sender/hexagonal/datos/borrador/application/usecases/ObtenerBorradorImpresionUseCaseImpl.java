package um.tesoreria.sender.hexagonal.datos.borrador.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.model.BorradorImpresion;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.in.ObtenerBorradorImpresionUseCase;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.out.BorradorImpresionRepository;

@Component
@RequiredArgsConstructor
public class ObtenerBorradorImpresionUseCaseImpl implements ObtenerBorradorImpresionUseCase {

    private final BorradorImpresionRepository repository;

    @Override
    public BorradorImpresion obtener(Long borradorId) {
        return repository.obtener(borradorId);
    }
}
