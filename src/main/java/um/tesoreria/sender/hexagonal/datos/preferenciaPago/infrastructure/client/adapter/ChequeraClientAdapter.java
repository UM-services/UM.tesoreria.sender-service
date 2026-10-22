package um.tesoreria.sender.hexagonal.datos.preferenciaPago.infrastructure.client.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.out.PreferenciaPagoRepository;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.infrastructure.client.ChequeraClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.PreferenciaPagoDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraClientAdapter implements PreferenciaPagoRepository {

    private final ChequeraClient chequeraClient;
    private final PreferenciaPagoDtoMapper preferenciaPagoDtoMapper;

    @Override
    public List<PreferenciaPago> crearContexto(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                               Integer alternativaId) {
        return chequeraClient.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)
                .stream()
                .map(preferenciaPagoDtoMapper::toDomain)
                .toList();
    }

    @Override
    public List<PreferenciaPago> crearContextoBulk(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                                   Integer alternativaId) {
        return chequeraClient.createChequeraContextBulk(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)
                .stream()
                .map(preferenciaPagoDtoMapper::toDomain)
                .toList();
    }

}
