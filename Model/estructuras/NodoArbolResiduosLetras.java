package Model.estructuras;

/**
 * ============================================================================
 * NODO DEL ÁRBOL DE BÚSQUEDA POR RESIDUOS (VARIANTE DE LETRAS)
 * ============================================================================
 *
 * Unidad del árbol que ubica letras (A-Z) según los bits de la POSICIÓN de
 * cada letra en el alfabeto (A=1, B=2, ..., Z=26), con binario SIEMPRE fijo
 * a 5 cifras. A diferencia del árbol digital de letras, la RAÍZ permanece
 * VACÍA siempre (sirve solo de punto de partida del descenso) y, cuando dos
 * letras colisionan en un nodo, ese nodo se convierte en un NODO DE ENLACE:
 * no guarda letra y ninguna letra futura podrá ubicarse en él.
 *
 * Cada nodo puede estar en uno de tres estados:
 *  - VACÍO (letra '\0'): espacio libre donde aún se puede insertar.
 *  - ENLACE (letra '\0' y enlace == true): nodo de unión tras una colisión;
 *    bloqueado: ninguna letra podrá ocuparlo nunca.
 *  - OCUPADO (letra != '\0'): contiene una letra.
 *
 * RESPONSABILIDAD ÚNICA: almacenar letra / estado / datos de ubicación.
 */
public class NodoArbolResiduosLetras {

    /** Letra almacenada ('\0' si el nodo está vacío o es un enlace). */
    private char letra;

    /** Posición de la letra en el alfabeto (1..26), -1 si no hay letra. */
    private int posicion;

    /** Binario fijado a 5 cifras de la posición, null si no hay letra. */
    private String binario;

    /** true si este nodo es un NODO DE ENLACE (bloqueado tras una colisión). */
    private boolean enlace;

    /** Hijo por el que se baja cuando el bit evaluado es 0. */
    private NodoArbolResiduosLetras izquierda;

    /** Hijo por el que se baja cuando el bit evaluado es 1. */
    private NodoArbolResiduosLetras derecha;

    /**
     * Crea un nodo VACÍO (no es enlace) y sin letra ni hijos. Se usa para la
     * raíz y para los espacios libres creados durante el descenso.
     */
    public NodoArbolResiduosLetras() {
        this.letra = '\0';
        this.posicion = -1;
        this.binario = null;
        this.enlace = false;
    }

    /**
     * Crea un nodo OCUPADO con la letra indicada y sin hijos.
     *
     * @param letra letra mayúscula A-Z a ubicar en este nodo.
     */
    public NodoArbolResiduosLetras(char letra) {
        this.letra = letra;
        this.posicion = letra - 'A' + 1;
        this.binario = EstructuraArbolDigitalLetras.binarioDePosicion(posicion);
        this.enlace = false;
    }

    /** @return true si el nodo no guarda letra (vacío o enlace). */
    public boolean estaVacio() {
        return letra == '\0';
    }

    /** @return true si el nodo guarda una letra (ocupado). */
    public boolean estaOcupado() {
        return letra != '\0';
    }

    /** @return true si el nodo es un NODO DE ENLACE (bloqueado). */
    public boolean esEnlace() {
        return enlace;
    }

    /**
     * Convierte un nodo ocupado (o vacío) en NODO DE ENLACE: pierde la letra
     * y queda marcado como bloqueado; conserva sus hijos.
     */
    public void convertirEnEnlace() {
        this.letra = '\0';
        this.posicion = -1;
        this.binario = null;
        this.enlace = true;
    }

    /**
     * Coloca una letra en este nodo, que debe estar VACÍO y NO ser enlace.
     * Si se pasa '\0', deja el nodo como VACÍO normal (no enlace).
     *
     * @param letra letra mayúscula A-Z a ubicar aquí ('\0' para vaciarlo).
     */
    public void guardarLetra(char letra) {
        this.letra = letra;
        this.enlace = false;
        if (letra == '\0') {
            this.posicion = -1;
            this.binario = null;
        } else {
            this.posicion = letra - 'A' + 1;
            this.binario =
                    EstructuraArbolDigitalLetras.binarioDePosicion(posicion);
        }
    }

    /**
     * Devuelve el bit (índice) del binario fijo de la letra de este nodo.
     * Solo debe llamarse sobre un nodo OCUPADO.
     *
     * @param indice índice del bit (0 = más significativo).
     * @return carácter '0' o '1'; '0' si el índice queda fuera del patrón.
     */
    public char bitEn(int indice) {
        if (binario == null || indice < 0 || indice >= binario.length()) {
            return '0';
        }
        return binario.charAt(indice);
    }

    /** @return letra alojada o '\0' si no hay letra. */
    public char getLetra() {
        return letra;
    }

    /** @return posición en el alfabeto (1..26) o -1 si no hay letra. */
    public int getPosicion() {
        return posicion;
    }

    /** @return binario fijo a 5 cifras de la posición o null si no hay letra. */
    public String getBinario() {
        return binario;
    }

    /** @return hijo izquierdo (bit 0) o null si no existe. */
    public NodoArbolResiduosLetras getIzquierda() {
        return izquierda;
    }

    /**
     * Enlaza el hijo izquierdo (bit 0).
     *
     * @param izquierda nodo a enlazar.
     */
    public void setIzquierda(NodoArbolResiduosLetras izquierda) {
        this.izquierda = izquierda;
    }

    /** @return hijo derecho (bit 1) o null si no existe. */
    public NodoArbolResiduosLetras getDerecha() {
        return derecha;
    }

    /**
     * Enlaza el hijo derecho (bit 1).
     *
     * @param derecha nodo a enlazar.
     */
    public void setDerecha(NodoArbolResiduosLetras derecha) {
        this.derecha = derecha;
    }
}
