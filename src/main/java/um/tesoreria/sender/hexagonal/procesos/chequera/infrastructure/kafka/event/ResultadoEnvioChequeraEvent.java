package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event;

import java.time.OffsetDateTime;

/**
 * Resultado de envío de chequera publicado en {@code notificacion-resultado}.
 *
 * <p>Se declara acá, con la misma forma JSON que {@code ResultadoNotificacionEvent} del flujo
 * genérico de notificaciones, para no acoplar el slice de chequera al de notificación: core
 * deserializa por nombre de campo.</p>
 */
public record ResultadoEnvioChequeraEvent(
        String eventId,
        OffsetDateTime occurredAt,
        boolean enviado,
        String error,
        Referencia referencia) {

    public record Referencia(String tipo, String id) {
    }
}
