package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

/**
 * Orden de envío de una chequera.
 *
 * <p>{@code solicitudId} y {@code chequeraId} vienen del ledger de core; si son nulos (evento
 * legacy o shim REST) el caso de uso crea/recupera la solicitud en core por facultad/tipo/serie.</p>
 */
public record SolicitudEnvioChequera(String solicitudId,
                                     Long chequeraId,
                                     Integer facultadId,
                                     Integer tipoChequeraId,
                                     Long chequeraSerieId,
                                     Integer alternativaId,
                                     boolean copiaInformes,
                                     boolean codigoBarras,
                                     boolean incluyeMatricula,
                                     boolean bulk,
                                     boolean actualizarCodigosBarras) {

    /** Constructor legacy (shim REST) sin identidad de ledger. */
    public SolicitudEnvioChequera(Integer facultadId,
                                  Integer tipoChequeraId,
                                  Long chequeraSerieId,
                                  Integer alternativaId,
                                  boolean copiaInformes,
                                  boolean codigoBarras,
                                  boolean incluyeMatricula,
                                  boolean bulk,
                                  boolean actualizarCodigosBarras) {
        this(null, null, facultadId, tipoChequeraId, chequeraSerieId, alternativaId,
                copiaInformes, codigoBarras, incluyeMatricula, bulk, actualizarCodigosBarras);
    }

}
