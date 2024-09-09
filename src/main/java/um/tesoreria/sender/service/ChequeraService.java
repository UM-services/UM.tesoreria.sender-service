package um.tesoreria.sender.service;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

@Service
@Slf4j
public class ChequeraService {

    private final FormulariosToPdfService formulariosToPdfService;

    public ChequeraService(FormulariosToPdfService formulariosToPdfService) {
        this.formulariosToPdfService = formulariosToPdfService;
    }

    public String sendChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                               Boolean copiaInformes, Boolean incluyeMatricula) throws MessagingException {
        String filename = "";
        return "";
    }

    public String sendCuota(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId, Integer productoId, Integer cuotaId,
                            Boolean copiaInformes, Boolean incluyeMatricula) throws MessagingException {
        return "";
    }

}
