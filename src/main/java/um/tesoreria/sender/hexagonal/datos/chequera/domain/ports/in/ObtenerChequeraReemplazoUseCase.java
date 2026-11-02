package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;

public interface ObtenerChequeraReemplazoUseCase {

    ChequeraSerieReemplazo obtenerSerie(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

    List<ChequeraCuotaReemplazo> obtenerCuotas(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                               Integer alternativaId);

}
