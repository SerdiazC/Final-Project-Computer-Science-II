package Model.excepciones;

/**
 * ============================================================================
 * CLAVE NO ENCONTRADA
 * ============================================================================
 *
 * Se lanza cuando una operación exige que la clave exista (por ejemplo,
 * eliminar) y tras recorrer la estructura la clave no está presente.
 *
 * NOTA DE DISEÑO: la operación "buscar" NO lanza esta excepción; devuelve un
 * objeto ResultadoBusqueda con la bandera encontrada=false y sus pasos, pues
 * "no encontrar" es un resultado normal y visualizable de una búsqueda.
 */
public class ClaveNoEncontradaException extends ExcepcionEstructura {

    /** Identificador de versión para la serialización. */
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción de la búsqueda sin éxito.
     */
    public ClaveNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
