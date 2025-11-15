package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import um.tesoreria.sender.util.Jsonifier
import java.math.BigDecimal
import java.time.OffsetDateTime

data class ChequeraCuotaDto(

    var chequeraCuotaId: Long? = null,
    var chequeraId: Long? = null,
    var facultadId: Int? = null,
    var tipoChequeraId: Int? = null,
    var chequeraSerieId: Long? = null,
    var productoId: Int? = null,
    var alternativaId: Int? = null,
    var cuotaId: Int? = null,
    var mes: Int = 0,
    var anho: Int = 0,
    var arancelTipoId: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var vencimiento1: OffsetDateTime? = null,
    var importe1: BigDecimal = BigDecimal.ZERO,
    var importe1Original: BigDecimal = BigDecimal.ZERO,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var vencimiento2: OffsetDateTime? = null,
    var importe2: BigDecimal = BigDecimal.ZERO,
    var importe2Original: BigDecimal = BigDecimal.ZERO,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var vencimiento3: OffsetDateTime? = null,
    var importe3: BigDecimal = BigDecimal.ZERO,
    var importe3Original: BigDecimal = BigDecimal.ZERO,
    var codigoBarras: String = "",
    var i2Of5: String = "",
    var pagado: Byte = 0,
    var baja: Byte = 0,
    var manual: Byte = 0,
    var compensada: Byte = 0,
    var tramoId: Int = 0,
    var facultad: FacultadDto? = null,
    var tipoChequera: TipoChequeraDto? = null,
    var producto: ProductoDto? = null,
    var chequeraSerie: ChequeraSerieDto? = null

) {

    fun jsonify(): String {
        return Jsonifier.builder(this).build()
    }

}
