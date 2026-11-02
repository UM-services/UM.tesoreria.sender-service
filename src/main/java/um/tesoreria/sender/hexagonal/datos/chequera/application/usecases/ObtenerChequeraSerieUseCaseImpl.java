package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.exception.ChequeraDatosException;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraSerieUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraSerieRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;

@Component
@RequiredArgsConstructor
public class ObtenerChequeraSerieUseCaseImpl implements ObtenerChequeraSerieUseCase {

    private final ChequeraSerieRepository chequeraSerieRepository;

    @Override
    public ChequeraSerie obtener(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return chequeraSerieRepository.findByUnique(facultadId, tipoChequeraId, chequeraSerieId)
                .orElseThrow(() -> new ChequeraDatosException(
                        "No se encontró la chequera " + facultadId + "/" + tipoChequeraId + "/" + chequeraSerieId));
    }

}
