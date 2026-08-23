package Model.transformaciones;

/**
 * ============================================================================
 * FUNCIÓN HASH CUADRADO (DÍGITOS CENTRALES)
 * ============================================================================
 *
 * Especificación del proyecto:
 *
 *   1. Recibir una clave entera positiva y el tamaño de la estructura.
 *   2. Elevar la clave AL CUADRADO.
 *   3. Extraer dinámicamente la cantidad EXACTA de dígitos centrales según
 *      la longitud (cifras) que tenga el tamaño de la tabla:
 *        - tamaño 100  -> 3 cifras... no: 100 tiene 3 cifras; tamaño 99 -> 2.
 *        - Si el sobrante no permite un centro exacto, los dígitos centrales
 *          se toman HACIA LA IZQUIERDA (se recorta más por la derecha).
 *      Ejemplo: 3456^2 = 11943936 (8 cifras); tabla de 200 (3 cifras):
 *      sobran 5 cifras -> se recortan 2 por la izquierda y 3 por la
 *      derecha -> se extraen "943".
 *   4. Se ajusta con módulo del tamaño y SE SUMA 1 AL FINAL, nunca antes:
 *      943 % 200 = 143; 143 + 1 = 144.
 */
public class FuncionHashCuadrado implements FuncionHash {

    /** Nombre oficial de la función dentro del sistema. */
    public static final String NOMBRE = "HASH CUADRADO";

    /**
     * Aplica el algoritmo completo de la técnica del cuadrado.
     *
     * @param clave            valor entero positivo a transformar.
     * @param tamanoEstructura tamaño total de la estructura destino.
     * @return dirección resultante en [1 .. tamanoEstructura].
     */
    @Override
    public int calcularDireccion(int clave, int tamanoEstructura) {
        long cuadrado = (long) clave * clave;
        String cifrasCuadrado = Long.toString(cuadrado);

        int digitosObjetivo = contarDigitos(tamanoEstructura);
        int extraido;

        if (cifrasCuadrado.length() <= digitosObjetivo) {
            // El cuadrado es tan pequeño que ya cabe completo.
            extraido = (int) cuadrado;
        } else {
            extraido = Integer.parseInt(extraerCentrales(cifrasCuadrado, digitosObjetivo));
        }
        // Cierre estándar del proyecto: módulo del tamaño y +1 al final.
        return cerrarCalculo(extraido, tamanoEstructura);
    }

    /**
     * Extrae la porción central indicada. Cuando la cantidad de cifras a
     * descartar es IMPAR, se recorta menos por la izquierda y más por la
     * derecha: así el bloque extraído queda tomado "hacia la izquierda",
     * tal como exige la especificación.
     *
     * @param cifras    representación textual del cuadrado.
     * @param cantidad  dígitos centrales deseados.
     * @return subcadena central seleccionada.
     */
    private String extraerCentrales(String cifras, int cantidad) {
        int sobrante = cifras.length() - cantidad;
        int recorteIzquierdo = sobrante / 2;             // floor: menos a la izquierda
        int recorteDerecho = sobrante - recorteIzquierdo; // ceil: más a la derecha
        return cifras.substring(recorteIzquierdo,
                recorteIzquierdo + cantidad);
    }

    /**
     * @return narración completa: cuadrado, extracción central, módulo y suma.
     */
    @Override
    public String describirCalculo(int clave, int tamanoEstructura) {
        long cuadrado = (long) clave * clave;
        String cifras = Long.toString(cuadrado);
        int objetivo = contarDigitos(tamanoEstructura);

        String extraidoTexto;
        if (cifras.length() <= objetivo) {
            extraidoTexto = cifras + " (el cuadrado ya tiene pocas cifras)";
        } else {
            extraidoTexto = "\"" + extraerCentrales(cifras, objetivo)
                    + "\" (" + objetivo + " cifras centrales hacia la izquierda)";
        }

        int extraido = Integer.parseInt(
                cifras.length() <= objetivo
                        ? cifras
                        : extraerCentrales(cifras, objetivo));

        return "HASH CUADRADO: " + clave + "^2 = " + cuadrado + " (" + cifras.length()
                + " cifras); se extraen " + extraidoTexto + "; " + extraido + " % "
                + tamanoEstructura + " = " + (extraido % tamanoEstructura)
                + "; se suma 1 al final -> direccion " + calcularDireccion(clave, tamanoEstructura);
    }

    /** @return nombre único de la función ("HASH CUADRADO"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}
