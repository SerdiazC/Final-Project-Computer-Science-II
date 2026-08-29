package Model.colisiones;

/**
 * ============================================================================
 * SOLUCIÓN DE COLISIÓN: PRUEBA LINEAL (SONDEO)
 * ============================================================================
 *
 * Cuando la dirección calculada está ocupada, la clave avanza casilla por
 * casilla hacia la derecha (con vuelta al inicio al llegar al final) hasta
 * hallar un espacio libre:
 *
 *   índice(intentos) = (direcciónBase + intento) módulo capacidad
 *
 * Ejemplo con capacidad 10 y direcciónBase 0 ocupada:
 *   intento 1 -> 1, intento 2 -> 2, ... intento 9 -> 9.
 *
 * Basado en el prototipo de referencia LinearTest.
 */
public class PruebaLineal implements SolucionColision {

    /** Nombre único de esta solución. */
    public static final String NOMBRE = "PRUEBA LINEAL";

    /** @return "PRUEBA LINEAL". */
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
     * @return índice (direcciónBase + numeroIntento) módulo capacidad.
     */
    @Override
    public int calcularIndice(int direccionBase, int numeroIntento, int clave,
            int capacidad) {
        return (direccionBase + numeroIntento) % capacidad;
    }
}
