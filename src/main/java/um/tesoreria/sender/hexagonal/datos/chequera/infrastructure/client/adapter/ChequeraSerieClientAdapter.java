package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraSerieRepository;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraSerieClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.ChequeraDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraSerieClientAdapter implements ChequeraSerieRepository {

    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraDtoMapper chequeraDtoMapper;

    @Override
    public Optional<ChequeraSerie> findByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        try {
            return Optional.ofNullable(chequeraDtoMapper.toDomain(
                    chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public ChequeraSerie markSent(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return chequeraDtoMapper.toDomain(
                chequeraSerieClient.markSent(facultadId, tipoChequeraId, chequeraSerieId));
    }

}
