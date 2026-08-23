package Model.excepciones;

/**
 * ============================================================================
 * EXCEPCIÓN BASE DEL MODELO
 * ============================================================================
 *
 * Todas las excepciones propias del dominio (estructuras y búsquedas)
 * heredan de esta clase. Esto permite al controlador y a la vista capturar
 * UN solo tipo general (ExcepcionEstructura) y, si lo necesitan, refinar
 * por subtipo sin acoplarse a detalles internos del modelo.
 *
 * Es "checked" (hereda de Exception) para obligar a quien invoca a pensar
 * qué hacer cuando la regla de negocio se incumple.
 */
public class ExcepcionEstructura extends Exception {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción legible del error de negocio ocurrido.
     */
    public ExcepcionEstructura(String mensaje) {
        super(mensaje);
    }
}
