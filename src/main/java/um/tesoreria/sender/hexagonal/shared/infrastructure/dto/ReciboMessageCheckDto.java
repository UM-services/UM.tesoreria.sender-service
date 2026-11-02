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
public class ReciboMessageCheckDto implements Jsonifyable {

    private UUID reciboMessageCheckId;
    private Long facturacionElectronicaId;
    private Long chequeraPagoId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private String payload = "";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID reciboMessageCheckId;
        private Long facturacionElectronicaId;
        private Long chequeraPagoId;
        private Integer facultadId;
        private Integer tipoChequeraId;
        private Long chequeraSerieId;
        private Integer productoId;
        private Integer alternativaId;
        private Integer cuotaId;
        private String payload = "";

        public Builder reciboMessageCheckId(UUID reciboMessageCheckId) {
            this.reciboMessageCheckId = reciboMessageCheckId;
            return this;
        }

        public Builder facturacionElectronicaId(Long facturacionElectronicaId) {
            this.facturacionElectronicaId = facturacionElectronicaId;
            return this;
        }

        public Builder chequeraPagoId(Long chequeraPagoId) {
            this.chequeraPagoId = chequeraPagoId;
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

        public Builder productoId(Integer productoId) {
            this.productoId = productoId;
            return this;
        }

        public Builder alternativaId(Integer alternativaId) {
            this.alternativaId = alternativaId;
            return this;
        }

        public Builder cuotaId(Integer cuotaId) {
            this.cuotaId = cuotaId;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public ReciboMessageCheckDto build() {
            ReciboMessageCheckDto dto = new ReciboMessageCheckDto();
            dto.setReciboMessageCheckId(reciboMessageCheckId);
            dto.setFacturacionElectronicaId(facturacionElectronicaId);
            dto.setChequeraPagoId(chequeraPagoId);
            dto.setFacultadId(facultadId);
            dto.setTipoChequeraId(tipoChequeraId);
            dto.setChequeraSerieId(chequeraSerieId);
            dto.setProductoId(productoId);
            dto.setAlternativaId(alternativaId);
            dto.setCuotaId(cuotaId);
            dto.setPayload(payload);
            return dto;
        }
    }

}
