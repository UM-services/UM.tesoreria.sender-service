package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraMessageCheckDto;

import java.util.UUID;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraMessageCheckClient", path = "/api/tesoreria/core/chequeraMessageCheck")
public interface ChequeraMessageCheckClient {

    @GetMapping("/{chequeraMessageCheckId}")
    ChequeraMessageCheckDto findByChequeraMessageCheckId(@PathVariable UUID chequeraMessageCheckId);

    @PostMapping("/")
    ChequeraMessageCheckDto add(@RequestBody ChequeraMessageCheckDto chequeraMessageCheck);

}