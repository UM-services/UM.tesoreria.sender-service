package um.tesoreria.sender.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraCuotaDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UMPreferenceMPDto {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;

}
