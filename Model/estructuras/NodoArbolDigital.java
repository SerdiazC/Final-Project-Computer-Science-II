package Model.estructuras;

import Model.EstructuraDeDatos;

/**
 * ============================================================================
 * NODO DEL ÁRBOL DIGITAL
 * ============================================================================
 *
 * Unidad del árbol de residuos digitales. Cada nodo puede:
 *
 *  - Estar VACÍO (clave = CLAVE_VACIA): ocurre cuando su clave fue
 *    eliminada pero conserva hijos que deben seguir accesibles.
 *  - Guardar UNA clave y enlazar hasta dos hijos:
 *      * izquierda -> siguiente bit '0' de la clave que desciende.
 *      * derecha   -> siguiente bit '1' de la clave que desciende.
 *
 * RESPONSABILIDAD ÚNICA: almacenar una clave y sus dos enlaces.
 */
public class NodoArbolDigital {

    /** Clave almacenada o CLAVE_VACIA si el nodo quedó sin dato. */
    private int clave;

    /** Hijo por el que se baja cuando el bit evaluado es 0. */
    private NodoArbolDigital izquierda;

    /** Hijo por el que se baja cuando el bit evaluado es 1. */
    private NodoArbolDigital derecha;

    /**
     * Crea un nodo con la clave indicada y sin hijos.
     *
     * @param clave valor a almacenar.
     */
    public NodoArbolDigital(int clave) {
        this.clave = clave;
    }

    /** @return true si el nodo no guarda ninguna clave (fue vaciado). */
    public boolean estaVacio() {
        return clave == EstructuraDeDatos.CLAVE_VACIA;
    }

    /** @return clave almacenada o CLAVE_VACIA si está vacío. */
    public int getClave() {
        return clave;
    }

    /**
     * Asigna o libera la clave del nodo.
     *
     * @param clave nueva clave, o CLAVE_VACIA para vaciarlo.
     */
    public void setClave(int clave) {
        this.clave = clave;
    }

    /** @return hijo izquierdo (bit 0) o null si no existe. */
    public NodoArbolDigital getIzquierda() {
        return izquierda;
    }

    /**
     * Enlaza el hijo izquierdo (bit 0).
     *
     * @param izquierda nodo a enlazar.
     */
    public void setIzquierda(NodoArbolDigital izquierda) {
        this.izquierda = izquierda;
    }

    /** @return hijo derecho (bit 1) o null si no existe. */
    public NodoArbolDigital getDerecha() {
        return derecha;
    }

    /**
     * Enlaza el hijo derecho (bit 1).
     *
     * @param derecha nodo a enlazar.
     */
    public void setDerecha(NodoArbolDigital derecha) {
        this.derecha = derecha;
    }
}
