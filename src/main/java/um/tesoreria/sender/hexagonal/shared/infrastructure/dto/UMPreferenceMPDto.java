package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import lombok.*;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraCuotaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifyable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UMPreferenceMPDto implements Jsonifyable {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;

}
