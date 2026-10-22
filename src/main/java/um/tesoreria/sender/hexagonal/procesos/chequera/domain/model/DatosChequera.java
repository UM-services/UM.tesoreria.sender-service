package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

public record DatosChequera(ChequeraSerie serie,
                            List<PreferenciaPago> preferencias,
                            Persona persona,
                            Carrera carrera,
                            List<LectivoAlternativa> alternativas) {
}
