package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.ResultadoChequeraPdf;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudChequeraPdf;

public interface GenerarChequeraPdfUseCase {

    ResultadoChequeraPdf generar(SolicitudChequeraPdf solicitud);

}
