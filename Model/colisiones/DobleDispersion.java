package Model.colisiones;

/**
 * ============================================================================
 * SOLUCIÓN DE COLISIÓN: DOBLE DISPERSIÓN (SONDEO)
 * ============================================================================
 *
 * Cuando la dirección calculada está ocupada, la clave avanza con un SALTO
 * FIJO calculado con un segundo módulo sobre la propia clave:
 *
 *   salto            = 1 + (clave módulo (capacidad - 1))
 *   índice(intentos) = (direcciónBase + intento * salto) módulo capacidad
 *
 * El "+1" garantiza que el salto nunca sea cero: cada intento cae en una
 * casilla distinta y el recorrido no se estanca en la dirección original.
 *
 * Ejemplo con capacidad 10 y clave cuyo salto da 4, direcciónBase 0:
 *   intento 1 -> 4, intento 2 -> 8, intento 3 -> 2 (con vuelta)...
 *
 * Basado en el prototipo de referencia DoubleHash.
 */
public class DobleDispersion implements SolucionColision {

    /** Nombre único de esta solución. */
    public static final String NOMBRE = "DOBLE DISPERSION";

    /** @return "DOBLE DISPERSION". */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /** Es una técnica por sondeo dentro del arreglo principal. */
    @Override
    public boolean esPorSondeo() {
        return true;
    }

    /**
     * Calcula el salto de esta clave: 1 + (clave módulo (capacidad - 1)).
     * Con capacidad 1 se usa salto 1 para evitar dividir entre cero.
     *
     * @param clave     clave que está sondeando.
     * @param capacidad tamaño total de la estructura.
     * @return salto fijo entre 1 y capacidad - 1.
     */
    public int calcularSalto(int clave, int capacidad) {
        if (capacidad <= 1) {
            return 1;
        }
        return 1 + (clave % (capacidad - 1));
    }

    /**
     * @return índice (direcciónBase + numeroIntento * salto) módulo capacidad.
     */
    @Override
    public int calcularIndice(int direccionBase, int numeroIntento, int clave,
            int capacidad) {
        int salto = calcularSalto(clave, capacidad);
        return (direccionBase + numeroIntento * salto) % capacidad;
    }
}
