package um.tesoreria.sender.hexagonal.datos.chequera.application.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ActualizarCodigosBarrasUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.MarcarChequeraEnviadaUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraCuotaUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraFacturacionElectronicaUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraPagoUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraReemplazoUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraSerieUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.RegistrarChequeraMessageCheckUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;

@Service
@RequiredArgsConstructor
public class ChequeraDatosService {

    private final ObtenerChequeraSerieUseCase obtenerChequeraSerieUseCase;
    private final MarcarChequeraEnviadaUseCase marcarChequeraEnviadaUseCase;
    private final ObtenerChequeraCuotaUseCase obtenerChequeraCuotaUseCase;
    private final ActualizarCodigosBarrasUseCase actualizarCodigosBarrasUseCase;
    private final ObtenerChequeraPagoUseCase obtenerChequeraPagoUseCase;
    private final ObtenerChequeraFacturacionElectronicaUseCase obtenerChequeraFacturacionElectronicaUseCase;
    private final ObtenerChequeraReemplazoUseCase obtenerChequeraReemplazoUseCase;
    private final RegistrarChequeraMessageCheckUseCase registrarChequeraMessageCheckUseCase;

    public ChequeraSerie obtenerSerie(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return obtenerChequeraSerieUseCase.obtener(facultadId, tipoChequeraId, chequeraSerieId);
    }

    public ChequeraSerie marcarEnviada(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return marcarChequeraEnviadaUseCase.marcar(facultadId, tipoChequeraId, chequeraSerieId);
    }

    public ChequeraCuota obtenerCuotaPorId(Long chequeraCuotaId) {
        return obtenerChequeraCuotaUseCase.obtenerPorId(chequeraCuotaId);
    }

    public ChequeraCuota obtenerCuotaPorClave(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                              Integer productoId, Integer alternativaId, Integer cuotaId) {
        return obtenerChequeraCuotaUseCase.obtenerPorClave(facultadId, tipoChequeraId, chequeraSerieId, productoId,
                alternativaId, cuotaId);
    }

    public void actualizarCodigosBarras(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        actualizarCodigosBarrasUseCase.actualizar(facultadId, tipoChequeraId, chequeraSerieId);
    }

    public ChequeraPago obtenerPago(Long chequeraPagoId) {
        return obtenerChequeraPagoUseCase.obtener(chequeraPagoId);
    }

    public Optional<ChequeraFacturacionElectronica> obtenerFacturacionElectronica(Long chequeraId) {
        return obtenerChequeraFacturacionElectronicaUseCase.obtenerPorChequeraId(chequeraId);
    }

    public ChequeraSerieReemplazo obtenerSerieReemplazo(Integer facultadId, Integer tipoChequeraId,
                                                       Long chequeraSerieId) {
        return obtenerChequeraReemplazoUseCase.obtenerSerie(facultadId, tipoChequeraId, chequeraSerieId);
    }

    public List<ChequeraCuotaReemplazo> obtenerCuotasReemplazo(Integer facultadId, Integer tipoChequeraId,
                                                               Long chequeraSerieId, Integer alternativaId) {
        return obtenerChequeraReemplazoUseCase.obtenerCuotas(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId);
    }

    public ChequeraMessageCheck registrarMessageCheck(ChequeraMessageCheck messageCheck) {
        return registrarChequeraMessageCheckUseCase.registrar(messageCheck);
    }

}
