package um.tesoreria.sender.kotlin.dto.tesoreria.core.message

import java.util.*

data class ReciboMessageDto(

    var uuid: UUID,
    var facturacionElectronicaId: Long,
    var chequeraPagoId: Long? = null,
    var facultadId: Int? = null,
    var tipoChequeraId: Int? = null,
    var chequeraSerieId: Long? = null,
    var productoId: Int? = null,
    var alternativaId: Int? = null,
    var cuotaId: Int? = null

)
