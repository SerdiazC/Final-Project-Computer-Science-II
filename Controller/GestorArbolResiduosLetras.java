package Controller;

import java.util.List;

import Model.estructuras.EstructuraArbolResiduosLetras;
import Model.estructuras.PasoBusquedaLetras;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * GESTOR DEL ÁRBOL DE BÚSQUEDA POR RESIDUOS (VARIANTE DE LETRAS)
 * ============================================================================
 *
 * Fachada de la vista hacia la estructura de búsqueda por residuos que ubica
 * LETRAS (A-Z) según la posición en el alfabeto y su binario fijo a 5 cifras,
 * con raíz SIEMPRE vacía y NODOS DE ENLACE por colisión.
 *
 * Al igual que el del árbol digital de letras, es un controlador ligero e
 * independiente: sin configuración previa (el usuario digita una letra) y
 * soluciona el reinicio creando una estructura nueva.
 */
public class GestorArbolResiduosLetras {

    /** Estructura de búsqueda por residuos (letras) activa. */
    private EstructuraArbolResiduosLetras arbol;

    /** Crea el gestor con la estructura nueva y vacía. */
    public GestorArbolResiduosLetras() {
        this.arbol = new EstructuraArbolResiduosLetras();
    }

    /**
     * Inserta la letra aplicando la regla del desglose por colisión.
     *
     * @param letra letra A-Z digitada por el usuario.
     * @throws ExcepcionEstructura si la letra no es válida o ya existe.
     */
    public void insertarLetra(char letra) throws ExcepcionEstructura {
        arbol.insertar(letra);
    }

    /**
     * Elimina la letra dejando su nodo vacío (los enlaces se conservan).
     *
     * @param letra letra A-Z a eliminar.
     * @throws ExcepcionEstructura si la letra no es válida o no existe.
     */
    public void eliminarLetra(char letra) throws ExcepcionEstructura {
        arbol.eliminar(letra);
    }

    /**
     * Busca la letra y devuelve los pasos del descenso para animarla.
     *
     * @param letra letra A-Z solicitada.
     * @return pasos de la búsqueda.
     * @throws ExcepcionEstructura si la letra es inválida o el árbol está vacío.
     */
    public List<PasoBusquedaLetras> buscarLetra(char letra)
            throws ExcepcionEstructura {
        return arbol.buscarPasos(letra);
    }

    /**
     * Vacía la estructura dejándola lista para empezar de cero.
     */
    public void reiniciar() {
        this.arbol = new EstructuraArbolResiduosLetras();
    }

    /** @return la estructura activa para consultas de solo lectura. */
    public EstructuraArbolResiduosLetras getEstructura() {
        return arbol;
    }

    /** @return true si la estructura existe y está lista para operar. */
    public boolean isSeleccionHecha() {
        return arbol != null;
    }
}
