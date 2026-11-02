package um.tesoreria.sender.hexagonal.shared.infrastructure.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraCuotaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraCuotaReemplazoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraFacturacionElectronicaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraMessageCheckDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraPagoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraSerieDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraSerieReemplazoDto;

@Component
@RequiredArgsConstructor
public class ChequeraDtoMapper {

    private final CatalogoDtoMapper catalogoDtoMapper;
    private final PersonaDtoMapper personaDtoMapper;

    public ChequeraSerie toDomain(ChequeraSerieDto dto) {
        if (dto == null) return null;
        var builder = ChequeraSerie.builder()
                .chequeraId(dto.getChequeraId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .lectivoId(dto.getLectivoId())
                .arancelTipoId(dto.getArancelTipoId())
                .alternativaId(dto.getAlternativaId())
                .persona(personaDtoMapper.toDomain(dto.getPersona()))
                .domicilio(personaDtoMapper.toDomain(dto.getDomicilio()))
                .facultad(catalogoDtoMapper.toDomain(dto.getFacultad()))
                .tipoChequera(catalogoDtoMapper.toDomain(dto.getTipoChequera()))
                .lectivo(catalogoDtoMapper.toDomain(dto.getLectivo()))
                .arancelTipo(catalogoDtoMapper.toDomain(dto.getArancelTipo()))
                .geografica(catalogoDtoMapper.toDomain(dto.getGeografica()));
        if (dto.getBecaPorcentaje() != null) builder.becaPorcentaje(dto.getBecaPorcentaje());
        return builder.build();
    }

    public ChequeraCuota toDomain(ChequeraCuotaDto dto) {
        if (dto == null) return null;
        var builder = ChequeraCuota.builder()
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .vencimiento1(dto.getVencimiento1())
                .vencimiento2(dto.getVencimiento2())
                .vencimiento3(dto.getVencimiento3())
                .pagado(dto.getPagado())
                .baja(dto.getBaja())
                .compensada(dto.getCompensada())
                .facultad(catalogoDtoMapper.toDomain(dto.getFacultad()))
                .tipoChequera(catalogoDtoMapper.toDomain(dto.getTipoChequera()))
                .producto(catalogoDtoMapper.toDomain(dto.getProducto()))
                .chequeraSerie(toDomain(dto.getChequeraSerie()));
        if (dto.getImporte1() != null) builder.importe1(dto.getImporte1());
        if (dto.getImporte2() != null) builder.importe2(dto.getImporte2());
        if (dto.getImporte3() != null) builder.importe3(dto.getImporte3());
        if (dto.getCodigoBarras() != null) builder.codigoBarras(dto.getCodigoBarras());
        return builder.build();
    }

    public ChequeraPago toDomain(ChequeraPagoDto dto) {
        if (dto == null) return null;
        var builder = ChequeraPago.builder()
                .chequeraPagoId(dto.getChequeraPagoId())
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .fecha(dto.getFecha())
                .acreditacion(dto.getAcreditacion())
                .observaciones(dto.getObservaciones());
        if (dto.getImporte() != null) builder.importe(dto.getImporte());
        return builder.build();
    }

    public ChequeraFacturacionElectronica toDomain(ChequeraFacturacionElectronicaDto dto) {
        if (dto == null) return null;
        var builder = ChequeraFacturacionElectronica.builder()
                .chequeraFacturacionElectronicaId(dto.getChequeraFacturacionElectronicaId())
                .chequeraId(dto.getChequeraId())
                .cuit(dto.getCuit())
                .razonSocial(dto.getRazonSocial())
                .domicilio(dto.getDomicilio())
                .condicionIva(dto.getCondicionIva());
        if (dto.getEmail() != null) builder.email(dto.getEmail());
        return builder.build();
    }

    public ChequeraSerieReemplazo toDomain(ChequeraSerieReemplazoDto dto) {
        if (dto == null) return null;
        return ChequeraSerieReemplazo.builder()
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .lectivoId(dto.getLectivoId())
                .alternativaId(dto.getAlternativaId())
                .persona(personaDtoMapper.toDomain(dto.getPersona()))
                .domicilio(personaDtoMapper.toDomain(dto.getDomicilio()))
                .facultad(catalogoDtoMapper.toDomain(dto.getFacultad()))
                .tipoChequera(catalogoDtoMapper.toDomain(dto.getTipoChequera()))
                .lectivo(catalogoDtoMapper.toDomain(dto.getLectivo()))
                .arancelTipo(catalogoDtoMapper.toDomain(dto.getArancelTipo()))
                .geografica(catalogoDtoMapper.toDomain(dto.getGeografica()))
                .build();
    }

    public ChequeraCuotaReemplazo toDomain(ChequeraCuotaReemplazoDto dto) {
        if (dto == null) return null;
        var builder = ChequeraCuotaReemplazo.builder()
                .chequeraCuotaReemplazoId(dto.getChequeraCuotaReemplazoId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .vencimiento1(dto.getVencimiento1())
                .vencimiento2(dto.getVencimiento2())
                .vencimiento3(dto.getVencimiento3())
                .pagado(dto.getPagado())
                .baja(dto.getBaja())
                .compensada(dto.getCompensada());
        if (dto.getImporte1() != null) builder.importe1(dto.getImporte1());
        if (dto.getImporte2() != null) builder.importe2(dto.getImporte2());
        if (dto.getImporte3() != null) builder.importe3(dto.getImporte3());
        if (dto.getCodigoBarras() != null) builder.codigoBarras(dto.getCodigoBarras());
        return builder.build();
    }


    public ChequeraMessageCheckDto toDto(ChequeraMessageCheck domain) {
        if (domain == null) return null;
        return new ChequeraMessageCheckDto.Builder()
                .chequeraMessageCheckId(domain.getChequeraMessageCheckId())
                .facultadId(domain.getFacultadId())
                .tipoChequeraId(domain.getTipoChequeraId())
                .chequeraSerieId(domain.getChequeraSerieId())
                .payload(domain.getPayload())
                .build();
    }

}
