package um.tesoreria.sender.kotlin.dto.tesoreria.core

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.OffsetDateTime

data class FacturacionElectronicaDto(

    var facturacionElectronicaId: Long? = null,
    var chequeraPagoId: Long? = null,
    var comprobanteId: Int? = null,
    var numeroComprobante: Long = 0,
    var personaId: BigDecimal? = null,
    var tipoDocumento: String? = null,
    var apellido: String? = null,
    var nombre: String? = null,
    var cuit: String? = null,
    var condicionIva: String = "",
    var importe: BigDecimal = BigDecimal.ZERO,
    var cae: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fechaRecibo: OffsetDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fechaVencimientoCae: OffsetDateTime? = null,
    var enviada: Byte = 0,
    var retries: Int = 0,
    var chequeraPago: ChequeraPagoDto? = null,
    var comprobante: ComprobanteDto? = null

)
