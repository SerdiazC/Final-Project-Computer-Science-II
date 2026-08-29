package Model.transformaciones;

/**
 * ============================================================================
 * FUNCIÓN HASH TRUNCAMIENTO
 * ============================================================================
 *
 * Especificación del proyecto:
 *
 *   1. Observar el tamaño de la estructura: la cantidad de CIFRAS de ese
 *      tamaño indica cuántas POSICIONES de la clave se seleccionan
 *      (tamaño 100 -> 2 posiciones; tamaño 123 -> 3 posiciones; etc.).
 *   2. Se toman las PRIMERAS N cifras de la clave (posición 1, 2, ... N)
 *      y con ellas se FORMA UN NÚMERO.
 *      Ejemplo: clave 3678 con tabla de tamaño 100 -> posiciones 1 y 2,
 *      dígitos "3" y "6" -> número formado: 36.
 *   3. Se ajusta con módulo del tamaño y SE SUMA 1 AL FINAL, no antes:
 *      36 % 100 = 36; 36 + 1 = 37.
 *
 * CASO ESPECIAL: si la clave tiene menos cifras que las posiciones
 * requeridas (por ejemplo claves de 2 dígitos con tabla de tamaño 500),
 * se usa la clave completa como número formado.
 */
public class FuncionHashTruncamiento implements FuncionHash {

    /** Nombre oficial de la función dentro del sistema. */
    public static final String NOMBRE = "HASH TRUNCAMIENTO";

    /**
     * Aplica el algoritmo completo de la técnica de truncamiento.
     *
     * @param clave            valor entero positivo a transformar.
     * @param tamanoEstructura tamaño total de la estructura destino.
     * @return dirección resultante en [1 .. tamanoEstructura].
     */
    @Override
    public int calcularDireccion(int clave, int tamanoEstructura) {
        return cerrarCalculo(formarNumero(clave, tamanoEstructura), tamanoEstructura);
    }

    /**
     * Forma el número truncando las primeras N cifras de la clave, donde
     * N es la cantidad de cifras del tamaño de la estructura.
     *
     * @param clave            valor a transformar.
     * @param tamanoEstructura tamaño total de la estructura.
     * @return número formado con las cifras seleccionadas.
     */
    private int formarNumero(int clave, int tamanoEstructura) {
        String cifrasClave = Integer.toString(clave);
        int posiciones = contarCifrasBase(tamanoEstructura);

        if (cifrasClave.length() <= posiciones) {
            // No alcanzan posiciones: se usa la clave completa.
            return clave;
        }
        // Primeras N posiciones -> se forma el número correspondiente.
        return Integer.parseInt(cifrasClave.substring(0, posiciones));
    }

    /**
     * @return narración completa: posiciones tomadas, número formado,
     *         módulo y suma final.
     */
    @Override
    public String describirCalculo(int clave, int tamanoEstructura) {
        int posiciones = contarCifrasBase(tamanoEstructura);
        int numeroFormado = formarNumero(clave, tamanoEstructura);

        String detalle;
        if (Integer.toString(clave).length() <= posiciones) {
            detalle = "la clave completa (" + numeroFormado + ") porque tiene "
                    + "menos cifras que las " + posiciones + " posiciones pedidas";
        } else {
            detalle = "las primeras " + posiciones + " cifras de " + clave
                    + ", que son \"" + Integer.toString(clave).substring(0, posiciones)
                    + "\", y se forma el numero " + numeroFormado;
        }

        return "HASH TRUNCAMIENTO: el tamaño " + tamanoEstructura + " tiene "
                + posiciones + " cifra(s); se toman " + detalle + "; " + numeroFormado
                + " % " + tamanoEstructura + " = " + (numeroFormado % tamanoEstructura)
                + "; se suma 1 al final -> direccion " + calcularDireccion(clave, tamanoEstructura);
    }

    /** @return nombre único de la función ("HASH TRUNCAMIENTO"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}
