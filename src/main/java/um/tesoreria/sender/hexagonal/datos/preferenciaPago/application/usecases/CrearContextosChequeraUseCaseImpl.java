package um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.usecases;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in.CrearContextosChequeraUseCase;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.out.PreferenciaPagoRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

@Component
@RequiredArgsConstructor
public class CrearContextosChequeraUseCaseImpl implements CrearContextosChequeraUseCase {

    private final PreferenciaPagoRepository preferenciaPagoRepository;

    @Override
    public List<PreferenciaPago> crear(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                       Integer alternativaId) {
        return preferenciaPagoRepository.crearContexto(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

}
