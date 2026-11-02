package um.tesoreria.sender.hexagonal.shared.domain.ports.out;

public interface DireccionCorreoValidador {

    boolean esValida(String email);

}
