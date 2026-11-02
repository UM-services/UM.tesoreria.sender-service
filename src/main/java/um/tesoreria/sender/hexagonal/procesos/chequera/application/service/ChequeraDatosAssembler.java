package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.catalogo.application.service.CatalogoDatosService;
import um.tesoreria.sender.hexagonal.datos.persona.application.service.PersonaDatosService;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequeraReemplazo;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChequeraDatosAssembler {

    private final PersonaDatosService personaDatosService;
    private final CatalogoDatosService catalogoDatosService;

    public DatosChequera armar(ChequeraSerie serie, List<PreferenciaPago> preferencias) {
        var persona = obtenerPersona(serie.getFacultadId(), serie.getPersonaId(), serie.getDocumentoId());
        var carrera = obtenerCarrera(serie.getFacultadId(), serie.getPersonaId(), serie.getDocumentoId(), persona);
        var alternativas = obtenerAlternativas(serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                serie.getAlternativaId());
        return new DatosChequera(serie, preferencias, persona, carrera, alternativas);
    }

    public DatosChequeraReemplazo armarReemplazo(ChequeraSerieReemplazo serie, List<ChequeraCuotaReemplazo> cuotas) {
        var persona = obtenerPersona(serie.getFacultadId(), serie.getPersonaId(), serie.getDocumentoId());
        var carrera = obtenerCarrera(serie.getFacultadId(), serie.getPersonaId(), serie.getDocumentoId(), persona);
        var alternativas = obtenerAlternativas(serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                serie.getAlternativaId());
        return new DatosChequeraReemplazo(serie, cuotas, persona, carrera, alternativas);
    }

    public DatosCuota armarCuota(ChequeraSerie serie, ChequeraCuota cuota) {
        var persona = obtenerPersona(serie.getFacultadId(), serie.getPersonaId(), serie.getDocumentoId());
        var carrera = obtenerCarrera(serie.getFacultadId(), serie.getPersonaId(), serie.getDocumentoId(), persona);
        var alternativas = obtenerAlternativas(serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                serie.getAlternativaId());
        return new DatosCuota(serie, cuota, persona, carrera, alternativas);
    }

    private Persona obtenerPersona(Integer facultadId, BigDecimal personaId, Integer documentoId) {
        try {
            return personaDatosService.obtenerPersona(personaId, documentoId);
        } catch (Exception e) {
            log.error("Sin datos de persona {}: {}", personaId, e.getMessage());
            return new Persona();
        }
    }

    private Carrera obtenerCarrera(Integer facultadId, BigDecimal personaId, Integer documentoId, Persona persona) {
        try {
            personaDatosService.sincronizarCarreraAlumno(facultadId, persona.getPersonaId(), persona.getDocumentoId());
        } catch (Exception e) {
            log.error("Sin sincronizar carrera");
        }
        Legajo legajo;
        try {
            legajo = personaDatosService.obtenerLegajo(facultadId, personaId, documentoId);
        } catch (Exception e) {
            log.error("Sin datos de legajo {}: {}", personaId, e.getMessage());
            legajo = new Legajo();
        }
        try {
            return personaDatosService.obtenerCarrera(legajo.getFacultadId(), legajo.getPlanId(), legajo.getCarreraId());
        } catch (Exception e) {
            log.error("Sin datos de carrera {}: {}", legajo.getCarreraId(), e.getMessage());
            return new Carrera();
        }
    }

    private List<LectivoAlternativa> obtenerAlternativas(Integer facultadId, Integer lectivoId,
                                                         Integer tipoChequeraId, Integer alternativaId) {
        try {
            return catalogoDatosService.obtenerAlternativas(facultadId, lectivoId, tipoChequeraId, alternativaId);
        } catch (Exception e) {
            log.error("Sin alternativas para {}/{}/{}/{}: {}", facultadId, lectivoId, tipoChequeraId, alternativaId,
                    e.getMessage());
            return List.of();
        }
    }

}
