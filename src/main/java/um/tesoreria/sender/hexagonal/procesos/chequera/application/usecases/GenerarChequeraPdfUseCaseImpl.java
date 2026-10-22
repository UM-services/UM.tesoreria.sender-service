package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service.PreferenciaPagoDatosService;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.ResultadoChequeraPdf;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudChequeraPdf;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarChequeraPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerarChequeraPdfUseCaseImpl implements GenerarChequeraPdfUseCase {

    private final ChequeraDatosService chequeraDatosService;
    private final PreferenciaPagoDatosService preferenciaPagoDatosService;
    private final ChequeraDatosAssembler chequeraDatosAssembler;
    private final ChequeraPdfGenerator chequeraPdfGenerator;

    @Override
    public ResultadoChequeraPdf generar(SolicitudChequeraPdf solicitud) {
        log.debug("\n\nProcessing GenerarChequeraPdfUseCaseImpl.generar\n\n");
        var serie = chequeraDatosService.obtenerSerie(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId());
        List<PreferenciaPago> preferencias = solicitud.bulk()
                ? preferenciaPagoDatosService.crearContextoBulk(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId(), solicitud.alternativaId())
                : preferenciaPagoDatosService.crearContexto(solicitud.facultadId(), solicitud.tipoChequeraId(),
                solicitud.chequeraSerieId(), solicitud.alternativaId());
        preferencias.forEach(preferencia -> log.debug("\n\nPreference -> {}\n\n", preferencia));

        boolean hayCuotas = preferencias.stream()
                .map(PreferenciaPago::getChequeraCuota)
                .filter(cuota -> cuota != null)
                .anyMatch(ChequeraCuota::pendienteDePago);
        if (!hayCuotas) {
            log.debug("No hay nada para imprimir.");
            return new ResultadoChequeraPdf(DocumentoPdf.vacio(), serie, preferencias);
        }

        var datos = chequeraDatosAssembler.armar(serie, preferencias);
        var documento = chequeraPdfGenerator.generarChequera(datos, solicitud.codigoBarras(), solicitud.completa());
        return new ResultadoChequeraPdf(documento, serie, preferencias);
    }

}
