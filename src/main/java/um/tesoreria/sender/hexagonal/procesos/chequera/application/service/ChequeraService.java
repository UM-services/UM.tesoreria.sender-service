package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudChequeraPdf;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.EnviarChequeraUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.EnviarCuotaUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarChequeraPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarChequeraReemplazoPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarCuotaPdfUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@Service
@RequiredArgsConstructor
public class ChequeraService {

    private final GenerarChequeraPdfUseCase generarChequeraPdfUseCase;
    private final GenerarChequeraReemplazoPdfUseCase generarChequeraReemplazoPdfUseCase;
    private final GenerarCuotaPdfUseCase generarCuotaPdfUseCase;
    private final EnviarChequeraUseCase enviarChequeraUseCase;
    private final EnviarCuotaUseCase enviarCuotaUseCase;

    public DocumentoPdf generarChequeraPdf(SolicitudChequeraPdf solicitud) {
        return generarChequeraPdfUseCase.generar(solicitud).documento();
    }

    public DocumentoPdf generarChequeraReemplazoPdf(Integer facultadId, Integer tipoChequeraId,
                                                    Long chequeraSerieId, Integer alternativaId, boolean completa) {
        return generarChequeraReemplazoPdfUseCase.generar(facultadId, tipoChequeraId, chequeraSerieId, alternativaId,
                completa);
    }

    public DocumentoPdf generarCuotaPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                        Integer alternativaId, Integer productoId, Integer cuotaId) {
        return generarCuotaPdfUseCase.generar(facultadId, tipoChequeraId, chequeraSerieId, alternativaId, productoId,
                cuotaId);
    }

    public String enviarChequera(SolicitudEnvioChequera solicitud) {
        return enviarChequeraUseCase.enviar(solicitud);
    }

    public String enviarCuota(Long chequeraCuotaId) {
        return enviarCuotaUseCase.enviar(chequeraCuotaId);
    }

}
