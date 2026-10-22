package um.tesoreria.sender.hexagonal.shared.infrastructure.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.UMPreferenceMPDto;

@Component
@RequiredArgsConstructor
public class PreferenciaPagoDtoMapper {

    private final ChequeraDtoMapper chequeraDtoMapper;

    public PreferenciaPago toDomain(UMPreferenceMPDto dto) {
        if (dto == null) return null;
        return PreferenciaPago.builder()
                .mercadoPagoContext(toDomain(dto.getMercadoPagoContext()))
                .chequeraCuota(chequeraDtoMapper.toDomain(dto.getChequeraCuota()))
                .build();
    }

    public MercadoPagoContext toDomain(MercadoPagoContextDto dto) {
        if (dto == null) return null;
        var builder = MercadoPagoContext.builder()
                .mercadoPagoContextId(dto.getMercadoPagoContextId())
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .initPoint(dto.getInitPoint())
                .fechaVencimiento(dto.getFechaVencimiento())
                .chequeraPagoId(dto.getChequeraPagoId())
                .preferenceId(dto.getPreferenceId());
        if (dto.getImporte() != null) builder.importe(dto.getImporte());
        if (dto.getActivo() != null) builder.activo(dto.getActivo());
        return builder.build();
    }

}
