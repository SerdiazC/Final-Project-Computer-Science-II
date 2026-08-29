package Model.colisiones;

/**
 * ============================================================================
 * SOLUCIÓN DE COLISIÓN: ARREGLO ANIDADO (CUBETAS)
 * ============================================================================
 *
 * Igual que el encadenamiento, la clave colisionada NO cambia de dirección:
 * se guarda en un arreglo secundario asociado a SU dirección, ocupando la
 * primera celda libre de esa "fila".
 *
 *   direccion 35 -> principal: 1234 | fila anidada: [1334, 1444]
 *
 * LÍMITE EN ESTE PROYECTO: el techo GLOBAL de la estructura ("tamaño"
 * claves en total) es matemáticamente más restrictivo que cualquier límite
 * por fila, así que al agotarse el espacio se informa "estructura llena";
 * la fila de cada dirección nunca alcanza a desbordarse antes.
 *
 * Basado en el prototipo de referencia NestedArray.
 */
public class ArregloAnidado implements SolucionColision {

    /** Nombre único de esta solución. */
    public static final String NOMBRE = "ARREGLO ANIDADO";

    /** @return "ARREGLO ANIDADO". */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /** NO sondea: guarda los desbordes en filas por dirección. */
    @Override
    public boolean esPorSondeo() {
        return false;
    }

    /**
     * Las soluciones por cubeta no sondean; devuelven el índice base intacto.
     */
    @Override
    public int calcularIndice(int direccionBase, int numeroIntento, int clave,
            int capacidad) {
        return direccionBase;
    }
}
