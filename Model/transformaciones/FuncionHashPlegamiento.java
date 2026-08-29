package Model.transformaciones;

import java.util.StringJoiner;

/**
 * ============================================================================
 * FUNCIÓN HASH PLEGAMIENTO
 * ============================================================================
 *
 * Especificación del proyecto:
 *
 *   1. Mirar el número total de datos de la estructura y, a partir de él,
 *      decidir DE A CUÁNTOS DÍGITOS se asocian: se usan las cifras de la
 *      mayor dirección posible. Ejemplo: tamaño 100 -> mayor dirección 99
 *      -> se asocian de a 2 dígitos.
 *   2. PARTIR la clave en grupos de ese tamaño, de izquierda a derecha.
 *      Ejemplo: clave 2345 con grupos de 2 -> "23" y "45".
 *   3. SUMAR todos los números resultantes.
 *   4. SI la suma SE SALE del límite de la estructura, se toman SOLO los
 *      últimos dígitos (tantos como el ancho de grupo).
 *   5. AL FINAL se suma 1 (tras el ajuste estándar por módulo).
 * <p>
 * Ejemplo completo (tamaño 100, clave 2345):
 *   grupos 23 + 45 = 68; no se sale -> 68 % 100 = 68; +1 -> dirección 69.
 */
public class FuncionHashPlegamiento implements FuncionHash {

    /** Nombre oficial de la función dentro del sistema. */
    public static final String NOMBRE = "HASH PLEGAMIENTO";

    /**
     * Aplica el algoritmo completo de la técnica de plegamiento.
     *
     * @param clave            valor entero positivo a transformar.
     * @param tamanoEstructura tamaño total de la estructura destino.
     * @return dirección resultante en [1 .. tamanoEstructura].
     */
    @Override
    public int calcularDireccion(int clave, int tamanoEstructura) {

        // --- PASO 1: ancho de cada pliegue según el tamaño -------------------
        int cifrasPorGrupo = contarCifrasBase(tamanoEstructura);
        String cifrasClave = Integer.toString(clave);

        // --- PASO 2: partir la clave en grupos de ese ancho -------------------
        int suma = 0;
        for (int inicio = 0; inicio < cifrasClave.length(); inicio += cifrasPorGrupo) {
            int fin = Math.min(inicio + cifrasPorGrupo, cifrasClave.length());
            suma += Integer.parseInt(cifrasClave.substring(inicio, fin));
        }

        // --- PASO 4: si la suma excede el límite, últimos dígitos solamente ---
        int limiteGrupo = (int) Math.pow(10, cifrasPorGrupo);
        int intermedio = (suma >= limiteGrupo) ? (suma % limiteGrupo) : suma;

        // --- PASO 5: cierre estándar del proyecto (módulo del tamaño y +1) ----
        return cerrarCalculo(intermedio, tamanoEstructura);
    }

    /**
     * @return narración completa: ancho de pliegue, grupos formados, suma,
     *         recorte si excede el límite y suma final de 1.
     */
    @Override
    public String describirCalculo(int clave, int tamanoEstructura) {
        int cifrasPorGrupo = contarCifrasBase(tamanoEstructura);
        String cifrasClave = Integer.toString(clave);

        // Reconstruir los grupos solo para narrarlos.
        StringJoiner joiner = new StringJoiner(" + ");
        int suma = 0;
        for (int inicio = 0; inicio < cifrasClave.length(); inicio += cifrasPorGrupo) {
            int fin = Math.min(inicio + cifrasPorGrupo, cifrasClave.length());
            String grupo = cifrasClave.substring(inicio, fin);
            joiner.add(grupo);
            suma += Integer.parseInt(grupo);
        }

        int limiteGrupo = (int) Math.pow(10, cifrasPorGrupo);
        boolean recortada = suma >= limiteGrupo;
        int intermedio = recortada ? (suma % limiteGrupo) : suma;

        String detalleRecorte = recortada
                ? "la suma EXCEDE el límite: se toman solo los últimos "
                        + cifrasPorGrupo + " digitos -> " + intermedio
                : "la suma cabe en el limite";

        return "HASH PLEGAMIENTO: tamaño " + tamanoEstructura + " -> pliegues de "
                + cifrasPorGrupo + " digitos; " + clave + " se parte en "
                + joiner + "; suma = " + suma + "; " + detalleRecorte + "; "
                + intermedio + " % " + tamanoEstructura + " = "
                + (intermedio % tamanoEstructura)
                + "; se suma 1 al final -> direccion "
                + calcularDireccion(clave, tamanoEstructura);
    }

    /** @return nombre único de la función ("HASH PLEGAMIENTO"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}
