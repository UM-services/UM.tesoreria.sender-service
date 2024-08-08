package um.tesoreria.sender.kotlin.dto.tesoreria.core

data class CarreraDto(

    var uniqueId: Long? = null,
    var facultadId: Int? = null,
    var planId: Int? = null,
    var carreraId: Int? = null,
    var nombre: String = "",
    var iniciales: String = "",
    var titulo: String = "",
    var trabajofinal: Byte = 0,
    var resolucion: String = "",
    var chequeraunica: Byte = 0,
    var bloqueId: Int? = null,
    var obligatorias: Int = 0,
    var optativas: Int = 0,
    var vigente: Byte = 0,

    )
