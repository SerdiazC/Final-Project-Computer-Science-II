package Model.busquedas;

/**
 * ============================================================================
 * PASO DE BÚSQUEDA
 * ============================================================================
 *
 * Objeto inmutable que describe UNA comparación dentro de una búsqueda.
 *
 * Cada estrategia registra aquí lo que hizo en cada paso para que la futura
 * interfaz web pueda REPRODUCIR la búsqueda animada paso a paso sin volver a
 * ejecutarla ni conocer el algoritmo (la vista solo dibuja estos datos).
 *
 * Campos no aplicables a una búsqueda concreta se guardan en -1
 * (por ejemplo, la lineal no reduce límites).
 */
public class PasoBusqueda {

    /** Número del paso dentro de la búsqueda (1, 2, 3...). */
    private final int numeroPaso;

    /** Índice del dato con el que se comparó en este paso. */
    private final int indiceExplorado;

    /** Límite izquierdo del rango vigente (-1 si la búsqueda no usa límites). */
    private final int limiteInferior;

    /** Límite derecho del rango vigente (-1 si la búsqueda no usa límites). */
    private final int limiteSuperior;

    /** Valor numérico que se comparó contra la clave buscada. */
    private final int claveComparada;

    /** Descripción en español de lo que ocurrió en este paso. */
    private final String descripcion;

    /**
     * @param numeroPaso      número ordinal del paso (1 en adelante).
     * @param indiceExplorado índice del dato comparado.
     * @param limiteInferior  límite izquierdo del rango vigente o -1.
     * @param limiteSuperior  límite derecho del rango vigente o -1.
     * @param claveComparada  valor con el que se comparó.
     * @param descripcion     texto explicativo del paso.
     */
    public PasoBusqueda(int numeroPaso, int indiceExplorado,
                        int limiteInferior, int limiteSuperior,
                        int claveComparada, String descripcion) {
        this.numeroPaso = numeroPaso;
        this.indiceExplorado = indiceExplorado;
        this.limiteInferior = limiteInferior;
        this.limiteSuperior = limiteSuperior;
        this.claveComparada = claveComparada;
        this.descripcion = descripcion;
    }

    /** @return número ordinal del paso. */
    public int getNumeroPaso() {
        return numeroPaso;
    }

    /** @return índice del dato comparado en este paso. */
    public int getIndiceExplorado() {
        return indiceExplorado;
    }

    /** @return límite izquierdo vigente, o -1 si no aplica. */
    public int getLimiteInferior() {
        return limiteInferior;
    }

    /** @return límite derecho vigente, o -1 si no aplica. */
    public int getLimiteSuperior() {
        return limiteSuperior;
    }

    /** @return valor con el que se comparó la clave buscada. */
    public int getClaveComparada() {
        return claveComparada;
    }

    /** @return explicación textual del paso. */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Representación lista para mostrar en consola: el número de paso, la
     * descripción completa del movimiento y, si la búsqueda usa rangos
     * (lineal/binaria), el rango vigente. La web podrá usar los getters
     * individuales para dibujar la estructura.
     */
    @Override
    public String toString() {
        String rango = (limiteInferior == -1)
                ? ""
                : " [rango " + limiteInferior + ".." + limiteSuperior + "]";
        return "Paso " + numeroPaso + ": " + descripcion + rango;
    }
}
