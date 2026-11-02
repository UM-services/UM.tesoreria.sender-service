package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

import java.util.List;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

public record ResultadoChequeraPdf(DocumentoPdf documento,
                                   ChequeraSerie serie,
                                   List<PreferenciaPago> preferencias) {
}
