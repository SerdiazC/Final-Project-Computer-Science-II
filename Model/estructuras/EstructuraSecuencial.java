package Model.estructuras;

import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA SECUENCIAL (ORDEN DE LLEGADA)
 * ============================================================================
 *
 * Guarda las claves tal como el usuario las ingresa, SIN reordenarlas.
 * Es la estructura sobre la que opera la BÚSQUEDA LINEAL: como los datos
 * están en cualquier orden, la única garantía de encontrarlos es revisar
 * dato por dato desde el primero.
 *
 * RESPONSABILIDAD ÚNICA: únicamente decide que cada clave nueva va al
 * primer espacio libre. Toda la mecánica común la resuelve
 * {@link EstructuraContigua}.
 */
public class EstructuraSecuencial extends EstructuraContigua {

    /**
     * Crea la estructura secuencial.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @param capacidad    tamaño exacto de la estructura (1..1000).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public EstructuraSecuencial(int digitosClave, int capacidad)
            throws ExcepcionEstructura {
        super(digitosClave, capacidad);
    }

    /**
     * La clave nueva siempre va al final del bloque ocupado: así se conserva
     * fielmente el orden en el que el usuario ingresó los datos.
     *
     * @param clave clave ya validada que se desea ubicar.
     * @return índice igual a la cantidad actual (primer espacio libre).
     */
    @Override
    protected int calcularPosicionInsercion(int clave) {
        return getCantidad();
    }

    /** @return identificador técnico de este tipo de estructura. */
    @Override
    public String getTipo() {
        return TIPO_SECUENCIAL;
    }
}
