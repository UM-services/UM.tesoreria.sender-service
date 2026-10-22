package um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in.CrearContextosChequeraBulkUseCase;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in.CrearContextosChequeraUseCase;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in.ObtenerContextoActivoUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

@Service
@RequiredArgsConstructor
public class PreferenciaPagoDatosService {

    private final CrearContextosChequeraUseCase crearContextosChequeraUseCase;
    private final CrearContextosChequeraBulkUseCase crearContextosChequeraBulkUseCase;
    private final ObtenerContextoActivoUseCase obtenerContextoActivoUseCase;

    public List<PreferenciaPago> crearContexto(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                               Integer alternativaId) {
        return crearContextosChequeraUseCase.crear(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

    public List<PreferenciaPago> crearContextoBulk(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                                   Integer alternativaId) {
        return crearContextosChequeraBulkUseCase.crear(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

    public MercadoPagoContext obtenerContextoActivo(Long chequeraCuotaId) {
        return obtenerContextoActivoUseCase.obtener(chequeraCuotaId);
    }

}
