package Model.excepciones;

/**
 * ============================================================================
 * ESTRUCTURA LLENA
 * ============================================================================
 *
 * Se lanza al intentar insertar cuando los 100 espacios disponibles
 * ya están ocupados.
 */
public class EstructuraLlenaException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción de la inserción que agotó la capacidad.
     */
    public EstructuraLlenaException(String mensaje) {
        super(mensaje);
    }
}
