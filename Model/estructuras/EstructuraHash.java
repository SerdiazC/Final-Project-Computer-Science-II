package Model.estructuras;

import Model.EstructuraDeDatos;
import Model.excepciones.ColisionHashException;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * ESTRUCTURA HASH (TRANSFORMACIÓN DE CLAVES)
 * ============================================================================
 *
 * Familia de estructuras donde cada clave NO se guarda junto a las demás,
 * sino en la DIRECCIÓN que calcula una {@link FuncionHash}:
 *
 *   dirección = funciónHash(clave, tamaño)   con dirección en [1 .. tamaño]
 *
 * La misma clase sirve para HASH MOD, HASH CUADRADO y HASH TRUNCAMIENTO:
 * solo cambia la función inyectada (COMPOSICIÓN sobre herencia; principio
 * Open/Closed: nuevas funciones no exigen nuevas clases de estructura).
 *
 * POLÍTICA DE COLISIONES ACORDADA: si la dirección ya está ocupada por otra
 * clave, la inserción se RECHAZA informando la colisión (no se sondea).
 * Al eliminar, el espacio simplemente queda vacío (hueco), pues la ubicación
 * depende exclusivamente del cálculo hash.
 */
public class EstructuraHash extends EstructuraDeDatos {

    /** Función de transformación de claves que gobierna las direcciones. */
    private final FuncionHash funcionHash;

    /**
     * Crea la estructura dispersa.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @param capacidad    tamaño exacto de la estructura (1..1000).
     * @param funcionHash  estrategia que calcula las direcciones.
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public EstructuraHash(int digitosClave, int capacidad, FuncionHash funcionHash)
            throws ExcepcionEstructura {
        super(digitosClave, capacidad);
        this.funcionHash = funcionHash;
    }

    /** @return la función hash asociada a esta estructura. */
    public FuncionHash getFuncionHash() {
        return funcionHash;
    }

    /**
     * Calcula la dirección (1-based) que esta estructura asignaría a la clave.
     *
     * @param clave valor a transformar.
     * @return dirección resultante entre 1 y getCapacidad().
     */
    public int calcularDireccion(int clave) {
        return funcionHash.calcularDireccion(clave, getCapacidad());
    }

    /**
     * Fase de reserva: además de las reglas comunes, comprueba que la
     * dirección calculada esté LIBRE. Así el gestor puede validar TODAS las
     * estructuras antes de modificar cualquiera (inserciones atómicas).
     *
     * @param clave valor que se pretende almacenar.
     * @throws ExcepcionEstructura regla común incumplida o colisión.
     */
    @Override
    public void verificarPuedeInsertar(int clave) throws ExcepcionEstructura {
        super.verificarPuedeInsertar(clave);

        int direccion = calcularDireccion(clave);
        int ocupante = consultarPosicion(direccion - 1);
        if (ocupante != CLAVE_VACIA) {
            throw new ColisionHashException(
                    "Colisión con " + funcionHash.getNombre() + ": la dirección "
                            + direccion + " ya ocupa la clave " + ocupante + ".");
        }
    }

    /**
     * Guarda la clave EXACTAMENTE en su dirección calculada:
     *
     *   FASE 1 - Validaciones comunes + colisión (vía verificación).
     *   FASE 2 - Calcular la dirección con la función hash.
     *   FASE 3 - Ubicar la clave en ese espacio y contar.
     *
     * @param clave valor numérico a almacenar.
     * @throws ExcepcionEstructura si viola alguna regla de negocio.
     */
    @Override
    public void insertar(int clave) throws ExcepcionEstructura {

        // --- FASE 1: mismas garantías de siempre ----------------------------
        verificarPuedeInsertar(clave);

        // --- FASE 2: transformación de la clave ------------------------------
        int direccion = calcularDireccion(clave);

        // --- FASE 3: colocación directa y conteo -----------------------------
        claves[direccion - 1] = clave;
        cantidad++;
    }

    /** @return el nombre de la función hash como tipo de estructura. */
    @Override
    public String getTipo() {
        return funcionHash.getNombre();
    }
}
