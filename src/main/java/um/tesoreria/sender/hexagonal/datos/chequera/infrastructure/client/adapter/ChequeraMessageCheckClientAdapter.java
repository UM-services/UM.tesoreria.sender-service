package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraMessageCheckRepository;
import um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client.ChequeraMessageCheckClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.ChequeraDtoMapper;

@Component
@RequiredArgsConstructor
public class ChequeraMessageCheckClientAdapter implements ChequeraMessageCheckRepository {

    private final ChequeraMessageCheckClient chequeraMessageCheckClient;
    private final ChequeraDtoMapper chequeraDtoMapper;

    @Override
    public ChequeraMessageCheck registrar(ChequeraMessageCheck messageCheck) {
        var response = chequeraMessageCheckClient.add(chequeraDtoMapper.toDto(messageCheck));
        return ChequeraMessageCheck.builder()
                .chequeraMessageCheckId(response.getChequeraMessageCheckId())
                .facultadId(response.getFacultadId())
                .tipoChequeraId(response.getTipoChequeraId())
                .chequeraSerieId(response.getChequeraSerieId())
                .payload(response.getPayload())
                .build();
    }

}
