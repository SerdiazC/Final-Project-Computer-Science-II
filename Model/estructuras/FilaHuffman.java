package Model.estructuras;

/**
 * ============================================================================
 * FILA DE LA TABLA DE FRECUENCIAS DEL ÁRBOL DE HUFFMAN
 * ============================================================================
 *
 * Objeto inmutable que representa UNA entrada de la tabla de frecuencias que
 * la vista muestra al procesar una palabra:
 *
 *  - CARÁCTER (letra A-Z o el guion bajo '_'),
 *  - CANTIDAD de veces que se repitió en la palabra,
 *  - FRECUENCIA expresada como cociente (cantidad / total de caracteres),
 *  - CÓDIGO de Huffman asignado recorriendo el árbol (izquierda = 0,
 *    derecha = 1).
 *
 * La tabla exponen las filas ORDENADAS de mayor a menor frecuencia; cuando
 * dos caracteres empatan en frecuencia, el que el usuario escribió PRIMERO
 * en la palabra queda MÁS ABAJO en la tabla.
 */
public class FilaHuffman {

    /** Carácter al que pertenece la fila. */
    private final char caracter;

    /** Veces que se repitió el carácter en la palabra. */
    private final int cantidad;

    /** Frecuencia como cociente cantidad/total (0..1). */
    private final double frecuencia;

    /** Código de Huffman del carácter (bits 0 y 1) o cadena vacía. */
    private final String codigo;

    /**
     * @param caracter   carácter de la fila.
     * @param cantidad   veces que se repitió en la palabra.
     * @param frecuencia cociente cantidad/total.
     * @param codigo     código de Huffman del carácter.
     */
    public FilaHuffman(char caracter, int cantidad, double frecuencia,
            String codigo) {
        this.caracter = caracter;
        this.cantidad = cantidad;
        this.frecuencia = frecuencia;
        this.codigo = codigo;
    }

    /** @return carácter al que pertenece la fila. */
    public char getCaracter() {
        return caracter;
    }

    /** @return veces que se repitió el carácter en la palabra. */
    public int getCantidad() {
        return cantidad;
    }

    /** @return frecuencia como cociente cantidad/total (0..1). */
    public double getFrecuencia() {
        return frecuencia;
    }

    /** @return código de Huffman del carácter (o cadena vacía). */
    public String getCodigo() {
        return codigo;
    }
}