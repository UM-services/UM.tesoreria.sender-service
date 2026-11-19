package um.tesoreria.sender.hexagonal.datos.borrador.infrastructure.client.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.model.BorradorImpresion;
import um.tesoreria.sender.hexagonal.datos.borrador.domain.ports.out.BorradorImpresionRepository;
import um.tesoreria.sender.hexagonal.datos.borrador.infrastructure.client.BorradorChequeraClient;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.BorradorImpresionDto;

@Component
@RequiredArgsConstructor
public class BorradorImpresionClientAdapter implements BorradorImpresionRepository {

    private final BorradorChequeraClient client;

    @Override
    public BorradorImpresion obtener(Long borradorId) {
        return toDomain(client.impresion(borradorId));
    }

    private BorradorImpresion toDomain(BorradorImpresionDto dto) {
        if (dto == null) {
            return null;
        }
        List<BorradorImpresion.Cuota> cuotas = dto.getCuotas() == null ? List.of()
                : dto.getCuotas().stream().map(this::toDomain).toList();
        return new BorradorImpresion(dto.getBorradorId(), dto.getEstado(), dto.getPersonaId(),
                dto.getDocumentoId(), dto.getApellido(), dto.getNombre(), dto.getFacultadId(),
                dto.getFacultadNombre(), dto.getTipoChequeraId(), dto.getTipoChequeraNombre(),
                dto.getLectivoId(), dto.getLectivoNombre(), dto.getArancelTipoId(),
                dto.getArancelTipoDescripcion(), dto.getAlternativaId(), dto.getAlternativaTitulo(),
                dto.getBecaPorcentaje(), dto.getFecha(), dto.getCarrera(), dto.getTotal(), cuotas);
    }

    private BorradorImpresion.Cuota toDomain(BorradorImpresionDto.CuotaDto dto) {
        return new BorradorImpresion.Cuota(dto.getProductoId(), dto.getProductoNombre(),
                dto.getAlternativaId(), dto.getCuotaId(), dto.getMes(), dto.getAnio(),
                dto.getVencimiento1(), dto.getVencimiento2(), dto.getVencimiento3(),
                dto.getImporte1(), dto.getImporte2(), dto.getImporte3(),
                dto.getPagado(), dto.getBaja(), dto.getManual(), dto.getCompensada());
    }
}
