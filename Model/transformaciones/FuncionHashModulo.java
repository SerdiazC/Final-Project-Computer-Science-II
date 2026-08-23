package Model.transformaciones;

/**
 * ============================================================================
 * FUNCIÓN HASH MOD
 * ============================================================================
 *
 * Especificación del proyecto:
 *
 *   1. Tomar la clave.
 *   2. Hallar el MÓDULO entre la clave y el total (tamaño) de la estructura.
 *      Ejemplo: clave 3456 con estructura de 200 -> 3456 % 200 = 56.
 *   3. SUMAR 1 SIEMPRE AL FINAL. Resultado: 56 + 1 = 57.
 *
 * La dirección final queda garantizada en [1 .. tamanoEstructura].
 */
public class FuncionHashModulo implements FuncionHash {

    /** Nombre oficial de la función dentro del sistema. */
    public static final String NOMBRE = "HASH MOD";

    /**
     * Aplica el cálculo módulo y el cierre estándar (+1 al final).
     *
     * @param clave            valor numérico a transformar.
     * @param tamanoEstructura tamaño total de la estructura destino.
     * @return dirección resultante en [1 .. tamanoEstructura].
     */
    @Override
    public int calcularDireccion(int clave, int tamanoEstructura) {
        return cerrarCalculo(clave, tamanoEstructura);
    }

    /**
     * @return narración completa del cálculo: módulo y suma final.
     */
    @Override
    public String describirCalculo(int clave, int tamanoEstructura) {
        int modulo = clave % tamanoEstructura;
        return "HASH MOD: " + clave + " % " + tamanoEstructura + " = "
                + modulo + "; se suma 1 al final -> direccion " + (modulo + 1);
    }

    /** @return nombre único de la función ("HASH MOD"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}
