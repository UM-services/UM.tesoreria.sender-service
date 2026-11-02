package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendChequeraEvent;

@Component
public class SendChequeraEventMapper {

    public SolicitudEnvioChequera toSolicitud(SendChequeraEvent event) {
        return new SolicitudEnvioChequera(
                event.getFacultadId(),
                event.getTipoChequeraId(),
                event.getChequeraSerieId(),
                event.getAlternativaId(),
                Boolean.TRUE.equals(event.getCopiaInformes()),
                Boolean.TRUE.equals(event.getCodigoBarras()),
                Boolean.TRUE.equals(event.getIncluyeMatricula()),
                Boolean.TRUE.equals(event.getBulk()),
                Boolean.TRUE.equals(event.getActualizarCodigosBarras()));
    }

    public SendChequeraEvent toEvent(SolicitudEnvioChequera solicitud) {
        return SendChequeraEvent.builder()
                .facultadId(solicitud.facultadId())
                .tipoChequeraId(solicitud.tipoChequeraId())
                .chequeraSerieId(solicitud.chequeraSerieId())
                .alternativaId(solicitud.alternativaId())
                .copiaInformes(solicitud.copiaInformes())
                .incluyeMatricula(solicitud.incluyeMatricula())
                .codigoBarras(solicitud.codigoBarras())
                .bulk(solicitud.bulk())
                .actualizarCodigosBarras(solicitud.actualizarCodigosBarras())
                .build();
    }

}
