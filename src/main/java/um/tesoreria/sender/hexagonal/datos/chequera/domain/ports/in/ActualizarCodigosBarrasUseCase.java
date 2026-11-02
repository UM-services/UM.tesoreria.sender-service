package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

public interface ActualizarCodigosBarrasUseCase {

    void actualizar(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

}
