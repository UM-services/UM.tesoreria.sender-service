package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.model.BorradorImpresion;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.in.ObtenerBorradorImpresionUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.exception.BorradorNoImprimibleException;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosBorradorChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.GenerarBorradorPdfUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerarBorradorPdfUseCaseImpl implements GenerarBorradorPdfUseCase {

    private final ObtenerBorradorImpresionUseCase obtenerBorradorImpresionUseCase;
    private final ChequeraPdfGenerator chequeraPdfGenerator;

    @Override
    public DocumentoPdf generar(Long borradorId) {
        BorradorImpresion borrador;
        try {
            borrador = obtenerBorradorImpresionUseCase.obtener(borradorId);
        } catch (FeignException e) {
            // El core responde 404/409 cuando el borrador no existe o ya no está ABIERTO: no es un
            // error del servidor, el documento simplemente no se imprime como borrador.
            if (e.status() == 404 || e.status() == 409) {
                throw new BorradorNoImprimibleException("El borrador " + borradorId
                        + " no existe o ya no está abierto");
            }
            throw e;
        }
        if (borrador == null || !borrador.abierto()) {
            throw new BorradorNoImprimibleException("El borrador " + borradorId
                    + " no está abierto: sólo se imprime como borrador mientras está abierto");
        }
        DatosBorradorChequera datos = armar(borrador);
        boolean hayAdeudadas = datos.cuotas().stream()
                .anyMatch(DatosBorradorChequera.CuotaBorrador::adeudada);
        if (!hayAdeudadas) {
            log.debug("Borrador {} sin cuotas adeudadas: no se genera PDF", borradorId);
            return DocumentoPdf.vacio();
        }
        return chequeraPdfGenerator.generarBorradorChequera(datos);
    }

    private DatosBorradorChequera armar(BorradorImpresion borrador) {
        List<DatosBorradorChequera.CuotaBorrador> cuotas = borrador.cuotas().stream()
                .map(cuota -> new DatosBorradorChequera.CuotaBorrador(
                        cuota.productoId(),
                        cuota.productoNombre(),
                        cuota.alternativaId(),
                        cuota.cuotaId(),
                        cuota.mes(),
                        cuota.anio(),
                        cuota.vencimiento1(),
                        cuota.vencimiento2(),
                        cuota.vencimiento3(),
                        cuota.importe1(),
                        cuota.importe2(),
                        cuota.importe3(),
                        cuota.pagado() != null && cuota.pagado() == 1,
                        cuota.baja() != null && cuota.baja() == 1,
                        cuota.compensada() != null && cuota.compensada() == 1))
                .toList();
        return new DatosBorradorChequera(borrador.apellido(), borrador.nombre(), borrador.personaId(),
                borrador.facultadId(), borrador.facultadNombre(), borrador.tipoChequeraId(),
                borrador.tipoChequeraNombre(), borrador.lectivoNombre(), borrador.arancelTipoDescripcion(),
                borrador.alternativaId(), borrador.alternativaTitulo(), borrador.becaPorcentaje(),
                borrador.fecha(), borrador.carrera(), borrador.total(), cuotas);
    }
}
