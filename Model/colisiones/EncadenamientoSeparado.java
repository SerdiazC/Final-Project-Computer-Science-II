package Model.colisiones;

/**
 * ============================================================================
 * SOLUCIÓN DE COLISIÓN: ENCADENAMIENTO SEPARADO (CUBETAS)
 * ============================================================================
 *
 * Cuando la dirección calculada está ocupada, la clave NO busca otra
 * dirección: se AGREGA a la lista enlazada de SU dirección. Así cada
 * dirección puede contener varias claves encadenadas en orden de llegada:
 *
 *   direccion 35 -> 1234 -> 1334 -> null
 *
 * Ventajas: nunca se llena el arreglo principal y la búsqueda de las
 * colisionadas es recorrer la cadena de su propia dirección.
 *
 * Basado en el prototipo de referencia LinkedList.
 */
public class EncadenamientoSeparado implements SolucionColision {

    /** Nombre único de esta solución. */
    public static final String NOMBRE = "ENCADENAMIENTO";

    /** @return "ENCADENAMIENTO". */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /** NO sondea: guarda los desbordes en cubetas por dirección. */
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
