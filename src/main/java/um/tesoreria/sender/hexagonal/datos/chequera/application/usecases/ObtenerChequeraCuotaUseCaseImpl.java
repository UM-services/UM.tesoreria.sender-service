package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.exception.ChequeraDatosException;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraCuotaUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraCuotaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;

@Component
@RequiredArgsConstructor
public class ObtenerChequeraCuotaUseCaseImpl implements ObtenerChequeraCuotaUseCase {

    private final ChequeraCuotaRepository chequeraCuotaRepository;

    @Override
    public ChequeraCuota obtenerPorId(Long chequeraCuotaId) {
        return chequeraCuotaRepository.findByChequeraCuotaId(chequeraCuotaId)
                .orElseThrow(() -> new ChequeraDatosException("No se encontró la cuota " + chequeraCuotaId));
    }

    @Override
    public ChequeraCuota obtenerPorClave(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                         Integer productoId, Integer alternativaId, Integer cuotaId) {
        return chequeraCuotaRepository.findByUnique(facultadId, tipoChequeraId, chequeraSerieId, productoId,
                        alternativaId, cuotaId)
                .orElseThrow(() -> new ChequeraDatosException("No se encontró la cuota de la chequera "
                        + facultadId + "/" + tipoChequeraId + "/" + chequeraSerieId));
    }

}
