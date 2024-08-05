package um.tesoreria.sender.kotlin.dto.tesoreria.core

import java.math.BigDecimal

data class FacultadDto(

    var facultadId: Int? = null,
    var nombre: String = "",
    var codigoempresa: String = "",
    var server: String = "",
    var dbadm: String = "",
    var dsn: String = "",
    var cuentacontable: BigDecimal = BigDecimal.ZERO,
    var apiserver: String = "",
    var apiport: Long = 0L

)
