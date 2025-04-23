package um.tesoreria.sender.client.tesoreria.core.facade;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/tool")
public interface ToolClient {

    @PostMapping("/mailvalidate")
    Boolean mailValidate(@RequestBody List<String> mailes);

}
