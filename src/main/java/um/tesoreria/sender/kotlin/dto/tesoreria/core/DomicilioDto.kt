package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.OffsetDateTime

data class DomicilioDto(

    var domicilioId: Long? = null,
    var personaId: BigDecimal? = null,
    var documentoId: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fecha: OffsetDateTime? = OffsetDateTime.now(),
    var calle: String = "",
    var puerta: String = "",
    var piso: String = "",
    var dpto: String = "",
    var telefono: String = "",
    var movil: String = "",
    var observaciones: String = "",
    var codigoPostal: String = "",
    var facultadId: Int? = null,
    var provinciaId: Int? = null,
    var localidadId: Int? = null,
    var emailPersonal: String = "",
    var emailInstitucional: String = "",
    var laboral: String = ""

)
