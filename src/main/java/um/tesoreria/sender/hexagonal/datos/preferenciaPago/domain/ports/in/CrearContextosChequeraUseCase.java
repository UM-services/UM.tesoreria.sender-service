package um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

public interface CrearContextosChequeraUseCase {

    List<PreferenciaPago> crear(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                Integer alternativaId);

}
