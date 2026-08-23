package Model.excepciones;

/**
 * ============================================================================
 * CONFIGURACIÓN INVÁLIDA
 * ============================================================================
 *
 * Se lanza cuando los parámetros de creación de una estructura no cumplen
 * las reglas del proyecto:
 *
 *  - Cantidad de dígitos de las claves fuera del rango 1..7.
 *  - Tamaño de la estructura fuera del rango 1..1000.
 */
public class ConfiguracionInvalidaException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción del parámetro de configuración rechazado.
     */
    public ConfiguracionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
