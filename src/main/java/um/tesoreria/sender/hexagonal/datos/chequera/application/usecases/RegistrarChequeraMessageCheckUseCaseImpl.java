package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.RegistrarChequeraMessageCheckUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraMessageCheckRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;

@Component
@RequiredArgsConstructor
public class RegistrarChequeraMessageCheckUseCaseImpl implements RegistrarChequeraMessageCheckUseCase {

    private final ChequeraMessageCheckRepository chequeraMessageCheckRepository;

    @Override
    public ChequeraMessageCheck registrar(ChequeraMessageCheck messageCheck) {
        return chequeraMessageCheckRepository.registrar(messageCheck);
    }

}
