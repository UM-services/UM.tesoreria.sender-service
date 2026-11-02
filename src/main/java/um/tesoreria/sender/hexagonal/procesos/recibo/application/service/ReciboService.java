package um.tesoreria.sender.hexagonal.procesos.recibo.application.service;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.EnviarReciboUseCase;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.EnviarSiguienteReciboUseCase;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.GenerarReciboPdfUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReciboService {

    private final GenerarReciboPdfUseCase generarReciboPdfUseCase;
    private final EnviarReciboUseCase enviarReciboUseCase;
    private final EnviarSiguienteReciboUseCase enviarSiguienteReciboUseCase;

    public DocumentoPdf generarPdf(Long facturacionElectronicaId) {
        return generarReciboPdfUseCase.generar(facturacionElectronicaId);
    }

    public String enviar(Long facturacionElectronicaId) {
        return enviarReciboUseCase.enviar(facturacionElectronicaId);
    }

    @Async
    public CompletableFuture<String> enviarAsync(Long facturacionElectronicaId) {
        log.info("Iniciando procesamiento asíncrono de envío de recibo ID: {}", facturacionElectronicaId);
        return CompletableFuture.completedFuture(enviarReciboUseCase.enviar(facturacionElectronicaId));
    }

    public String enviarSiguiente() {
        return enviarSiguienteReciboUseCase.enviar();
    }

}
