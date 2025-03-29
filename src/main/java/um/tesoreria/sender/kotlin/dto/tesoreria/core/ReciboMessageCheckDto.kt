package um.tesoreria.sender.kotlin.dto.tesoreria.core

import java.util.UUID

data class ReciboMessageCheckDto(

    var reciboMessageCheckId: UUID? = null,
    var facturacionElectronicaId: Long? = null,
    var chequeraPagoId: Long? = null,
    var facultadId: Int? = null,
    var tipoChequeraId: Int? = null,
    var chequeraSerieId: Long? = null,
    var productoId: Int? = null,
    var alternativaId: Int? = null,
    var cuotaId: Int? = null,
    var payload: String = ""

) {
    class Builder {
        private var reciboMessageCheckId: UUID? = null
        private var facturacionElectronicaId: Long? = null
        private var chequeraPagoId: Long? = null
        private var facultadId: Int? = null
        private var tipoChequeraId: Int? = null
        private var chequeraSerieId: Long? = null
        private var productoId: Int? = null
        private var alternativaId: Int? = null
        private var cuotaId: Int? = null
        private var payload: String = ""

        fun reciboMessageCheckId(reciboMessageCheckId: UUID?) = apply {
            this.reciboMessageCheckId = reciboMessageCheckId
        }

        fun facturacionElectronicaId(facturacionElectronicaId: Long?) = apply {
            this.facturacionElectronicaId = facturacionElectronicaId
        }

        fun chequeraPagoId(chequeraPagoId: Long?) = apply {
            this.chequeraPagoId = chequeraPagoId
        }

        fun facultadId(facultadId: Int?) = apply {
            this.facultadId = facultadId
        }

        fun tipoChequeraId(tipoChequeraId: Int?) = apply {
            this.tipoChequeraId = tipoChequeraId
        }

        fun chequeraSerieId(chequeraSerieId: Long?) = apply {
            this.chequeraSerieId = chequeraSerieId
        }

        fun productoId(productoId: Int?) = apply {
            this.productoId = productoId
        }

        fun alternativaId(alternativaId: Int?) = apply {
            this.alternativaId = alternativaId
        }

        fun cuotaId(cuotaId: Int?) = apply {
            this.cuotaId = cuotaId
        }

        fun payload(payload: String) = apply {
            this.payload = payload
        }

        fun build() = ReciboMessageCheckDto(
            reciboMessageCheckId = reciboMessageCheckId,
            facturacionElectronicaId = facturacionElectronicaId,
            chequeraPagoId = chequeraPagoId,
            facultadId = facultadId,
            tipoChequeraId = tipoChequeraId,
            chequeraSerieId = chequeraSerieId,
            productoId = productoId,
            alternativaId = alternativaId,
            cuotaId = cuotaId,
            payload = payload
        )
    }

    companion object {
        fun builder() = Builder()
    }
}
