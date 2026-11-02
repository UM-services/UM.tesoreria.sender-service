package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

@Service
public class ChequeraEmailComposer {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String cuerpoChequera(String nombreAlumno, List<PreferenciaPago> preferences,
                                 ChequeraSerie chequeraSerie) {
        StringBuilder data = new StringBuilder(encabezado(nombreAlumno,
                "Para agilizar tu gestión de pago te acercamos los enlaces de las próximas cuotas a vencer:"));
        for (var preferencia : preferences) {
            var chequeraCuota = preferencia.getChequeraCuota();
            if (chequeraCuota != null && chequeraCuota.pendienteDePago()
                    && preferencia.getMercadoPagoContext() != null) {
                data.append(fila(preferencia, chequeraCuota));
            }
        }
        data.append(pie(Objects.requireNonNull(chequeraSerie.getFacultad()).getNombre(),
                Objects.requireNonNull(chequeraSerie.getTipoChequera()).getNombre(),
                Objects.requireNonNull(chequeraSerie.getGeografica()).getNombre(),
                Objects.requireNonNull(chequeraSerie.getLectivo()).getNombre(),
                Objects.requireNonNull(chequeraSerie.getArancelTipo()).getDescripcion(),
                chequeraSerie.getAlternativaId()));
        return data.toString();
    }

    public String cuerpoCuota(String nombreAlumno, List<PreferenciaPago> preferences, ChequeraCuota chequeraCuota) {
        StringBuilder data = new StringBuilder(encabezado(nombreAlumno,
                "Para agilizar tu gestión de pago te acercamos el enlace de la cuota a pagar:"));
        for (var preferencia : preferences) {
            if (preferencia.getChequeraCuota() != null
                    && Objects.equals(chequeraCuota.getChequeraCuotaId(),
                    preferencia.getChequeraCuota().getChequeraCuotaId())
                    && chequeraCuota.pendienteDePago()
                    && preferencia.getMercadoPagoContext() != null) {
                data.append(fila(preferencia, chequeraCuota));
            }
        }
        var serie = Objects.requireNonNull(chequeraCuota.getChequeraSerie());
        data.append(pie(Objects.requireNonNull(chequeraCuota.getFacultad()).getNombre(),
                Objects.requireNonNull(chequeraCuota.getTipoChequera()).getNombre(),
                Objects.requireNonNull(serie.getGeografica()).getNombre(),
                Objects.requireNonNull(serie.getLectivo()).getNombre(),
                Objects.requireNonNull(serie.getArancelTipo()).getDescripcion(),
                serie.getAlternativaId()));
        return data.toString();
    }

    private String encabezado(String nombreAlumno, String introduccion) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0; }" +
                ".container { background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 600px; margin: auto; }" +
                ".header { background-color: #007BFF; color: white; padding: 20px; text-align: center; position: relative; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 20px; }" +
                ".info-section { background-color: #f4f4f4; padding: 15px; margin-bottom: 20px; border-radius: 8px; display: flex; flex-wrap: wrap; }" +
                ".info-details { flex: 1; min-width: 60%; }" +
                ".info-logo { flex: 0 0 150px; margin-top: 85px; text-align: right; }" +
                ".info-logo img { width: 150px; height: auto; }" +
                ".details-table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                ".details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }" +
                ".details-table th { background-color: #007BFF; color: white; }" +
                ".footer { background-color: #e9ecef; padding: 10px; text-align: center; font-size: 12px; color: #333; }" +
                ".qr-section { text-align: center; margin-top: 20px; }" +
                "@media only screen and (max-width: 600px) {" +
                "    .container { width: 100%; padding: 0; }" +
                "    .header h1 { font-size: 20px; }" +
                "    .content, .info-section { padding: 10px; }" +
                "    .details-table th, .details-table td { font-size: 14px; padding: 8px; }" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Universidad de Mendoza te envió un enlace de pago</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p><strong>Estimad@ " + nombreAlumno + ",</strong></p>" +
                "<p>" + introduccion + "</p>" +
                "<table class='details-table'>" +
                "<tr>" +
                "<th>Producto</th>" +
                "<th>Período</th>" +
                "<th>Fecha de Vencimiento</th>" +
                "<th>Importe</th>" +
                "<th>Enlace</th>" +
                "</tr>";
    }

    private String fila(PreferenciaPago preferencia, ChequeraCuota chequeraCuota) {
        var contexto = preferencia.getMercadoPagoContext();
        String fechaVencimientoFormatted = contexto.getFechaVencimiento().format(FORMATO_FECHA);
        return "<tr>" + "<td>" + Objects.requireNonNull(chequeraCuota.getProducto()).getNombre() + "</td>"
                + "<td>" + chequeraCuota.getMes() + "/" + chequeraCuota.getAnho() + "</td>"
                + "<td>" + fechaVencimientoFormatted + "</td>"
                + "<td>$" + String.format("%.2f", contexto.getImporte()) + "</td>"
                + "<td><a href='" + contexto.getInitPoint()
                + "' style='color: #007BFF; font-weight: bold;'>Click aquí para realizar el pago</a></td>"
                + "</tr>";
    }

    private String pie(String facultadNombre, String tipoChequeraNombre, String geograficaNombre,
                       String lectivoNombre, String arancelTipoDescripcion, Integer alternativaId) {
        return "</table>" +
                "<div class='info-section'>" +
                "<div class='info-details'>" +
                "<p>Unidad Académica: <strong>" + facultadNombre + "</strong></p>" +
                "<br/>" +
                "<p>Tipo de Chequera: <strong>" + tipoChequeraNombre + "</strong></p>" +
                "<br/>" +
                "<p>Sede: <strong>" + geograficaNombre + "</strong></p>" +
                "<br/>" +
                "<p>Ciclo Lectivo: <strong>" + lectivoNombre + "</strong></p>" +
                "<br/>" +
                "<p>Tipo de Arancel: <strong>" + arancelTipoDescripcion + "</strong></p>" +
                "<br/>" +
                "<p>Alternativa: <strong>" + alternativaId + "</strong></p>" +
                "</div>" +
                "<div class='info-logo'>" +
                "<img src='cid:logoImage' alt='Logo'/>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Hasta pronto, seguimos en contacto</p>" +
                "<p>Si ya has cancelado tu cuota desestima este recordatorio. </p>" +
                "<br/>" +
                "<p>Universidad de Mendoza</p>" +
                "<p><small>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</small></p>" +
                "</div>" +
                "</div>" +
                "<div class='container'>" +
                "<div class='content'>" +
                "<img src='cid:medioPago1' alt='Medio de Pago 1' style='width: 100%; height: auto; margin-bottom: 20px;'/>" +
                "<img src='cid:medioPago2' alt='Medio de Pago 2' style='width: 100%; height: auto;'/>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}
