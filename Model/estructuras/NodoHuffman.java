package Model.estructuras;

/**
 * ============================================================================
 * NODO DEL ÁRBOL DE HUFFMAN
 * ============================================================================
 *
 * Nodo binario que representa UNA parte del árbol de Huffman construido a
 * partir de las frecuencias de una palabra:
 *
 *  - SI ES HOJA: guarda un CARÁCTER (letra A-Z o el guion bajo '_') y la
 *    FRECUENCIA (cantidad/total) con que apareció en la palabra.
 *  - SI ES INTERNO: no guarda carácter ('\0') y su FRECUENCIA es la suma de
 *    la de sus dos hijos (el resultado de agruparlos).
 *
 * Además guarda la EXPRESIÓN de su subecuación (ej. "(C + H)") que describe
 * la agrupación con que se formó este nodo, y una SECUENCIA de creación que
 * se usa para desempatar en el algoritmo cuando dos nodos tienen la misma
 * frecuencia (el creado antes va primero).
 */
public class NodoHuffman {

    /** Carácter que guarda la hoja ('\0' si este nodo es interno). */
    private final char caracter;

    /** Frecuencia del nodo (acumulada en los nodos internos). */
    private final double frecuencia;

    /** Hijo izquierdo (enlace marcado con el bit 0). */
    private final NodoHuffman izquierda;

    /** Hijo derecho (enlace marcado con el bit 1). */
    private final NodoHuffman derecha;

    /** Expresión textual de la subecuación que formó este nodo. */
    private final String expresion;

    /** Orden de creación (0 para la primera hoja): desempata en frecuencias
     *  iguales (el de menor secuencia se procesa primero). */
    private final int secuencia;

    /**
     * Crea un nodo de Huffman.
     *
     * @param caracter   carácter si es hoja ('\0' si es interno).
     * @param frecuencia frecuencia del nodo.
     * @param izquierda  hijo izquierdo (bit 0), o null si es hoja.
     * @param derecha    hijo derecho (bit 1), o null si es hoja.
     * @param expresion  subecuación textual del nodo (ej. "(A + B)").
     * @param secuencia  orden de creación para desempates.
     */
    public NodoHuffman(char caracter, double frecuencia,
            NodoHuffman izquierda, NodoHuffman derecha,
            String expresion, int secuencia) {
        this.caracter = caracter;
        this.frecuencia = frecuencia;
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.expresion = expresion;
        this.secuencia = secuencia;
    }

    /** @return true si es una HOJA (guarda un carácter). */
    public boolean esHoja() {
        return caracter != '\0';
    }

    /** @return el carácter de la hoja ('\0' si es interno). */
    public char getCaracter() {
        return caracter;
    }

    /** @return frecuencia del nodo (acumulada en los internos). */
    public double getFrecuencia() {
        return frecuencia;
    }

    /** @return hijo izquierdo (bit 0), o null si es hoja. */
    public NodoHuffman getIzquierda() {
        return izquierda;
    }

    /** @return hijo derecho (bit 1), o null si es hoja. */
    public NodoHuffman getDerecha() {
        return derecha;
    }

    /** @return subecuación textual que formó este nodo. */
    public String getExpresion() {
        return expresion;
    }

    /** @return orden de creación usado para desempatar frecuencias iguales. */
    public int getSecuencia() {
        return secuencia;
    }
}