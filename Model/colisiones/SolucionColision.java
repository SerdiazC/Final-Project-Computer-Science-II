package Model.colisiones;

/**
 * ============================================================================
 * SOLUCIÓN DE COLISIÓN (INTERFAZ DEL PATRÓN STRATEGY PARA DISPERSIÓN)
 * ============================================================================
 *
 * Contrato común de las cinco técnicas para resolver colisiones hash del
 * proyecto. Una COLISIÓN ocurre cuando dos claves distintas calculan la
 * MISMA dirección; la solución define dónde vive la clave que llegó tarde:
 *
 *   SONDEO (la clave queda dentro del arreglo principal, en otra posición):
 *     - PRUEBA LINEAL       -> avanza casilla por casilla.
 *     - PRUEBA CUADRATICA   -> avanza saltos 1, 4, 9, 16...
 *     - DOBLE DISPERSION    -> avance fijo calculado con un segundo módulo.
 *
 *   CUBETAS (la clave queda asociada a SU dirección, en un contenedor):
 *     - ENCADENAMIENTO      -> lista enlazada por dirección.
 *     - ARREGLO ANIDADO     -> arreglo secundario por dirección.
 *
 * REGLA DEL PROYECTO: la solución se ELIGE al crear la estructura y solo
 * puede cambiarse REINICIANDO la estructura de datos (vacía); así todas
 * las colisiones de una misma estructura se resuelven siempre igual.
 *
 * Basado en los prototipos de referencia (LinearTest, QuadraticTest,
 * DoubleHash, LinkedList y NestedArray), unificando centinelas (-1 como
 * espacio libre) y usando el avance cuadrático i*i también en inserción.
 *
 * PRINCIPIOS SOLID:
 *  - ISP: solo lo esencial (nombre, tipo de técnica y cálculo de sondeo).
 *  - OCP: agregar una solución nueva = otra implementación registrada.
 *  - DIP: EstructuraHash depende de esta interfaz, no de clases concretas.
 */
public interface SolucionColision {

    /** Índice interno que indica "no se halló espacio libre al sondear". */
    int SIN_ESPACIO = -1;

    /**
     * @return nombre único de la solución (constantes NOMBRE_* de las
     *         implementaciones; aparece en menús y narraciones).
     */
    String getNombre();

    /**
     * @return true si la solución SONDEA posiciones alternas del arreglo
     *         principal (lineal, cuadrática o doble); false si guarda las
     *         claves en cubetas asociadas a su dirección (encadenamiento
     *         o arreglo anidado).
     */
    boolean esPorSondeo();

    /**
     * Calcula el índice interno (base 0) que corresponde al intento número
     * "numeroIntento" del sondeo. Solo lo usan las soluciones por sondeo;
     * las de cubetas devuelven el índice base sin alterar.
     *
     * @param direccionBase índice interno (base 0) calculado por la función hash.
     * @param numeroIntento intento del sondeo (1, 2, 3, ...; 0 sería el original).
     * @param clave         clave que está sondeando (la usa la doble dispersión).
     * @param capacidad     tamaño total de la estructura.
     * @return índice interno candidato, siempre dentro de [0 .. capacidad-1].
     */
    int calcularIndice(int direccionBase, int numeroIntento, int clave, int capacidad);
}
