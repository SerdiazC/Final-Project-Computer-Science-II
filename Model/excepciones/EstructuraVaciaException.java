package Model.excepciones;

/**
 * ============================================================================
 * ESTRUCTURA VACÍA
 * ============================================================================
 *
 * Se lanza cuando una operación requiere al menos un dato almacenado
 * (por ejemplo, eliminar) y la estructura no contiene ninguno.
 */
public class EstructuraVaciaException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción de la operación intentada sobre la
     *                estructura sin datos.
     */
    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}
