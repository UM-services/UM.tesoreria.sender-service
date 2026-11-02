package um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.exception.PreferenciaPagoException;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.in.ObtenerContextoActivoUseCase;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.domain.ports.out.MercadoPagoContextRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;

@Component
@RequiredArgsConstructor
public class ObtenerContextoActivoUseCaseImpl implements ObtenerContextoActivoUseCase {

    private final MercadoPagoContextRepository mercadoPagoContextRepository;

    @Override
    public MercadoPagoContext obtener(Long chequeraCuotaId) {
        return mercadoPagoContextRepository.findActivoByChequeraCuotaId(chequeraCuotaId)
                .orElseThrow(() -> new PreferenciaPagoException(
                        "No hay contexto de MercadoPago activo para la cuota " + chequeraCuotaId));
    }

}
