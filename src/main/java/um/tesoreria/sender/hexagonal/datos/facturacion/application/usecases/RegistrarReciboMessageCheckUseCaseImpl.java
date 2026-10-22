package um.tesoreria.sender.hexagonal.datos.facturacion.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.RegistrarReciboMessageCheckUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.ReciboMessageCheckRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;

@Component
@RequiredArgsConstructor
public class RegistrarReciboMessageCheckUseCaseImpl implements RegistrarReciboMessageCheckUseCase {

    private final ReciboMessageCheckRepository reciboMessageCheckRepository;

    @Override
    public ReciboMessageCheck registrar(ReciboMessageCheck messageCheck) {
        return reciboMessageCheckRepository.registrar(messageCheck);
    }

}
