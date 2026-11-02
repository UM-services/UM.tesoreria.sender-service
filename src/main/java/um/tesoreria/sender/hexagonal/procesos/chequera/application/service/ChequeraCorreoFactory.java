package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.AdjuntoCorreo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.ImagenEnLinea;

@Service
public class ChequeraCorreoFactory {

    private static final String REPLY_TO = "no-reply@um.edu.ar";
    private static final int ETEC_FACULTAD_ID = 15;

    public Correo crear(String from, List<String> para, String asunto, String cuerpo, String archivoAdjunto,
                        Integer facultadId) {
        List<AdjuntoCorreo> adjuntos = archivoAdjunto == null
                ? List.of()
                : List.of(new AdjuntoCorreo(archivoAdjunto, archivoAdjunto));
        String logo = facultadId != null && facultadId == ETEC_FACULTAD_ID
                ? "marca_etec.png"
                : "marca_um_65.png";
        List<ImagenEnLinea> imagenes = List.of(
                new ImagenEnLinea("logoImage", logo),
                new ImagenEnLinea("medioPago1", "medio_pago_1.png"),
                new ImagenEnLinea("medioPago2", "medio_pago_2.png"));
        return Correo.builder()
                .from(from)
                .para(para)
                .replyTo(REPLY_TO)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .html(true)
                .adjuntos(adjuntos)
                .imagenesEnLinea(imagenes)
                .build();
    }

}
