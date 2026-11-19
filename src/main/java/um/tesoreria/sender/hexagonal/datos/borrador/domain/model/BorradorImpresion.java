package um.tesoreria.sender.hexagonal.datos.borrador.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Datos de un borrador de chequera listos para imprimir. No hay número de chequera ni código de
 * barras: el borrador todavía no fue emitido.
 */
public record BorradorImpresion(Long borradorId,
                                String estado,
                                BigDecimal personaId,
                                Integer documentoId,
                                String apellido,
                                String nombre,
                                Integer facultadId,
                                String facultadNombre,
                                Integer tipoChequeraId,
                                String tipoChequeraNombre,
                                Integer lectivoId,
                                String lectivoNombre,
                                Integer arancelTipoId,
                                String arancelTipoDescripcion,
                                Integer alternativaId,
                                String alternativaTitulo,
                                BigDecimal becaPorcentaje,
                                OffsetDateTime fecha,
                                String carrera,
                                BigDecimal total,
                                List<Cuota> cuotas) {

    public boolean abierto() {
        return "ABIERTO".equals(estado);
    }

    public record Cuota(Integer productoId,
                        String productoNombre,
                        Integer alternativaId,
                        Integer cuotaId,
                        Integer mes,
                        Integer anio,
                        OffsetDateTime vencimiento1,
                        OffsetDateTime vencimiento2,
                        OffsetDateTime vencimiento3,
                        BigDecimal importe1,
                        BigDecimal importe2,
                        BigDecimal importe3,
                        Byte pagado,
                        Byte baja,
                        Byte manual,
                        Byte compensada) {
    }
}
