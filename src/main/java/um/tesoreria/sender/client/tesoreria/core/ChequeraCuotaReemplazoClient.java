package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraCuotaReemplazoDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/chequeraCuotaReemplazo")
public interface ChequeraCuotaReemplazoClient {

    @GetMapping("/chequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaReemplazoDto> findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(@PathVariable Integer facultadId,
                                                      @PathVariable Integer tipoChequeraId, @PathVariable Long chequeraSerieId,
                                                      @PathVariable Integer alternativaId);

}
