package Model.estructuras;

import Model.colisiones.SolucionColision;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * ESTRUCTURA ORDENADA (REGLA DEL PROYECTO: INSERCIÓN POR FUNCIÓN HASH)
 * ============================================================================
 *
 * En esta versión del proyecto TODAS las búsquedas internas (lineal, binaria
 * y por transformación) preguntan al inicio qué {@link FuncionHash} se usa
 * para INSERTAR cada clave, y esa decisión NO puede cambiarse sin reiniciar
 * la estructura. Por eso la "estructura ordenada" ya no mantiene las claves
 * ordenadas al insertar: las coloca en la dirección que calcula la función
 * hash elegida, resolviendo las colisiones con la {@link SolucionColision}
 * configurada.
 *
 * DETALLE ACADÉMICO: el usuario aceptó que esto cambia la definición
 * clásica de la búsqueda binaria (su requisito de "datos ordenados" deja de
 * cumplirse); esta clase SOLO aporta la identidad del tipo ("ORDENADA") y
 * hereda de {@link EstructuraHash} toda la mecánica de transformación de
 * claves y de resolución de colisiones.
 */
public class EstructuraOrdenada extends EstructuraHash {

    /**
     * Crea la estructura ordenada como una tabla hash dirigida por la
     * función indicada y con la solución de colisiones elegida.
     *
     * @param digitosClave     dígitos exactos que tendrán las claves (1..7).
     * @param capacidad        tamaño exacto de la estructura (1..1000).
     * @param funcionHash      función que calcula dónde se inserta cada clave.
     * @param solucionColision técnica para TODAS las colisiones (no null).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public EstructuraOrdenada(int digitosClave, int capacidad,
            FuncionHash funcionHash, SolucionColision solucionColision)
            throws ExcepcionEstructura {
        super(digitosClave, capacidad, funcionHash, solucionColision);
    }

    /** @return identificador técnico de este tipo de estructura. */
    @Override
    public String getTipo() {
        return TIPO_ORDENADA;
    }
}