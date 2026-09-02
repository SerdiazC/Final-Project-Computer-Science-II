package Controller;

import java.util.List;

import Model.estructuras.EstructuraArbolDigitalLetras;
import Model.estructuras.PasoBusquedaLetras;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * GESTOR DEL ÁRBOL DIGITAL DE LETRAS (CONTROLADOR / FACHADA)
 * ============================================================================
 *
 * Punto único de contacto de la vista con el árbol digital de letras
 * (búsqueda por residuos digital A-Z).
 *
 * REGLA: NO hay configuración previa (ni dígitos ni tamaño): el usuario digita
 * directamente una letra y el árbol crece con su propia regla de bits. Por
 * eso este gestor es más ligero que el interno: nace listo para operar y solo
 * necesita "reiniciarse" para vaciarse.
 *
 * Este controlador es totalmente INDEPENDIENTE del GestorBusquedas interno:
 * no toca ni reutiliza la lógica de las búsquedas internas numéricas.
 */
public class GestorArbolDigitalLetras {

    /** Árbol digital de letras activo. */
    private EstructuraArbolDigitalLetras arbol;

    /**
     * Crea el gestor con un árbol vacío y listo para insertar letras.
     */
    public GestorArbolDigitalLetras() {
        this.arbol = new EstructuraArbolDigitalLetras();
    }

    /**
     * Inserta la letra en el árbol aplicando su regla de bits.
     *
     * @param letra letra A-Z digitada por el usuario.
     * @throws ExcepcionEstructura si la letra no es válida o ya existe.
     */
    public void insertarLetra(char letra) throws ExcepcionEstructura {
        arbol.insertar(letra);
    }

    /**
     * Elimina la letra del árbol (el nodo queda vacío conservando sus hijos).
     *
     * @param letra letra A-Z a eliminar.
     * @throws ExcepcionEstructura si la letra no es válida o no existe.
     */
    public void eliminarLetra(char letra) throws ExcepcionEstructura {
        arbol.eliminar(letra);
    }

    /**
     * Busca la letra y devuelve los pasos de la búsqueda para animarla.
     *
     * @param letra letra A-Z solicitada.
     * @return pasos de la búsqueda nodo a nodo.
     * @throws ExcepcionEstructura si la letra no es válida o el árbol está vacío.
     */
    public List<PasoBusquedaLetras> buscarLetra(char letra)
            throws ExcepcionEstructura {
        return arbol.buscarPasos(letra);
    }

    /**
     * Vacía el árbol dejándolo listo para empezar de cero.
     */
    public void reiniciar() {
        this.arbol = new EstructuraArbolDigitalLetras();
    }

    /** @return la estructura activa para consultas de solo lectura. */
    public EstructuraArbolDigitalLetras getEstructura() {
        return arbol;
    }

    /** @return true si el árbol tiene letras colocadas. */
    public boolean isSeleccionHecha() {
        return arbol != null;
    }
}