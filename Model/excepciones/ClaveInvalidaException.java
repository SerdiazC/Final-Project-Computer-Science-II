package Model.excepciones;

/**
 * ============================================================================
 * CLAVE INVÁLIDA
 * ============================================================================
 *
 * Se lanza cuando la clave no cumple las reglas acordadas:
 *
 *  1. La cantidad de dígitos de la estructura debe estar entre 1 y 7.
 *  2. La clave debe tener EXACTAMENTE la misma cantidad de dígitos con la
 *     que se creó la estructura (los ceros a la izquierda no cuentan:
 *     una clave de 4 dígitos va de 1000 a 9999).
 */
public class ClaveInvalidaException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción de por qué la clave fue rechazada.
     */
    public ClaveInvalidaException(String mensaje) {
        super(mensaje);
    }
}
