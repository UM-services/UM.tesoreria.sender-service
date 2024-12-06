package um.tesoreria.sender.service.pdf;

import lombok.Builder;
import lombok.Data;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.*;
import um.tesoreria.sender.domain.dto.UMPreferenceMPDto;

import java.util.List;

@Data
@Builder
public class ChequeraDataProvider {
    private ChequeraSerieDto chequeraSerie;
    private List<UMPreferenceMPDto> preferences;
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private PersonaDto persona;
    private LectivoDto lectivo;
    private LegajoDto legajo;
    private CarreraDto carrera;
    private Integer alternativaId;
    private Boolean codigoBarras;
    private Boolean completa;
}
