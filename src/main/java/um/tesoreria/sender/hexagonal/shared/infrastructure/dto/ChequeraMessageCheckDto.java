package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifyable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChequeraMessageCheckDto implements Jsonifyable {

    private UUID chequeraMessageCheckId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private String payload = "";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID chequeraMessageCheckId;
        private Integer facultadId;
        private Integer tipoChequeraId;
        private Long chequeraSerieId;
        private String payload = "";

        public Builder chequeraMessageCheckId(UUID chequeraMessageCheckId) {
            this.chequeraMessageCheckId = chequeraMessageCheckId;
            return this;
        }

        public Builder facultadId(Integer facultadId) {
            this.facultadId = facultadId;
            return this;
        }

        public Builder tipoChequeraId(Integer tipoChequeraId) {
            this.tipoChequeraId = tipoChequeraId;
            return this;
        }

        public Builder chequeraSerieId(Long chequeraSerieId) {
            this.chequeraSerieId = chequeraSerieId;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public ChequeraMessageCheckDto build() {
            ChequeraMessageCheckDto dto = new ChequeraMessageCheckDto();
            dto.setChequeraMessageCheckId(chequeraMessageCheckId);
            dto.setFacultadId(facultadId);
            dto.setTipoChequeraId(tipoChequeraId);
            dto.setChequeraSerieId(chequeraSerieId);
            dto.setPayload(payload);
            return dto;
        }
    }

}
