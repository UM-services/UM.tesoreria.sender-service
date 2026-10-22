package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraCuotaReemplazoDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraCuotaReemplazoClient",
        path = "/api/tesoreria/core/chequeraCuotaReemplazo")
public interface ChequeraCuotaReemplazoClient {

    @GetMapping("/chequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaReemplazoDto> findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(
            @PathVariable Integer facultadId,
            @PathVariable Integer tipoChequeraId,
            @PathVariable Long chequeraSerieId,
            @PathVariable Integer alternativaId);

}
