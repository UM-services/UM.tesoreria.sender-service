package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.model.BorradorImpresion;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.in.ObtenerBorradorImpresionUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.exception.BorradorNoImprimibleException;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosBorradorChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@ExtendWith(MockitoExtension.class)
class GenerarBorradorPdfUseCaseImplTest {

    @Mock
    private ObtenerBorradorImpresionUseCase obtenerBorradorImpresionUseCase;
    @Mock
    private ChequeraPdfGenerator chequeraPdfGenerator;

    @InjectMocks
    private GenerarBorradorPdfUseCaseImpl useCase;

    @Test
    void generaElPdfDelBorradorAbierto() {
        when(obtenerBorradorImpresionUseCase.obtener(7L)).thenReturn(borrador("ABIERTO", cuota(false, false, false)));
        when(chequeraPdfGenerator.generarBorradorChequera(any())).thenReturn(DocumentoPdf.de("/tmp/x.pdf"));

        DocumentoPdf documento = useCase.generar(7L);

        assertTrue(!documento.estaVacio());
        verify(chequeraPdfGenerator).generarBorradorChequera(any(DatosBorradorChequera.class));
    }

    @Test
    void noImprimeUnBorradorEmitidoODescartado() {
        when(obtenerBorradorImpresionUseCase.obtener(7L)).thenReturn(borrador("EMITIDO", cuota(false, false, false)));

        assertThrows(BorradorNoImprimibleException.class, () -> useCase.generar(7L));
        verify(chequeraPdfGenerator, never()).generarBorradorChequera(any());
    }

    @Test
    void noGeneraPdfCuandoNoHayCuotasAdeudadas() {
        when(obtenerBorradorImpresionUseCase.obtener(7L)).thenReturn(borrador("ABIERTO",
                cuota(true, false, false), cuota(false, false, true), cuota(false, true, false)));

        DocumentoPdf documento = useCase.generar(7L);

        assertTrue(documento.estaVacio());
        verify(chequeraPdfGenerator, never()).generarBorradorChequera(any());
    }

    @Test
    void generaElPdfCuandoHayAlMenosUnaCuotaAdeudada() {
        when(obtenerBorradorImpresionUseCase.obtener(7L))
                .thenReturn(borrador("ABIERTO", cuota(true, false, false), cuota(false, false, false)));
        when(chequeraPdfGenerator.generarBorradorChequera(any())).thenReturn(DocumentoPdf.de("/tmp/x.pdf"));

        assertTrue(!useCase.generar(7L).estaVacio());
    }

    private static BorradorImpresion.Cuota cuota(boolean pagada, boolean baja, boolean compensada) {
        return new BorradorImpresion.Cuota(3, "Cuota", 1, 1, 3, 2026, null, null, null,
                new BigDecimal("2100.00"), null, null,
                (byte) (pagada ? 1 : 0), (byte) (baja ? 1 : 0), (byte) 0, (byte) (compensada ? 1 : 0));
    }

    private static BorradorImpresion borrador(String estado, BorradorImpresion.Cuota... cuotas) {
        return new BorradorImpresion(7L, estado, new BigDecimal("49024730"), 1, "Pérez", "Ana",
                5, "Ingeniería", 2, "Cuota", 38, "Lectivo 2026 - 2027", 3, "Arancel completo",
                1, "Cuota", new BigDecimal("0.25"), null, null, BigDecimal.ZERO, List.of(cuotas));
    }
}
