package um.tesoreria.sender.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleMailTestRequest {
    private String token;
    private String to;
    private String subject;
    private String body;
}
