package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCuotaEvent implements Serializable {

    private Long chequeraCuotaId;

}
