package um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.in;

public interface EnviarReciboUseCase {

    String enviar(Long facturacionElectronicaId);

}
