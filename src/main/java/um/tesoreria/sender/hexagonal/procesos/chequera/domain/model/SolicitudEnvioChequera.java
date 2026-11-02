package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

public record SolicitudEnvioChequera(Integer facultadId,
                                     Integer tipoChequeraId,
                                     Long chequeraSerieId,
                                     Integer alternativaId,
                                     boolean copiaInformes,
                                     boolean codigoBarras,
                                     boolean incluyeMatricula,
                                     boolean bulk,
                                     boolean actualizarCodigosBarras) {
}
