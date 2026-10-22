package um.tesoreria.sender.hexagonal.shared.infrastructure.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.model.InscripcionFull;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.CarreraDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.DomicilioDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.InscripcionFullDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.LegajoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.PersonaDto;

@Component
public class PersonaDtoMapper {

    public Persona toDomain(PersonaDto dto) {
        if (dto == null) return null;
        return Persona.builder()
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .apellido(dto.getApellido())
                .nombre(dto.getNombre())
                .build();
    }

    public Domicilio toDomain(DomicilioDto dto) {
        if (dto == null) return null;
        return Domicilio.builder()
                .domicilioId(dto.getDomicilioId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .calle(dto.getCalle())
                .puerta(dto.getPuerta())
                .piso(dto.getPiso())
                .dpto(dto.getDpto())
                .observaciones(dto.getObservaciones())
                .emailPersonal(dto.getEmailPersonal())
                .emailInstitucional(dto.getEmailInstitucional())
                .build();
    }

    public InscripcionFull toDomain(InscripcionFullDto dto) {
        if (dto == null) return null;
        return InscripcionFull.builder()
                .personaPago(toDomain(dto.getPersonaPago()))
                .domicilioPago(toDomain(dto.getDomicilioPago()))
                .build();
    }

    public Legajo toDomain(LegajoDto dto) {
        if (dto == null) return null;
        return Legajo.builder()
                .legajoId(dto.getLegajoId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .facultadId(dto.getFacultadId())
                .lectivoId(dto.getLectivoId())
                .planId(dto.getPlanId())
                .carreraId(dto.getCarreraId())
                .build();
    }

    public Carrera toDomain(CarreraDto dto) {
        if (dto == null) return null;
        return Carrera.builder()
                .facultadId(dto.getFacultadId())
                .planId(dto.getPlanId())
                .carreraId(dto.getCarreraId())
                .nombre(dto.getNombre())
                .build();
    }

}
