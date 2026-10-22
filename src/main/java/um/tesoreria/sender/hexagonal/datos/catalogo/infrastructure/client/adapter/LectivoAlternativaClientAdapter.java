package um.tesoreria.sender.hexagonal.datos.catalogo.infrastructure.client.adapter;

import feign.FeignException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.catalogo.domain.ports.out.LectivoAlternativaRepository;
import um.tesoreria.sender.hexagonal.datos.catalogo.infrastructure.client.LectivoAlternativaClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.CatalogoDtoMapper;

@Component
@RequiredArgsConstructor
public class LectivoAlternativaClientAdapter implements LectivoAlternativaRepository {

    private final LectivoAlternativaClient lectivoAlternativaClient;
    private final CatalogoDtoMapper catalogoDtoMapper;

    @Override
    public List<LectivoAlternativa> findAllByTipo(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                                  Integer alternativaId) {
        return lectivoAlternativaClient.findAllByTipo(facultadId, lectivoId, tipoChequeraId, alternativaId)
                .stream()
                .map(catalogoDtoMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<LectivoAlternativa> findByUnique(Integer facultadId, Integer lectivoId, Integer tipoChequeraId,
                                                     Integer productoId, Integer alternativaId) {
        try {
            return Optional.ofNullable(catalogoDtoMapper.toDomain(
                    lectivoAlternativaClient.findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                            facultadId, lectivoId, tipoChequeraId, productoId, alternativaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
