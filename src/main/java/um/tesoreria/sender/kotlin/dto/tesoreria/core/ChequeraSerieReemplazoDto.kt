package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import um.tesoreria.sender.util.Jsonifier
import java.math.BigDecimal
import java.time.OffsetDateTime

data class ChequeraSerieReemplazoDto(

    var chequeraReemplazoId: Long? = null,
    var facultadId: Int? = null,
    var tipoChequeraId: Int? = null,
    var chequeraSerieId: Long? = null,
    var personaId: BigDecimal? = null,
    var documentoId: Int? = null,
    var lectivoId: Int? = null,
    var arancelTipoId: Long? = null,
    var cursoId: Int? = null,
    var asentado: Int = 0,
    var geograficaId: Int? = 1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fecha: OffsetDateTime,
    var cuotasPagadas: Int = 0,
    var observaciones: String = "",
    var alternativaId: Int? = 1,
    var algoPagado: Byte = 0,
    var tipoImpresionId: Int? = null,
    var facultad: FacultadDto? = null,
    var tipoChequera: TipoChequeraDto? = null,
    var persona: PersonaDto? = null,
    var domicilio: DomicilioDto? = null,
    var lectivo: LectivoDto? = null,
    var arancelTipo: ArancelTipoDto? = null,
    var geografica: GeograficaDto? = null

) {

    fun jsonify(): String {
        return Jsonifier.builder(this).build()
    }

}
