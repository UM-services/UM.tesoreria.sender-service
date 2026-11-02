package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraCuotaRepository;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraCuotaClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.ChequeraDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraCuotaClientAdapter implements ChequeraCuotaRepository {

    private final ChequeraCuotaClient chequeraCuotaClient;
    private final ChequeraDtoMapper chequeraDtoMapper;

    @Override
    public Optional<ChequeraCuota> findByChequeraCuotaId(Long chequeraCuotaId) {
        try {
            return Optional.ofNullable(chequeraDtoMapper.toDomain(
                    chequeraCuotaClient.findByChequeraCuotaId(chequeraCuotaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ChequeraCuota> findByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                                Integer productoId, Integer alternativaId, Integer cuotaId) {
        try {
            return Optional.ofNullable(chequeraDtoMapper.toDomain(chequeraCuotaClient.findByUnique(facultadId,
                    tipoChequeraId, chequeraSerieId, productoId, alternativaId, cuotaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public void actualizarCodigosBarras(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        chequeraCuotaClient.updateBarras(facultadId, tipoChequeraId, chequeraSerieId);
    }

}
