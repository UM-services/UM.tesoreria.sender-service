package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import um.tesoreria.sender.util.Jsonifier
import java.time.OffsetDateTime

data class LectivoDto(

    var lectivoId: Int? = null,
    var nombre: String = "",
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fechaInicio: OffsetDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fechaFinal: OffsetDateTime? = null

) {

    fun jsonify(): String {
        return Jsonifier.builder(this).build()
    }

}
