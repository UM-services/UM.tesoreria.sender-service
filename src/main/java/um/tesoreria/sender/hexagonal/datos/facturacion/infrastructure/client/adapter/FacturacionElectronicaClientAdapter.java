package um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.FacturacionElectronicaRepository;
import um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client.FacturacionElectronicaClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.FacturacionDtoMapper;

@Component
@RequiredArgsConstructor
public class FacturacionElectronicaClientAdapter implements FacturacionElectronicaRepository {

    private final FacturacionElectronicaClient facturacionElectronicaClient;
    private final FacturacionDtoMapper facturacionDtoMapper;

    @Override
    public Optional<FacturacionElectronica> findById(Long facturacionElectronicaId) {
        try {
            return Optional.ofNullable(facturacionDtoMapper.toDomain(
                    facturacionElectronicaClient.findByFacturacionElectronicaId(facturacionElectronicaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FacturacionElectronica> findNextPendiente() {
        try {
            return Optional.ofNullable(facturacionDtoMapper.toDomain(
                    facturacionElectronicaClient.findNextPendiente()));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public FacturacionElectronica update(FacturacionElectronica facturacionElectronica) {
        var dto = facturacionDtoMapper.toDto(facturacionElectronica);
        return facturacionDtoMapper.toDomain(
                facturacionElectronicaClient.update(dto, facturacionElectronica.getFacturacionElectronicaId()));
    }

}
