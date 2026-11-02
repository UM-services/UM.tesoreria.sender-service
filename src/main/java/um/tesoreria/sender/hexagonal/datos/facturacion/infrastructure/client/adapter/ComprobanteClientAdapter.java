package um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.ComprobanteRepository;
import um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client.ComprobanteClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.FacturacionDtoMapper;

@Component
@RequiredArgsConstructor
public class ComprobanteClientAdapter implements ComprobanteRepository {

    private final ComprobanteClient comprobanteClient;
    private final FacturacionDtoMapper facturacionDtoMapper;

    @Override
    public Optional<Comprobante> findByComprobanteId(Integer comprobanteId) {
        try {
            return Optional.ofNullable(facturacionDtoMapper.toDomain(
                    comprobanteClient.findByComprobanteId(comprobanteId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
