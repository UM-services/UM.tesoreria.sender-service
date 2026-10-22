package um.tesoreria.sender.hexagonal.shared.domain.model;

/**
 * Resultado de la generación de un PDF. La ruta es el contrato operativo actual
 * (path.reports): el archivo queda en disco para adjuntarlo al correo y para
 * que el controlador lo transmita.
 */
public record DocumentoPdf(String ruta) {

    public static DocumentoPdf vacio() {
        return new DocumentoPdf(null);
    }

    public static DocumentoPdf de(String ruta) {
        return new DocumentoPdf(ruta);
    }

    public boolean estaVacio() {
        return ruta == null || ruta.isEmpty();
    }

}
