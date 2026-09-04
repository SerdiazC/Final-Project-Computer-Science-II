package Controller;

import Model.estructuras.EstructuraArbolHuffman;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * GESTOR DEL ÁRBOL DE HUFFMAN
 * ============================================================================
 *
 * Fachada de la vista hacia el árbol de Huffman. Orquesta el procesamiento de
 * UNA palabra digitada por el usuario: valida el texto (solo letras A-Z y el
 * guion bajo '_'), construye la estructura (conteo de frecuencias, orden de la
 * tabla, agrupación hasta la ecuación final y asignación de códigos) y la
 * mantiene lista para que la vista la consulte.
 *
 * A diferencia de los gestores de los árboles por residuos (que siempre tienen
 * una estructura cargada), aquí la estructura solo existe DESPUÉS de procesar
 * una palabra; con <code>isSeleccionHecha()</code> la vista sabe si ya hay un
 * árbol generado o si la hoja sigue limpia.
 */
public class GestorArbolHuffman {

    /** Estructura activa del árbol de Huffman (null si aún no se procesa). */
    private EstructuraArbolHuffman arbol;

    /** Crea el gestor sin estructura (la hoja empieza limpia). */
    public GestorArbolHuffman() {
        this.arbol = null;
    }

    /**
     * Procesa una palabra y genera el árbol de Huffman. La palabra admite
     * solo letras A-Z (se normaliza a mayúsculas) y el guion bajo '_'.
     *
     * @param palabra palabra digitada por el usuario.
     * @throws ExcepcionEstructura si la palabra es inválida o está vacía.
     */
    public void procesar(String palabra) throws ExcepcionEstructura {
        String normalizada = normalizar(palabra);
        this.arbol = new EstructuraArbolHuffman(normalizada);
    }

    /**
     * Normaliza y valida la palabra: mayúsculas y rechazo de caracteres que
     * no sean letras o el guion bajo.
     *
     * @param palabra texto digitado.
     * @return palabra normalizada en mayúsculas.
     * @throws IllegalArgumentException si está vacía.
     */
    private String normalizar(String palabra) {
        if (palabra == null || palabra.isBlank()) {
            throw new IllegalArgumentException(
                    "Digite una palabra para generar el árbol.");
        }
        return palabra.trim().toUpperCase();
    }

    /** Vacía la estructura dejando la hoja lista para una palabra nueva. */
    public void reiniciar() {
        this.arbol = null;
    }

    /** @return la estructura activa (null si aún no se procesa una palabra). */
    public EstructuraArbolHuffman getEstructura() {
        return arbol;
    }

    /** @return true si ya hay un árbol de Huffman generado. */
    public boolean isSeleccionHecha() {
        return arbol != null;
    }
}