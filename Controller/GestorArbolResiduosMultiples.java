package Controller;

import java.util.List;

import Model.estructuras.EstructuraArbolResiduosMultiples;
import Model.estructuras.PasoBusquedaMultiples;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * GESTOR DEL ÁRBOL DE BÚSQUEDA POR RESIDUOS MÚLTIPLES
 * ============================================================================
 *
 * Fachada de la vista hacia el árbol de búsqueda por residuos MÚLTIPLES que
 * ubica letras (A-Z) por su posición en el alfabeto con binario fijo a 5
 * cifras, pero agrupando las cifras en ENLACES de varios niveles (2-2-1).
 *
 * Es un controlador ligero e independiente: sin configuración previa (el
 * usuario digita una letra) y soluciona el reinicio creando una estructura
 * nueva con su esqueleto fijo (raíz vacía + 4→4→2 nodos de cada nivel).
 */
public class GestorArbolResiduosMultiples {

    /** Estructura de búsqueda por residuos múltiples activa. */
    private EstructuraArbolResiduosMultiples arbol;

    /** Crea el gestor con la estructura nueva (esqueleto fijo y vacío). */
    public GestorArbolResiduosMultiples() {
        this.arbol = new EstructuraArbolResiduosMultiples();
    }

    /**
     * Inserta la letra siguiendo la ruta que forman sus enlaces (sus
     * etiquetas reunidas dan el binario completo de 5 cifras).
     *
     * @param letra letra A-Z digitada por el usuario.
     * @throws ExcepcionEstructura si la letra no es válida o ya existe.
     */
    public void insertarLetra(char letra) throws ExcepcionEstructura {
        arbol.insertar(letra);
    }

    /**
     * Elimina la letra dejando su hoja del nivel 3 vacía (el esqueleto se
     * conserva).
     *
     * @param letra letra A-Z a eliminar.
     * @throws ExcepcionEstructura si la letra no es válida o no existe.
     */
    public void eliminarLetra(char letra) throws ExcepcionEstructura {
        arbol.eliminar(letra);
    }

    /**
     * Busca la letra y devuelve los pasos del recorrido para animarla.
     *
     * @param letra letra A-Z solicitada.
     * @return pasos de la búsqueda.
     * @throws ExcepcionEstructura si la letra es inválida o el árbol está vacío.
     */
    public List<PasoBusquedaMultiples> buscarLetra(char letra)
            throws ExcepcionEstructura {
        return arbol.buscarPasos(letra);
    }

    /** Vacía la estructura dejándola lista para empezar de cero. */
    public void reiniciar() {
        this.arbol = new EstructuraArbolResiduosMultiples();
    }

    /** @return la estructura activa para consultas de solo lectura. */
    public EstructuraArbolResiduosMultiples getEstructura() {
        return arbol;
    }

    /** @return true si la estructura existe y está lista para operar. */
    public boolean isSeleccionHecha() {
        return arbol != null;
    }
}
