package Model.estructuras;

/**
 * ============================================================================
 * NODO DEL ÁRBOL DIGITAL DE LETRAS
 * ============================================================================
 *
 * Unidad del árbol digital que ubica letras (A-Z) según los bits de la
 * POSICIÓN de cada letra en el alfabeto (A=1, B=2, ..., Z=26).
 *
 * Cada nodo guarda:
 *  - la LETRA alojada (o queda VACÍO si fue eliminada),
 *  - el número de POSICIÓN de esa letra y su código BINARIO,
 *  - dos enlaces guiados por bits:
 *      * izquierda -> bit 0 de la posición que desciende.
 *      * derecha   -> bit 1 de la posición que desciende.
 *
 * RESPONSABILIDAD ÚNICA: almacenar una letra y sus datos de ubicación.
 */
public class NodoArbolDigitalLetras {

    /** Letra almacenada ('\0' cuando el nodo quedó vacío tras eliminarse). */
    private char letra;

    /** Posición de la letra en el alfabeto (1..26), -1 si el nodo está vacío. */
    private int posicion;

    /** Binario de la posición (sin ceros a la izquierda), null si vacío. */
    private String binario;

    /** Hijo por el que se baja cuando el bit evaluado es 0. */
    private NodoArbolDigitalLetras izquierda;

    /** Hijo por el que se baja cuando el bit evaluado es 1. */
    private NodoArbolDigitalLetras derecha;

    /**
     * Crea un nodo ocupado con la letra indicada y sin hijos.
     *
     * @param letra letra mayúscula A-Z a ubicar en este nodo.
     */
    public NodoArbolDigitalLetras(char letra) {
        this.letra = letra;
        this.posicion = letra - 'A' + 1;
        this.binario = EstructuraArbolDigitalLetras.binarioDePosicion(posicion);
    }

    /** @return true si el nodo no guarda ninguna letra (fue vaciado). */
    public boolean estaVacio() {
        return letra == '\0';
    }

    /**
     * Vacía el nodo conservando sus hijos: la ruta de otras letras puede
     * pasar por él.
     */
    public void vaciar() {
        this.letra = '\0';
        this.posicion = -1;
        this.binario = null;
    }

    /** @return letra alojada o '\0' si el nodo está vacío. */
    public char getLetra() {
        return letra;
    }

    /** @return posición en el alfabeto (1..26) o -1 si está vacío. */
    public int getPosicion() {
        return posicion;
    }

    /** @return binario de la posición o null si está vacío. */
    public String getBinario() {
        return binario;
    }

    /** @return hijo izquierdo (bit 0) o null si no existe. */
    public NodoArbolDigitalLetras getIzquierda() {
        return izquierda;
    }

    /**
     * Enlaza el hijo izquierdo (bit 0).
     *
     * @param izquierda nodo a enlazar.
     */
    public void setIzquierda(NodoArbolDigitalLetras izquierda) {
        this.izquierda = izquierda;
    }

    /** @return hijo derecho (bit 1) o null si no existe. */
    public NodoArbolDigitalLetras getDerecha() {
        return derecha;
    }

    /**
     * Enlaza el hijo derecho (bit 1).
     *
     * @param derecha nodo a enlazar.
     */
    public void setDerecha(NodoArbolDigitalLetras derecha) {
        this.derecha = derecha;
    }
}