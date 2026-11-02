package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.adapter;

import feign.FeignException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraReemplazoRepository;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraCuotaReemplazoClient;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraSerieReemplazoClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.ChequeraDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraReemplazoClientAdapter implements ChequeraReemplazoRepository {

    private final ChequeraSerieReemplazoClient chequeraSerieReemplazoClient;
    private final ChequeraCuotaReemplazoClient chequeraCuotaReemplazoClient;
    private final ChequeraDtoMapper chequeraDtoMapper;

    @Override
    public Optional<ChequeraSerieReemplazo> findSerieByUnique(Integer facultadId, Integer tipoChequeraId,
                                                              Long chequeraSerieId) {
        try {
            return Optional.ofNullable(chequeraDtoMapper.toDomain(chequeraSerieReemplazoClient.findByUnique(
                    facultadId, tipoChequeraId, chequeraSerieId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ChequeraCuotaReemplazo> findCuotas(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                                   Integer alternativaId) {
        return chequeraCuotaReemplazoClient
                .findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(facultadId, tipoChequeraId,
                        chequeraSerieId, alternativaId)
                .stream()
                .map(chequeraDtoMapper::toDomain)
                .toList();
    }

}
