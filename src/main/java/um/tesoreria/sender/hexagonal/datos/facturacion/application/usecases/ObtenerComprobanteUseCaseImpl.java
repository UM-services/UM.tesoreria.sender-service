package um.tesoreria.sender.hexagonal.datos.facturacion.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.exception.FacturacionDatosException;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.in.ObtenerComprobanteUseCase;
import um.tesoreria.sender.hexagonal.datos.facturacion.domain.ports.out.ComprobanteRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;

@Component
@RequiredArgsConstructor
public class ObtenerComprobanteUseCaseImpl implements ObtenerComprobanteUseCase {

    private final ComprobanteRepository comprobanteRepository;

    @Override
    public Comprobante obtener(Integer comprobanteId) {
        return comprobanteRepository.findByComprobanteId(comprobanteId)
                .orElseThrow(() -> new FacturacionDatosException("No se encontró el comprobante " + comprobanteId));
    }

}
