package um.tesoreria.sender.hexagonal.shared.infrastructure.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;
import um.tesoreria.sender.hexagonal.shared.domain.model.ComprobanteAfip;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ComprobanteAfipDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ComprobanteDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.FacturacionElectronicaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ReciboMessageCheckDto;

@Component
@RequiredArgsConstructor
public class FacturacionDtoMapper {

    private final ChequeraDtoMapper chequeraDtoMapper;

    public FacturacionElectronica toDomain(FacturacionElectronicaDto dto) {
        if (dto == null) return null;
        var builder = FacturacionElectronica.builder()
                .facturacionElectronicaId(dto.getFacturacionElectronicaId())
                .chequeraPagoId(dto.getChequeraPagoId())
                .comprobanteId(dto.getComprobanteId())
                .numeroComprobante(dto.getNumeroComprobante())
                .personaId(dto.getPersonaId())
                .tipoDocumento(dto.getTipoDocumento())
                .apellido(dto.getApellido())
                .nombre(dto.getNombre())
                .cuit(dto.getCuit())
                .condicionIva(dto.getCondicionIva())
                .importe(dto.getImporte())
                .cae(dto.getCae())
                .fechaRecibo(dto.getFechaRecibo())
                .fechaVencimientoCae(dto.getFechaVencimientoCae())
                .chequeraPago(chequeraDtoMapper.toDomain(dto.getChequeraPago()))
                .comprobante(toDomain(dto.getComprobante()))
                .original(dto);
        if (dto.getEnviada() != null) builder.enviada(dto.getEnviada());
        if (dto.getRetries() != null) builder.retries(dto.getRetries());
        return builder.build();
    }

    public Comprobante toDomain(ComprobanteDto dto) {
        if (dto == null) return null;
        return Comprobante.builder()
                .comprobanteId(dto.getComprobanteId())
                .comprobanteAfipId(dto.getComprobanteAfipId())
                .puntoVenta(dto.getPuntoVenta())
                .letraComprobante(dto.getLetraComprobante())
                .comprobanteAfip(toDomain(dto.getComprobanteAfip()))
                .build();
    }

    public ComprobanteAfip toDomain(ComprobanteAfipDto dto) {
        if (dto == null) return null;
        return ComprobanteAfip.builder()
                .comprobanteAfipId(dto.getComprobanteAfipId())
                .nombre(dto.getNombre())
                .label(dto.getLabel())
                .build();
    }

    /**
     * Reconstruye el DTO de core partiendo del original recibido, para no perder
     * campos que el negocio no consume. Sólo se pisan los campos que el proceso modifica.
     */
    public FacturacionElectronicaDto toDto(FacturacionElectronica domain) {
        if (domain == null) return null;
        var dto = domain.getOriginal() instanceof FacturacionElectronicaDto original
                ? original
                : new FacturacionElectronicaDto();
        dto.setFacturacionElectronicaId(domain.getFacturacionElectronicaId());
        dto.setChequeraPagoId(domain.getChequeraPagoId());
        dto.setComprobanteId(domain.getComprobanteId());
        dto.setNumeroComprobante(domain.getNumeroComprobante());
        dto.setPersonaId(domain.getPersonaId());
        dto.setTipoDocumento(domain.getTipoDocumento());
        dto.setApellido(domain.getApellido());
        dto.setNombre(domain.getNombre());
        dto.setCuit(domain.getCuit());
        dto.setCondicionIva(domain.getCondicionIva());
        dto.setImporte(domain.getImporte());
        dto.setCae(domain.getCae());
        dto.setFechaRecibo(domain.getFechaRecibo());
        dto.setFechaVencimientoCae(domain.getFechaVencimientoCae());
        dto.setEnviada(domain.getEnviada());
        dto.setRetries(domain.getRetries());
        return dto;
    }



    public ReciboMessageCheckDto toDto(ReciboMessageCheck domain) {
        if (domain == null) return null;
        return new ReciboMessageCheckDto.Builder()
                .reciboMessageCheckId(domain.getReciboMessageCheckId())
                .facturacionElectronicaId(domain.getFacturacionElectronicaId())
                .chequeraPagoId(domain.getChequeraPagoId())
                .facultadId(domain.getFacultadId())
                .tipoChequeraId(domain.getTipoChequeraId())
                .chequeraSerieId(domain.getChequeraSerieId())
                .productoId(domain.getProductoId())
                .alternativaId(domain.getAlternativaId())
                .cuotaId(domain.getCuotaId())
                .payload(domain.getPayload())
                .build();
    }

}
