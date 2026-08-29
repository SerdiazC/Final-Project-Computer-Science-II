package Model.colisiones;

/**
 * ============================================================================
 * SOLUCIÓN DE COLISIÓN: PRUEBA CUADRÁTICA (SONDEO)
 * ============================================================================
 *
 * Cuando la dirección calculada está ocupada, la clave avanza con saltos
 * que crecen al cuadrado, evitando el agrupamiento de la prueba lineal:
 *
 *   índice(intentos) = (direcciónBase + intento^2) módulo capacidad
 *
 * Ejemplo con capacidad 10 y direcciónBase 0 ocupada:
 *   intento 1 -> 1, intento 2 -> 4, intento 3 -> 9, intento 4 -> 6...
 *
 * Basado en el prototipo de referencia QuadraticTest; a diferencia del
 * prototipo, el avance cuadrático i*i se usa TANTO en inserción como en
 * búsqueda para que ambas recorran exactamente las mismas posiciones.
 */
public class PruebaCuadratica implements SolucionColision {

    /** Nombre único de esta solución. */
    public static final String NOMBRE = "PRUEBA CUADRATICA";

    /** @return "PRUEBA CUADRATICA". */
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
     * @return índice (direcciónBase + numeroIntento^2) módulo capacidad.
     */
    @Override
    public int calcularIndice(int direccionBase, int numeroIntento, int clave,
            int capacidad) {
        int salto = numeroIntento * numeroIntento;
        return (direccionBase + salto) % capacidad;
    }
}
