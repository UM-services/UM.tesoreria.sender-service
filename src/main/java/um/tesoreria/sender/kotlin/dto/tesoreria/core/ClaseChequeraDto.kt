package um.tesoreria.sender.kotlin.dto.tesoreria.core

data class ClaseChequeraDto(

    var claseChequeraId: Int? = null,
    var nombre: String? = null,
    var preuniversitario: Byte = 0,
    var grado: Byte = 0,
    var posgrado: Byte = 0,
    var curso: Byte = 0,
    var secundario: Byte = 0,
    var titulo: Byte = 0

)
