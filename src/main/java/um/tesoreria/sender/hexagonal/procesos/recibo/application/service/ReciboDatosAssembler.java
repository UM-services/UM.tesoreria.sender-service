package um.tesoreria.sender.hexagonal.procesos.recibo.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.service.FacturacionDatosService;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.model.DatosRecibo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.Comprobante;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReciboDatosAssembler {

    private final FacturacionDatosService facturacionDatosService;
    private final ChequeraDatosService chequeraDatosService;

    public DatosRecibo armar(Long facturacionElectronicaId, FacturacionElectronica facturacionElectronica) {
        FacturacionElectronica factura = facturacionElectronica != null
                ? facturacionElectronica
                : facturacionDatosService.obtener(facturacionElectronicaId);
        log.debug("FacturacionElectronica -> {}", factura);

        Comprobante comprobante = factura.getComprobante() != null
                ? factura.getComprobante()
                : facturacionDatosService.obtenerComprobante(factura.getComprobanteId());

        var chequeraPago = factura.getChequeraPago() != null
                ? factura.getChequeraPago()
                : chequeraDatosService.obtenerPago(factura.getChequeraPagoId());
        log.debug("ChequeraPago -> {}", chequeraPago);

        var chequeraCuota = chequeraDatosService.obtenerCuotaPorClave(chequeraPago.getFacultadId(),
                chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getProductoId(),
                chequeraPago.getAlternativaId(), chequeraPago.getCuotaId());
        log.debug("ChequeraCuota -> {}", chequeraCuota);

        var chequeraSerie = chequeraDatosService.obtenerSerie(chequeraPago.getFacultadId(),
                chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId());

        ChequeraFacturacionElectronica chequeraFacturacionElectronica;
        try {
            chequeraFacturacionElectronica = chequeraDatosService
                    .obtenerFacturacionElectronica(chequeraSerie.getChequeraId())
                    .orElseGet(ChequeraFacturacionElectronica::new);
        } catch (Exception e) {
            log.error("Sin datos particulares");
            chequeraFacturacionElectronica = new ChequeraFacturacionElectronica();
        }

        return new DatosRecibo(factura, comprobante, chequeraPago, chequeraCuota, chequeraSerie,
                chequeraFacturacionElectronica);
    }

}
