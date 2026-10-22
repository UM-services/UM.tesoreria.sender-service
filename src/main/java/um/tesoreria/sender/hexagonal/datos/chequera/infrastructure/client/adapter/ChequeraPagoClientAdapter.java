package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraPagoRepository;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraPagoClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.ChequeraDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraPagoClientAdapter implements ChequeraPagoRepository {

    private final ChequeraPagoClient chequeraPagoClient;
    private final ChequeraDtoMapper chequeraDtoMapper;

    @Override
    public Optional<ChequeraPago> findByChequeraPagoId(Long chequeraPagoId) {
        try {
            return Optional.ofNullable(chequeraDtoMapper.toDomain(
                    chequeraPagoClient.findByChequeraPagoId(chequeraPagoId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
