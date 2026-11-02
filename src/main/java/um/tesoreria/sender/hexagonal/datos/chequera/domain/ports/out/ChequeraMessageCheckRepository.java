package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.out;

import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;

public interface ChequeraMessageCheckRepository {

    ChequeraMessageCheck registrar(ChequeraMessageCheck messageCheck);

}
