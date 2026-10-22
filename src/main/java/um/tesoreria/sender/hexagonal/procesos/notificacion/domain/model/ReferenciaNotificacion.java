package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Referencia opcional al objeto de negocio que originó la notificación. Permite correlacionar
 * el resultado del envío sin acoplar el contrato genérico a un caso de uso concreto.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReferenciaNotificacion {

    private String tipo;
    private String id;

}
