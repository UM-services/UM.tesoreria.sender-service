package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.exception.ChequeraDatosException;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraReemplazoUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraReemplazoRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;

@Component
@RequiredArgsConstructor
public class ObtenerChequeraReemplazoUseCaseImpl implements ObtenerChequeraReemplazoUseCase {

    private final ChequeraReemplazoRepository chequeraReemplazoRepository;

    @Override
    public ChequeraSerieReemplazo obtenerSerie(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return chequeraReemplazoRepository.findSerieByUnique(facultadId, tipoChequeraId, chequeraSerieId)
                .orElseThrow(() -> new ChequeraDatosException("No se encontró la chequera de reemplazo "
                        + facultadId + "/" + tipoChequeraId + "/" + chequeraSerieId));
    }

    @Override
    public List<ChequeraCuotaReemplazo> obtenerCuotas(Integer facultadId, Integer tipoChequeraId,
                                                      Long chequeraSerieId, Integer alternativaId) {
        return chequeraReemplazoRepository.findCuotas(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

}
