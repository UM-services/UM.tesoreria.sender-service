package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import um.tesoreria.sender.util.Jsonifier
import java.math.BigDecimal
import java.time.OffsetDateTime

data class LegajoDto(

    var legajoId: Long? = null,
    var personaId: BigDecimal? = null,
    var documentoId: Int? = null,
    var facultadId: Int? = null,
    var numeroLegajo: Long = 0L,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fecha: OffsetDateTime? = null,
    var lectivoId: Int? = null,
    var planId: Int? = null,
    var carreraId: Int? = null,
    var tieneCarrera: Byte = 0,
    var geograficaId: Int? = null,
    var contrasenha: String? = null,
    var intercambio: Byte = 0

) {

    fun jsonify(): String {
        return Jsonifier.builder(this).build()
    }

}
