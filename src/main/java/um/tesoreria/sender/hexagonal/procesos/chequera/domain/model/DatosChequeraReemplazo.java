package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

public record DatosChequeraReemplazo(ChequeraSerieReemplazo serie,
                                     List<ChequeraCuotaReemplazo> cuotas,
                                     Persona persona,
                                     Carrera carrera,
                                     List<LectivoAlternativa> alternativas) {
}
