package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import um.tesoreria.sender.util.Jsonifier
import java.math.BigDecimal
import java.time.OffsetDateTime

data class ChequeraSerieDto(

    var chequeraId: Long? = null,
    var facultadId: Int? = null,
    var tipoChequeraId: Int? = null,
    var chequeraSerieId: Long? = null,
    var personaId: BigDecimal? = null,
    var documentoId: Int? = null,
    var lectivoId: Int? = null,
    var arancelTipoId: Int? = null,
    var cursoId: Int? = null,
    var asentado: Byte? = null,
    var geograficaId: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fecha: OffsetDateTime? = null,
    var cuotasPagadas: Int? = null,
    var observaciones: String? = null,
    var alternativaId: Int? = null,
    var algoPagado: Byte? = null,
    var tipoImpresionId: Int? = null,
    var flagPayperTic: Byte = 0,
    var usuarioId: String? = null,
    var enviado: Byte = 0,
    var retenida: Byte = 0,
    var version: Long? = null,
    var cuotasDeuda: Int = 0,
    var importeDeuda: BigDecimal = BigDecimal.ZERO,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var ultimoEnvio: OffsetDateTime? = null,
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
