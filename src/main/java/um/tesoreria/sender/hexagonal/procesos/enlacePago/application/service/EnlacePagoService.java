package um.tesoreria.sender.hexagonal.procesos.enlacePago.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.procesos.enlacePago.domain.ports.in.EnviarEnlacePagoUseCase;

@Service
@RequiredArgsConstructor
public class EnlacePagoService {

    private final EnviarEnlacePagoUseCase enviarEnlacePagoUseCase;

    public String enviarCuota(Long chequeraCuotaId) {
        return enviarEnlacePagoUseCase.enviar(chequeraCuotaId);
    }

}
