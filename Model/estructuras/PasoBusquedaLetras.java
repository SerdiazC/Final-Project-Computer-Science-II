package Model.estructuras;

/**
 * ============================================================================
 * PASO DE BÚSQUEDA EN EL ÁRBOL DIGITAL DE LETRAS
 * ============================================================================
 *
 * Objeto inmutable que describe UNA comparación (o movimiento) registrado
 * durante una búsqueda dentro del árbol digital de letras.
 *
 * Guarda:
 *  - el NÚMERO del paso,
 *  - la LETRA alojada en el nodo visitado (o '\0' si es un nodo vacío),
 *  - el BIT que se estaba evaluando al llegar a ese nodo ('\0' si no aplica,
 *    por ejemplo en el remate de "letra no encontrada"),
 *  - una DESCRIPCIÓN en español de lo ocurrido.
 */
public class PasoBusquedaLetras {

    /** Número ordinal del paso dentro de la búsqueda (1 en adelante). */
    private final int numero;

    /** Letra alojada en el nodo visitado ('\0' si es un nodo vacío). */
    private final char letra;

    /** Bit que guiaba el descenso hasta este nodo ('\0' si no aplica). */
    private final char bit;

    /** Descripción en español de lo ocurrido en este paso. */
    private final String descripcion;

    /**
     * @param numero      número ordinal del paso.
     * @param letra       letra del nodo visitado, o '\0' si está vacío.
     * @param bit         bit que guiaba el descenso ('\0' si no aplica).
     * @param descripcion texto explicativo del paso.
     */
    public PasoBusquedaLetras(int numero, char letra, char bit,
            String descripcion) {
        this.numero = numero;
        this.letra = letra;
        this.bit = bit;
        this.descripcion = descripcion;
    }

    /** @return número ordinal del paso. */
    public int getNumero() {
        return numero;
    }

    /** @return letra del nodo visitado, o '\0' si el nodo está vacío. */
    public char getLetra() {
        return letra;
    }

    /** @return bit que guiaba el descenso ('\0' si no aplica). */
    public char getBit() {
        return bit;
    }

    /** @return explicación textual del paso. */
    public String getDescripcion() {
        return descripcion;
    }
}