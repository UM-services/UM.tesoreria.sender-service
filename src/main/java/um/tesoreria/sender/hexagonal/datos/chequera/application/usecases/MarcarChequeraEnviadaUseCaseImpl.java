package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.MarcarChequeraEnviadaUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraSerieRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;

@Component
@RequiredArgsConstructor
public class MarcarChequeraEnviadaUseCaseImpl implements MarcarChequeraEnviadaUseCase {

    private final ChequeraSerieRepository chequeraSerieRepository;

    @Override
    public ChequeraSerie marcar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return chequeraSerieRepository.markSent(facultadId, tipoChequeraId, chequeraSerieId);
    }

}
