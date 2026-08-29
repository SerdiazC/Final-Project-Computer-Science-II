package Controller;

import Model.EstructuraDeDatos;
import Model.colisiones.SolucionColision;
import Model.estructuras.EstructuraArbolDigital;
import Model.estructuras.EstructuraHash;
import Model.estructuras.EstructuraOrdenada;
import Model.estructuras.EstructuraSecuencial;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;

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
        HASH_TRUNCAMIENTO,
        /** Transformación de claves con función PLEGAMIENTO. */
        HASH_PLEGAMIENTO,
        /** Árbol digital guiado por los bits de la clave (residuos digital). */
        RESIDUOS_DIGITAL
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
     * REGLA DEL PROYECTO (TODAS las familias de arreglo son hash): la
     * función hash recibida gobierna CÓMO se inserta cada clave —en
     * SECUENCIAL, ORDENADA y en las cuatro HASH—; el árbol digital
     * (RESIDUOS_DIGITAL) la ignora porque crece con su propia regla de bits.
     *
     * @param tipo             tipo concreto deseado.
     * @param digitosClave     dígitos exactos que tendrán sus claves (1..7).
     * @param capacidad        tamaño exacto de la estructura (1..1000).
     * @param funcion          función hash para insertar (null solo válido
     *                         para el árbol digital, que la ignora).
     * @param solucionColision técnica de colisiones para las familias hash
     *                         (las demás la ignoran; puede ser null).
     * @return instancia polimórfica lista para usarse como EstructuraDeDatos.
     * @throws ExcepcionEstructura si la configuración queda fuera de rango.
     */
    public static EstructuraDeDatos crear(TipoEstructura tipo, int digitosClave,
            int capacidad, FuncionHash funcion, SolucionColision solucionColision)
            throws ExcepcionEstructura {

        switch (tipo) {
            case SECUENCIAL:
                return new EstructuraSecuencial(digitosClave, capacidad,
                        funcion, solucionColision);
            case ORDENADA:
                return new EstructuraOrdenada(digitosClave, capacidad,
                        funcion, solucionColision);
            case HASH_MOD:
                return new EstructuraHash(digitosClave, capacidad,
                        funcion, solucionColision);
            case HASH_CUADRADO:
                return new EstructuraHash(digitosClave, capacidad,
                        funcion, solucionColision);
            case HASH_TRUNCAMIENTO:
                return new EstructuraHash(digitosClave, capacidad,
                        funcion, solucionColision);
            case HASH_PLEGAMIENTO:
                return new EstructuraHash(digitosClave, capacidad,
                        funcion, solucionColision);
            case RESIDUOS_DIGITAL:
                // El árbol digital ignora el tamaño y la función: crece con las
                // claves; sus puntos ocupados se resuelven con su regla nativa
                // (avanzar al siguiente bit), no con soluciones hash.
                return new EstructuraArbolDigital(digitosClave);
            default:
                throw new IllegalArgumentException(
                        "Tipo de estructura no soportado: " + tipo);
        }
    }

    /**
     * Traduce el nombre técnico de una estructura (constantes TIPO_* de
     * EstructuraDeDatos, como las reporta getTipo()) al valor del
     * enumerado correspondiente. Lo usa la migración de datos al cambiar
     * de búsqueda.
     *
     * @param tipoTecnico nombre técnico de la estructura.
     * @return tipo construible equivalente.
     * @throws IllegalArgumentException si el nombre no corresponde a ningún tipo.
     */
    public static TipoEstructura tipoDe(String tipoTecnico) {
        switch (tipoTecnico) {
            case EstructuraDeDatos.TIPO_SECUENCIAL:
                return TipoEstructura.SECUENCIAL;
            case EstructuraDeDatos.TIPO_ORDENADA:
                return TipoEstructura.ORDENADA;
            case EstructuraDeDatos.TIPO_HASH_MOD:
                return TipoEstructura.HASH_MOD;
            case EstructuraDeDatos.TIPO_HASH_CUADRADO:
                return TipoEstructura.HASH_CUADRADO;
            case EstructuraDeDatos.TIPO_HASH_TRUNCAMIENTO:
                return TipoEstructura.HASH_TRUNCAMIENTO;
            case EstructuraDeDatos.TIPO_HASH_PLEGAMIENTO:
                return TipoEstructura.HASH_PLEGAMIENTO;
            case EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL:
                return TipoEstructura.RESIDUOS_DIGITAL;
            default:
                throw new IllegalArgumentException(
                        "Tipo de estructura desconocido: " + tipoTecnico);
        }
    }
}
