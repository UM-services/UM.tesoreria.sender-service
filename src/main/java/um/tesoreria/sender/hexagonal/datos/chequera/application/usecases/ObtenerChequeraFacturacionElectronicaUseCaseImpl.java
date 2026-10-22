package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraFacturacionElectronicaUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraFacturacionElectronicaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;

@Component
@RequiredArgsConstructor
public class ObtenerChequeraFacturacionElectronicaUseCaseImpl implements ObtenerChequeraFacturacionElectronicaUseCase {

    private final ChequeraFacturacionElectronicaRepository chequeraFacturacionElectronicaRepository;

    @Override
    public Optional<ChequeraFacturacionElectronica> obtenerPorChequeraId(Long chequeraId) {
        return chequeraFacturacionElectronicaRepository.findByChequeraId(chequeraId);
    }

}
