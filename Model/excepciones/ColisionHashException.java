package Model.excepciones;

/**
 * ============================================================================
 * COLISIÓN HASH
 * ============================================================================
 *
 * Se lanza cuando una función de transformación de claves calcula una
 * dirección que YA está ocupada por otra clave distinta.
 *
 * Política acordada para este proyecto: la inserción se RECHAZA y se
 * informa la colisión (no se buscan posiciones alternativas).
 */
public class ColisionHashException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción de la colisión detectada (dirección y
     *                clave que ocupa el espacio).
     */
    public ColisionHashException(String mensaje) {
        super(mensaje);
    }
}
