package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarCuotaPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerarCuotaPdfUseCaseImpl implements GenerarCuotaPdfUseCase {

    private final ChequeraDatosService chequeraDatosService;
    private final ChequeraDatosAssembler chequeraDatosAssembler;
    private final ChequeraPdfGenerator chequeraPdfGenerator;

    @Override
    public DocumentoPdf generar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                Integer alternativaId, Integer productoId, Integer cuotaId) {
        var serie = chequeraDatosService.obtenerSerie(facultadId, tipoChequeraId, chequeraSerieId);
        var cuota = chequeraDatosService.obtenerCuotaPorClave(serie.getFacultadId(), serie.getTipoChequeraId(),
                serie.getChequeraSerieId(), productoId, alternativaId, cuotaId);
        if (!cuota.pendienteDePago()) {
            log.debug("La cuota no se puede imprimir.");
            return DocumentoPdf.vacio();
        }
        var datos = chequeraDatosAssembler.armarCuota(serie, cuota);
        return chequeraPdfGenerator.generarCuota(datos);
    }

}
