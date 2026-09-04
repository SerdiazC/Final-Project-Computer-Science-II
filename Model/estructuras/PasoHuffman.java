package Model.estructuras;

/**
 * ============================================================================
 * PASO DEL PROCESO DE AGRUPACIÓN DEL ÁRBOL DE HUFFMAN
 * ============================================================================
 *
 * Objeto inmutable que describe UN movimiento registrado mientras se construye
 * el árbol de Huffman. Algunos pasos describen el análisis inicial de la
 * palabra (conteo y orden de la tabla), otros describen CADA agrupación de
 * los dos caracteres/nodos de menor frecuencia, y el último remata con la
 * ecuación final y la asignación de los códigos.
 *
 * Guarda el NÚMERO del paso y una DESCRIPCIÓN en español de lo ocurrido.
 */
public class PasoHuffman {

    /** Número ordinal del paso (1 en adelante). */
    private final int numero;

    /** Descripción en español del paso. */
    private final String descripcion;

    /**
     * @param numero      número ordinal del paso.
     * @param descripcion texto explicativo del paso.
     */
    public PasoHuffman(int numero, String descripcion) {
        this.numero = numero;
        this.descripcion = descripcion;
    }

    /** @return número ordinal del paso. */
    public int getNumero() {
        return numero;
    }

    /** @return explicación textual del paso. */
    public String getDescripcion() {
        return descripcion;
    }
}