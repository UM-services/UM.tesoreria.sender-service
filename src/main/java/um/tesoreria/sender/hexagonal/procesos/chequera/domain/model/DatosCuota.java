package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

public record DatosCuota(ChequeraSerie serie,
                         ChequeraCuota cuota,
                         Persona persona,
                         Carrera carrera,
                         List<LectivoAlternativa> alternativas) {
}
