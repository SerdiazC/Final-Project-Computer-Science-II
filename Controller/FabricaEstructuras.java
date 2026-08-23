package Controller;

import Model.EstructuraDeDatos;
import Model.estructuras.EstructuraContigua;
import Model.estructuras.EstructuraHash;
import Model.estructuras.EstructuraOrdenada;
import Model.estructuras.EstructuraSecuencial;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHashCuadrado;
import Model.transformaciones.FuncionHashModulo;
import Model.transformaciones.FuncionHashTruncamiento;

/**
 * ============================================================================
 * FÁBRICA DE ESTRUCTURAS (PATRÓN FACTORY METHOD)
 * ============================================================================
 *
 * ÚNICO lugar del sistema que sabe CÓMO construir cada tipo de estructura.
 *
 * PRINCIPIO Open/Closed: si mañana aparece un cuarto tipo solo se agrega su
 * valor al enumerado y una rama en crear(); el resto del sistema no cambia,
 * pues todos dependen de la abstracción EstructuraDeDatos.
 */
public final class FabricaEstructuras {

    /** Tipos de estructura construibles por esta fábrica. */
    public enum TipoEstructura {
        /** Orden de llegada: base de la búsqueda lineal. */
        SECUENCIAL,
        /** Ascendente permanente: base de la búsqueda binaria. */
        ORDENADA,
        /** Transformación de claves con función MÓDULO. */
        HASH_MOD,
        /** Transformación de claves con función CUADRADO. */
        HASH_CUADRADO,
        /** Transformación de claves con función TRUNCAMIENTO. */
        HASH_TRUNCAMIENTO
    }

    /**
     * Constructor privado: es una clase utilitaria y no debe instanciarse.
     */
    private FabricaEstructuras() {
        // Sin estado; evita instancias accidentales.
    }

    /**
     * Crea una estructura nueva y vacía del tipo solicitado.
     *
     * @param tipo         tipo concreto deseado.
     * @param digitosClave dígitos exactos que tendrán sus claves (1..7).
     * @param capacidad    tamaño exacto de la estructura (1..1000).
     * @return instancia polimórfica lista para usarse como EstructuraDeDatos.
     * @throws ExcepcionEstructura si la configuración queda fuera de rango.
     */
    public static EstructuraDeDatos crear(TipoEstructura tipo, int digitosClave, int capacidad)
            throws ExcepcionEstructura {

        switch (tipo) {
            case SECUENCIAL:
                return new EstructuraSecuencial(digitosClave, capacidad);
            case ORDENADA:
                return new EstructuraOrdenada(digitosClave, capacidad);
            case HASH_MOD:
                return new EstructuraHash(digitosClave, capacidad, new FuncionHashModulo());
            case HASH_CUADRADO:
                return new EstructuraHash(digitosClave, capacidad, new FuncionHashCuadrado());
            case HASH_TRUNCAMIENTO:
                return new EstructuraHash(digitosClave, capacidad, new FuncionHashTruncamiento());
            default:
                throw new IllegalArgumentException(
                        "Tipo de estructura no soportado: " + tipo);
        }
    }
}
