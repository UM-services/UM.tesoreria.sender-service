package um.tesoreria.sender.hexagonal.procesos.recibo.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.recibo.application.service.ReciboDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in.GenerarReciboPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.out.ReciboPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@Component
@RequiredArgsConstructor
public class GenerarReciboPdfUseCaseImpl implements GenerarReciboPdfUseCase {

    private final ReciboDatosAssembler reciboDatosAssembler;
    private final ReciboPdfGenerator reciboPdfGenerator;

    @Override
    public DocumentoPdf generar(Long facturacionElectronicaId) {
        var datos = reciboDatosAssembler.armar(facturacionElectronicaId, null);
        return reciboPdfGenerator.generar(facturacionElectronicaId, datos);
    }

}
