package Model.excepciones;

/**
 * ============================================================================
 * CLAVE DUPLICADA
 * ============================================================================
 *
 * Se lanza al intentar insertar una clave que ya existe dentro de la
 * estructura. Las claves son ÚNICAS por decisión de diseño: así las
 * búsquedas lineal y binaria tienen siempre una única respuesta posible.
 */
public class ClaveDuplicadaException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción de la clave repetida detectada.
     */
    public ClaveDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
