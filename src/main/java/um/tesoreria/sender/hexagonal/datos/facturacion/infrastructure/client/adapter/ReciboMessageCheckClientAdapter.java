package um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.ReciboMessageCheckRepository;
import um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client.ReciboMessageCheckClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.FacturacionDtoMapper;

@Component
@RequiredArgsConstructor
public class ReciboMessageCheckClientAdapter implements ReciboMessageCheckRepository {

    private final ReciboMessageCheckClient reciboMessageCheckClient;
    private final FacturacionDtoMapper facturacionDtoMapper;

    @Override
    public ReciboMessageCheck registrar(ReciboMessageCheck messageCheck) {
        var response = reciboMessageCheckClient.add(facturacionDtoMapper.toDto(messageCheck));
        return ReciboMessageCheck.builder()
                .reciboMessageCheckId(response.getReciboMessageCheckId())
                .facturacionElectronicaId(response.getFacturacionElectronicaId())
                .chequeraPagoId(response.getChequeraPagoId())
                .facultadId(response.getFacultadId())
                .tipoChequeraId(response.getTipoChequeraId())
                .chequeraSerieId(response.getChequeraSerieId())
                .productoId(response.getProductoId())
                .alternativaId(response.getAlternativaId())
                .cuotaId(response.getCuotaId())
                .payload(response.getPayload())
                .build();
    }

}
