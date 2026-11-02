package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out;

public interface SolicitudCuotaPublisher {

    void publicar(Long chequeraCuotaId);

}
