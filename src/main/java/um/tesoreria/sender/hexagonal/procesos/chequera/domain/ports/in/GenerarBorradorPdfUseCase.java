package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

/**
 * Imprime el borrador de una chequera con la leyenda BORRADOR. Sin código de barras, sin datos de
 * pago y sin efectos en la base.
 */
public interface GenerarBorradorPdfUseCase {

    DocumentoPdf generar(Long borradorId);
}
