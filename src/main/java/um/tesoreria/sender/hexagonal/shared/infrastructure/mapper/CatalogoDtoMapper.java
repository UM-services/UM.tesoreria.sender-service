package um.tesoreria.sender.hexagonal.shared.infrastructure.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.model.ArancelTipo;
import um.tesoreria.sender.hexagonal.shared.domain.model.Facultad;
import um.tesoreria.sender.hexagonal.shared.domain.model.Geografica;
import um.tesoreria.sender.hexagonal.shared.domain.model.Lectivo;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.domain.model.Producto;
import um.tesoreria.sender.hexagonal.shared.domain.model.TipoChequera;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ArancelTipoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.FacultadDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.GeograficaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.LectivoAlternativaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.LectivoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ProductoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.TipoChequeraDto;

@Component
public class CatalogoDtoMapper {

    public Facultad toDomain(FacultadDto dto) {
        if (dto == null) return null;
        return Facultad.builder()
                .facultadId(dto.getFacultadId())
                .nombre(dto.getNombre())
                .build();
    }

    public Geografica toDomain(GeograficaDto dto) {
        if (dto == null) return null;
        return Geografica.builder()
                .geograficaId(dto.getGeograficaId())
                .nombre(dto.getNombre())
                .build();
    }

    public Lectivo toDomain(LectivoDto dto) {
        if (dto == null) return null;
        return Lectivo.builder()
                .lectivoId(dto.getLectivoId())
                .nombre(dto.getNombre())
                .build();
    }

    public ArancelTipo toDomain(ArancelTipoDto dto) {
        if (dto == null) return null;
        return ArancelTipo.builder()
                .arancelTipoId(dto.getArancelTipoId())
                .descripcion(dto.getDescripcion())
                .build();
    }

    public Producto toDomain(ProductoDto dto) {
        if (dto == null) return null;
        return Producto.builder()
                .productoId(dto.getProductoId())
                .nombre(dto.getNombre())
                .build();
    }

    public TipoChequera toDomain(TipoChequeraDto dto) {
        if (dto == null) return null;
        return TipoChequera.builder()
                .tipoChequeraId(dto.getTipoChequeraId())
                .nombre(dto.getNombre())
                .emailCopia(dto.getEmailCopia())
                .geografica(toDomain(dto.getGeografica()))
                .build();
    }

    public LectivoAlternativa toDomain(LectivoAlternativaDto dto) {
        if (dto == null) return null;
        var builder = LectivoAlternativa.builder()
                .lectivoAlternativaId(dto.getLectivoAlternativaId())
                .facultadId(dto.getFacultadId())
                .lectivoId(dto.getLectivoId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .titulo(dto.getTitulo());
        if (dto.getCuotas() != null) builder.cuotas(dto.getCuotas());
        return builder.build();
    }

}
