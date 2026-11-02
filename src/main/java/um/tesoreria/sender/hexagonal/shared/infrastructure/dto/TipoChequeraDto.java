package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifyable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TipoChequeraDto implements Jsonifyable {

    private Integer tipoChequeraId;
    private String nombre = "";
    private String prefijo = "";
    private int geograficaId = 1;
    private int claseChequeraId = 2;
    private byte imprimir = 0;
    private byte contado = 0;
    private byte multiple = 0;
    private String emailCopia;
    private GeograficaDto geografica;
    private ClaseChequeraDto claseChequera;

}
