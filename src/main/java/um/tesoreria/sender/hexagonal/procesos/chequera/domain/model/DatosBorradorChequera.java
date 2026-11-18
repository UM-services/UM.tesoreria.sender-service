package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Datos de un borrador de chequera para el render con leyenda BORRADOR. Es un modelo propio de
 * {@code procesos} (no se falsifica {@code ChequeraSerie} ni {@code ChequeraCuota}, que exigen
 * persistencia y número).
 */
public record DatosBorradorChequera(String apellido,
                                    String nombre,
                                    BigDecimal personaId,
                                    Integer facultadId,
                                    String facultadNombre,
                                    Integer tipoChequeraId,
                                    String tipoChequeraNombre,
                                    String lectivoNombre,
                                    String arancelTipoDescripcion,
                                    Integer alternativaId,
                                    String alternativaTitulo,
                                    BigDecimal becaPorcentaje,
                                    OffsetDateTime fecha,
                                    String carrera,
                                    BigDecimal total,
                                    List<CuotaBorrador> cuotas) {

    public record CuotaBorrador(Integer productoId,
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
                                boolean pagada,
                                boolean deBaja,
                                boolean compensada) {

        /**
         * Sólo se imprime lo adeudado: ni pagada, ni de baja, ni compensada, y con importe (mismo
         * criterio que {@code ChequeraCuota.pendienteDePago()} del comprobante real).
         */
        public boolean adeudada() {
            return !pagada && !deBaja && !compensada
                    && importe1 != null && importe1.signum() != 0;
        }
    }
}
