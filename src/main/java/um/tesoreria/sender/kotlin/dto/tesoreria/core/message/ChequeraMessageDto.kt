package um.tesoreria.sender.kotlin.dto.tesoreria.core.message

data class ChequeraMessageDto(

    var facultadId: Int,
    var tipoChequeraId: Int,
    var chequeraSerieId: Long,
    var alternativaId: Int,
    var copiaInformes: Boolean,
    var codigoBarras: Boolean,
    var incluyeMatricula: Boolean

)
