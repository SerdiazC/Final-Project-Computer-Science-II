package Model.estructuras;

/**
 * ============================================================================
 * PASO DE BÚSQUEDA EN EL ÁRBOL DE BÚSQUEDA POR RESIDUOS MÚLTIPLES
 * ============================================================================
 *
 * Objeto inmutable que describe UN movimiento (o remate) registrado al
 * recorrer el árbol de residuos múltiples. Como este árbol tiene hasta 4
 * hijos por nivel y etiquetas de enlace, el paso guarda la RUTA (etiquetas
 * acumuladas desde la raíz) para que la vista pueda localizar y resaltar el
 * nodo visitado.
 *
 * Guarda:
 *  - el NÚMERO del paso,
 *  - la RUTA (p. ej. "00", "0101", "00100") del nodo visitado,
 *  - la ETIQUETA del enlace que llevó a ese nodo ("00", "01", "10", "11" o
 *    "0", "1") o vacía para la raíz,
 *  - la LETRA alojada en el nodo ('\0' si no tiene),
 *  - una DESCRIPCIÓN en español de lo ocurrido.
 */
public class PasoBusquedaMultiples {

    /** Número ordinal del paso (1 en adelante). */
    private final int numero;

    /** Ruta acumulada desde la raíz (etiquetas concatenadas) del nodo. */
    private final String ruta;

    /** Etiqueta del enlace hacia este nodo (vacía si es la raíz). */
    private final String etiqueta;

    /** Letra alojada en el nodo ('\0' si no tiene). */
    private final char letra;

    /** Descripción en español del paso. */
    private final String descripcion;

    /**
     * @param numero      número ordinal del paso.
     * @param ruta        ruta acumulada del nodo visitado (ej. "00100").
     * @param etiqueta    etiqueta del enlace hacia el nodo.
     * @param letra       letra alojada en el nodo ('\0' si no tiene).
     * @param descripcion texto explicativo del paso.
     */
    public PasoBusquedaMultiples(int numero, String ruta, String etiqueta,
            char letra, String descripcion) {
        this.numero = numero;
        this.ruta = ruta;
        this.etiqueta = etiqueta;
        this.letra = letra;
        this.descripcion = descripcion;
    }

    /** @return número ordinal del paso. */
    public int getNumero() {
        return numero;
    }

    /** @return ruta acumulada del nodo visitado (ej. "00100"). */
    public String getRuta() {
        return ruta;
    }

    /** @return etiqueta del enlace hacia el nodo. */
    public String getEtiqueta() {
        return etiqueta;
    }

    /** @return letra alojada en el nodo ('\0' si no tiene). */
    public char getLetra() {
        return letra;
    }

    /** @return explicación textual del paso. */
    public String getDescripcion() {
        return descripcion;
    }
}
