package um.tesoreria.sender.kotlin.dto.tesoreria.core

data class LectivoAlternativaDto(

    var lectivoAlternativaId: Long? = null,
    var facultadId: Int? = null,
    var lectivoId: Int? = null,
    var tipoChequeraId: Int? = null,
    var productoId: Int? = null,
    var alternativaId: Int? = null,
    var titulo: String? = "",
    var cuotas: Int? = 0,
    var descuentoPagoTermino: Int? = 0

)
