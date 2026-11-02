package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in;

public interface EnviarCuotaUseCase {

    String enviar(Long chequeraCuotaId);

}
