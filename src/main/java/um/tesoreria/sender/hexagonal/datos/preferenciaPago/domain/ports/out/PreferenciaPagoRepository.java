package um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.out;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

public interface PreferenciaPagoRepository {

    List<PreferenciaPago> crearContexto(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                        Integer alternativaId);

    List<PreferenciaPago> crearContextoBulk(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                            Integer alternativaId);

}
