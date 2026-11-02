package um.tesoreria.sender.hexagonal.procesos.enlacePago.domain.ports.in;

public interface EnviarEnlacePagoUseCase {

    String enviar(Long chequeraCuotaId);

}
