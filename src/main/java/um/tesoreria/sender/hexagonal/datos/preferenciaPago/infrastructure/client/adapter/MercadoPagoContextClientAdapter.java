package um.tesoreria.sender.hexagonal.datos.preferenciaPago.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.out.MercadoPagoContextRepository;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.infrastructure.client.MercadoPagoContextClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.PreferenciaPagoDtoMapper;

@Component
@RequiredArgsConstructor
public class MercadoPagoContextClientAdapter implements MercadoPagoContextRepository {

    private final MercadoPagoContextClient mercadoPagoContextClient;
    private final PreferenciaPagoDtoMapper preferenciaPagoDtoMapper;

    @Override
    public Optional<MercadoPagoContext> findActivoByChequeraCuotaId(Long chequeraCuotaId) {
        try {
            return Optional.ofNullable(preferenciaPagoDtoMapper.toDomain(
                    mercadoPagoContextClient.findActivoByChequeraCuotaId(chequeraCuotaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
