package um.tesoreria.sender.hexagonal.datos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraMessageCheck;

public interface RegistrarChequeraMessageCheckUseCase {

    ChequeraMessageCheck registrar(ChequeraMessageCheck messageCheck);

}
