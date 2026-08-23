package Model.estructuras;

import Model.EstructuraDeDatos;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA ORDENADA (ASCENDENTE PERMANENTE)
 * ============================================================================
 *
 * Mantiene las claves ordenadas de MENOR A MAYOR en todo momento: cada vez
 * que el usuario ingresa una clave, esta se coloca directamente en su sitio,
 * desplazando los mayores un espacio a la derecha.
 *
 * Es la estructura sobre la que opera la BÚSQUEDA BINARIA. Ordenar durante
 * la inserción (y no justo antes de buscar) garantiza que el requisito de
 * la binaria —trabajar sobre datos ordenados— SIEMPRE se cumple.
 *
 * RESPONSABILIDAD ÚNICA: decidir en qué posición va la clave para conservar
 * el orden ascendente. La mecánica común vive en {@link EstructuraContigua}.
 */
public class EstructuraOrdenada extends EstructuraContigua {

    /**
     * Crea la estructura ordenada.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @param capacidad    tamaño exacto de la estructura (1..1000).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public EstructuraOrdenada(int digitosClave, int capacidad)
            throws ExcepcionEstructura {
        super(digitosClave, capacidad);
    }

    /**
     * Ubica la clave recorriendo desde el final hacia atrás: mientras las
     * claves ya guardadas sean MAYORES que la nueva, estas deberán quedar a
     * su derecha; cuando aparece una clave menor o igual, ahí va la nueva.
     *
     * Ejemplo con [1000, 3000, 7000] e insertando 5000:
     *   - 7000 > 5000 -> sigue retrocediendo.
     *   - 3000 <= 5000 -> la posición destino es el índice de 7000.
     *   Resultado: [1000, 3000, 5000, 7000].
     *
     * @param clave clave ya validada y no repetida que se desea ubicar.
     * @return índice donde la clave queda en orden ascendente.
     */
    @Override
    protected int calcularPosicionInsercion(int clave) {
        int posicion = getCantidad();
        int[] actuales = obtenerClaves();

        while (posicion > 0 && actuales[posicion - 1] > clave) {
            posicion--;
        }
        return posicion;
    }

    /** @return identificador técnico de este tipo de estructura. */
    @Override
    public String getTipo() {
        return TIPO_ORDENADA;
    }
}
