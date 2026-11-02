package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

public record SolicitudChequeraPdf(Integer facultadId,
                                   Integer tipoChequeraId,
                                   Long chequeraSerieId,
                                   Integer alternativaId,
                                   boolean codigoBarras,
                                   boolean completa,
                                   boolean bulk) {
}
