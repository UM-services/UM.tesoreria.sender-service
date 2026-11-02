package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ActualizarCodigosBarrasUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraCuotaRepository;

@Component
@RequiredArgsConstructor
public class ActualizarCodigosBarrasUseCaseImpl implements ActualizarCodigosBarrasUseCase {

    private final ChequeraCuotaRepository chequeraCuotaRepository;

    @Override
    public void actualizar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        chequeraCuotaRepository.actualizarCodigosBarras(facultadId, tipoChequeraId, chequeraSerieId);
    }

}
