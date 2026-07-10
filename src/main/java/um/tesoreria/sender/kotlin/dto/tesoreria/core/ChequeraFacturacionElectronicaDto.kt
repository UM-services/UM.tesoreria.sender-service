package um.tesoreria.sender.kotlin.dto.tesoreria.core

import um.tesoreria.sender.util.Jsonifier
import um.tesoreria.sender.util.Jsonifyable

data class ChequeraFacturacionElectronicaDto(

    var chequeraFacturacionElectronicaId: Long? = null,
    var chequeraId: Long? = null,
    var cuit: String = "",
    var razonSocial: String = "",
    var domicilio: String = "",
    var email: String = "",
    var condicionIva: String = ""

) : Jsonifyable