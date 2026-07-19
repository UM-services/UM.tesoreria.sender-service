package um.tesoreria.sender.domain.dto;

import lombok.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraCuotaDto;
import um.tesoreria.sender.util.Jsonifyable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UMPreferenceMPDto implements Jsonifyable {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;

}
