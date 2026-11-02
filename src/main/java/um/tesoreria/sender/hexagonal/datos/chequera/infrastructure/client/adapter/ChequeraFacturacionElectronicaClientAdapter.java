package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraFacturacionElectronicaRepository;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraFacturacionElectronicaClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.ChequeraDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraFacturacionElectronicaClientAdapter implements ChequeraFacturacionElectronicaRepository {

    private final ChequeraFacturacionElectronicaClient chequeraFacturacionElectronicaClient;
    private final ChequeraDtoMapper chequeraDtoMapper;

    @Override
    public Optional<ChequeraFacturacionElectronica> findByChequeraId(Long chequeraId) {
        try {
            return Optional.ofNullable(chequeraDtoMapper.toDomain(
                    chequeraFacturacionElectronicaClient.findByChequeraId(chequeraId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
