package um.tesoreria.sender.hexagonal.datos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.chequera.application.exception.ChequeraDatosException;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in.ObtenerChequeraPagoUseCase;
import um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out.ChequeraPagoRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;

@Component
@RequiredArgsConstructor
public class ObtenerChequeraPagoUseCaseImpl implements ObtenerChequeraPagoUseCase {

    private final ChequeraPagoRepository chequeraPagoRepository;

    @Override
    public ChequeraPago obtener(Long chequeraPagoId) {
        return chequeraPagoRepository.findByChequeraPagoId(chequeraPagoId)
                .orElseThrow(() -> new ChequeraDatosException("No se encontró el pago " + chequeraPagoId));
    }

}
