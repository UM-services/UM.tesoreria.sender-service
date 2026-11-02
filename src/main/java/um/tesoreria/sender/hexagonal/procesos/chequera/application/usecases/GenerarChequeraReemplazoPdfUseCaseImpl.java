package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarChequeraReemplazoPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerarChequeraReemplazoPdfUseCaseImpl implements GenerarChequeraReemplazoPdfUseCase {

    private final ChequeraDatosService chequeraDatosService;
    private final ChequeraDatosAssembler chequeraDatosAssembler;
    private final ChequeraPdfGenerator chequeraPdfGenerator;

    @Override
    public DocumentoPdf generar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                Integer alternativaId, boolean completa) {
        var serie = chequeraDatosService.obtenerSerieReemplazo(facultadId, tipoChequeraId, chequeraSerieId);
        var cuotas = chequeraDatosService.obtenerCuotasReemplazo(serie.getFacultadId(), serie.getTipoChequeraId(),
                serie.getChequeraSerieId(), serie.getAlternativaId());
        boolean hayCuotas = cuotas.stream().anyMatch(ChequeraCuotaReemplazo::pendienteDePago);
        if (!hayCuotas) {
            log.debug("No hay nada para imprimir.");
            return DocumentoPdf.vacio();
        }
        var datos = chequeraDatosAssembler.armarReemplazo(serie, cuotas);
        return chequeraPdfGenerator.generarChequeraReemplazo(datos, completa);
    }

}
