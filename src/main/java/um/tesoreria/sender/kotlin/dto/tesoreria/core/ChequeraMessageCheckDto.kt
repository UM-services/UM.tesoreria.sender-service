package um.tesoreria.sender.kotlin.dto.tesoreria.core

import um.tesoreria.sender.util.Jsonifier
import java.util.*

data class ChequeraMessageCheckDto (

    var chequeraMessageCheckId: UUID? = null,
    var facultadId: Int? = null,
    var tipoChequeraId: Int? = null,
    var chequeraSerieId: Long? = null,
    var payload: String = ""

) {
    fun jsonify(): String {
        return Jsonifier.builder(this).build()
    }

    class Builder {
        private var chequeraMessageCheckId: UUID? = null
        private var facultadId: Int? = null
        private var tipoChequeraId: Int? = null
        private var chequeraSerieId: Long? = null
        private var payload: String = ""

        fun chequeraMessageCheckId(chequeraMessageCheckId: UUID?) = apply { 
            this.chequeraMessageCheckId = chequeraMessageCheckId 
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

        fun payload(payload: String) = apply {
            this.payload = payload 
        }

        fun build() = ChequeraMessageCheckDto(
            chequeraMessageCheckId = chequeraMessageCheckId,
            facultadId = facultadId,
            tipoChequeraId = tipoChequeraId,
            chequeraSerieId = chequeraSerieId,
            payload = payload
        )
    }

    companion object {
        fun builder() = Builder()
    }
}
