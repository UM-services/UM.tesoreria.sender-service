package um.tesoreria.sender.hexagonal.datos.validacionEmail.infrastructure.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "tesoreria-core-service", contextId = "validacionEmailClient",
        path = "/api/tesoreria/core/tool")
public interface ValidacionEmailClient {

    @PostMapping("/mailvalidate")
    Boolean mailValidate(@RequestBody List<String> emails);

}
