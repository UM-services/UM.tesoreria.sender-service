package um.tesoreria.sender.kotlin.dto.tesoreria.core.message

import java.util.UUID

data class ChequeraMessageDto(

    var uuid: UUID,
    var facultadId: Int,
    var tipoChequeraId: Int,
    var chequeraSerieId: Long,
    var alternativaId: Int,
    var copiaInformes: Boolean,
    var codigoBarras: Boolean,
    var incluyeMatricula: Boolean

)
