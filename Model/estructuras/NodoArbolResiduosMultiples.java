package Model.estructuras;

/**
 * ============================================================================
 * NODO DEL ÁRBOL DE BÚSQUEDA POR RESIDUOS MÚLTIPLES
 * ============================================================================
 *
 * Nodo de un árbol donde CADA NIVEL agrupa CIFRAS binarias (de ahí
 * "múltiples"). La estructura es siempre:
 *
 *   Nivel 0  RAÍZ ................. vacía (no guarda letra ni cifras)
 *   Nivel 1  4 nodos 00, 01, 10, 11 ... (primeras 2 cifras binarias)
 *   Nivel 2  4 nodos 00, 01, 10, 11 ... (siguientes 2 cifras) por cada nivel 1
 *   Nivel 3  2 nodos 0, 1 .......... (última cifra) por cada nivel 2
 *   Nivel 4  la LETRA insertada ....... cuelga del nodo del nivel 3
 *
 * Así, al recorrer desde la raíz, las etiquetas de los enlaces forman el
 * binario FIJO de 5 cifras de la letra (posición en el alfabeto A=1..Z=26).
 *
 * Cada nodo guarda su ETIQUETA y su NIVEL (0=raíz, 1, 2, 3). Los hijos son
 * fijos (siempre existen): 4 hijos en niveles 0 y 1, 2 hijos en nivel 2 y
 * ninguno en nivel 3 (es una hoja que puede alojar UNA letra).
 *
 * RESPONSABILIDAD ÚNICA: almacenar etiqueta/nivel/hijos fijos y la letra
 * (opcional) de la hoja.
 */
public class NodoArbolResiduosMultiples {

    /** Etiqueta del enlace de este nodo ("", "00", "01", "10", "11", "0", "1"). */
    private final String etiqueta;

    /** Nivel del nodo: 0=raíz, 1, 2, 3 (nivel 3 = hoja con posible letra). */
    private final int nivel;

    /** Hijos fijos (4 en nivel 0-1, 2 en nivel 2, null en nivel 3). Ninguno null si aplica. */
    private final NodoArbolResiduosMultiples[] hijos;

    /** Letra alojada en la hoja ('\0' si el nodo no guarda letra). */
    private char letra;

    /**
     * Crea un nodo con su etiqueta, su nivel y su número fijo de hijos.
     *
     * @param etiqueta     etiqueta del enlace hacia este nodo.
     * @param nivel        nivel del nodo (0=raíz, 1, 2, 3).
     * @param cantidadHijos hijos fijos (4, 2 o 0 para hojas).
     */
    public NodoArbolResiduosMultiples(String etiqueta, int nivel,
            int cantidadHijos) {
        this.etiqueta = etiqueta;
        this.nivel = nivel;
        this.hijos = (cantidadHijos > 0)
                ? new NodoArbolResiduosMultiples[cantidadHijos] : null;
        this.letra = '\0';
    }

    /** @return etiqueta del enlace de este nodo. */
    public String getEtiqueta() {
        return etiqueta;
    }

    /** @return nivel del nodo (0=raíz, 1, 2, 3). */
    public int getNivel() {
        return nivel;
    }

    /** @return true si el nodo es la RAÍZ (nivel 0, siempre vacía). */
    public boolean esRaiz() {
        return nivel == 0;
    }

    /** @return true si el nodo es una HOJA (nivel 3). */
    public boolean esHoja() {
        return nivel == 3;
    }

    /** @return true si esta hoja guarda una letra. */
    public boolean tieneLetra() {
        return letra != '\0';
    }

    /** @return letra alojada ('\0' si el nodo no guarda letra). */
    public char getLetra() {
        return letra;
    }

    /** Coloca la letra en esta hoja. */
    public void guardarLetra(char letra) {
        this.letra = letra;
    }

    /**
     * Devuelve el hijo fijo por su índice (0..3 en niveles 0-1, 0..1 en
     * nivel 2), o null para hojas.
     *
     * @param indice posición del hijo.
     * @return hijo en esa posición, o null si no aplica.
     */
    public NodoArbolResiduosMultiples getHijo(int indice) {
        if (hijos == null || indice < 0 || indice >= hijos.length) {
            return null;
        }
        return hijos[indice];
    }

    /** @return arreglo de hijos fijos (puede ser null en hojas). */
    public NodoArbolResiduosMultiples[] getHijos() {
        return hijos;
    }

    /** @return cantidad de hijos fijos (0 en hojas). */
    public int cantidadHijos() {
        return (hijos == null) ? 0 : hijos.length;
    }

    /** Enlaza un hijo fijo en la posición indicada. */
    public void setHijo(int indice, NodoArbolResiduosMultiples hijo) {
        if (hijos != null && indice >= 0 && indice < hijos.length) {
            hijos[indice] = hijo;
        }
    }

    /** @return la posición en el alfabeto (A=1..Z=26) o 0 si no hay letra. */
    public int posicionDeLetra() {
        return tieneLetra() ? (letra - 'A' + 1) : 0;
    }

    /** @return el binario fijo a 5 cifras de la letra, o null si no hay. */
    public String binarioDeLetra() {
        return tieneLetra()
                ? EstructuraArbolDigitalLetras.binarioDePosicion(posicionDeLetra())
                : null;
    }
}
