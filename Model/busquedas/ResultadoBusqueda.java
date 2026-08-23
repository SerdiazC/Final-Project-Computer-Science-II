package Model.busquedas;

import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * RESULTADO DE BÚSQUEDA
 * ============================================================================
 *
 * Encapsula TODO lo que una búsqueda produce:
 *
 *  - Si encontró o no la clave ("no encontrar" es un resultado normal,
 *    NO una excepción: así la vista puede mostrarlo gráficamente).
 *  - El índice donde quedó hallada (o -1).
 *  - La lista completa de pasos para reproducir la animación en la web.
 *  - Un mensaje final legible para el usuario.
 *
 * Es inmutable: una vez creada no cambia, por lo que puede pasarse a
 * cualquier vista sin riesgo de alteraciones.
 */
public class ResultadoBusqueda {

    /** Clave que se estaba buscando. */
    private final int claveBuscada;

    /** true si la clave existe en la estructura. */
    private final boolean encontrada;

    /** Índice donde se halló la clave, o -1 si no fue encontrada. */
    private final int indiceEncontrado;

    /** Pasos registrados durante el proceso (solo lectura hacia afuera). */
    private final List<PasoBusqueda> pasos;

    /** Conclusión final legible de la búsqueda. */
    private final String mensaje;

    /**
     * Constructor privado: use las fábricas {@link #exitosa} y
     * {@link #fallida} para dejar claro el desenlace de la búsqueda.
     */
    private ResultadoBusqueda(int claveBuscada, boolean encontrada,
                              int indiceEncontrado, List<PasoBusqueda> pasos,
                              String mensaje) {
        this.claveBuscada = claveBuscada;
        this.encontrada = encontrada;
        this.indiceEncontrado = indiceEncontrado;
        this.pasos = Collections.unmodifiableList(pasos);
        this.mensaje = mensaje;
    }

    /**
     * Crea el resultado de una búsqueda con éxito.
     *
     * @param claveBuscada     clave solicitada por el usuario.
     * @param indiceEncontrado posición donde quedó hallada.
     * @param pasos            comparaciones realizadas hasta hallarla.
     * @return resultado marcado como encontrado.
     */
    public static ResultadoBusqueda exitosa(int claveBuscada, int indiceEncontrado,
                                            List<PasoBusqueda> pasos) {
        return new ResultadoBusqueda(claveBuscada, true, indiceEncontrado, pasos,
                "La clave " + claveBuscada + " SÍ se encuentra en la estructura, "
                        + "en la posición " + indiceEncontrado + ".");
    }

    /**
     * Crea el resultado de una búsqueda sin éxito (el "error" visible).
     *
     * @param claveBuscada clave solicitada por el usuario.
     * @param mensaje      explicación de por qué no se halló.
     * @param pasos        comparaciones realizadas antes de rendirse.
     * @return resultado marcado como no encontrado.
     */
    public static ResultadoBusqueda fallida(int claveBuscada, String mensaje,
                                            List<PasoBusqueda> pasos) {
        return new ResultadoBusqueda(claveBuscada, false, -1, pasos, mensaje);
    }

    /** @return clave que se buscó. */
    public int getClaveBuscada() {
        return claveBuscada;
    }

    /** @return true si la clave fue hallada. */
    public boolean isEncontrada() {
        return encontrada;
    }

    /** @return posición de la clave hallada, o -1. */
    public int getIndiceEncontrado() {
        return indiceEncontrado;
    }

    /** @return pasos de la búsqueda (lista inmodificable). */
    public List<PasoBusqueda> getPasos() {
        return pasos;
    }

    /** @return conclusión final legible de la búsqueda. */
    public String getMensaje() {
        return mensaje;
    }
}
