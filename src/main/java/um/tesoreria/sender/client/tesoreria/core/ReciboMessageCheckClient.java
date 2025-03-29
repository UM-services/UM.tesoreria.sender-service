package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ReciboMessageCheckDto;

import java.util.UUID;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/reciboMessageCheck")
public interface ReciboMessageCheckClient {

    @GetMapping("/{reciboMessageCheckId}")
    ReciboMessageCheckDto findByReciboMessageCheckId(@PathVariable UUID reciboMessageCheckId);

    @PostMapping("/")
    ReciboMessageCheckDto add(@RequestBody ReciboMessageCheckDto reciboMessageCheck);

}
