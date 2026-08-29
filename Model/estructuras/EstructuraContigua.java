package Model.estructuras;

import java.util.Arrays;

import Model.EstructuraDeDatos;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA CONTIGUA (BASE DE LAS FAMILIAS BASADAS EN ARREGLO)
 * ============================================================================
 *
 * Familia de estructuras donde los datos viven en un arreglo del tamaño
 * configurado: uno tras otro (Secuencial), ascendente (Ordenada) o en la
 * dirección que calcule una función hash (Hash).
 *
 * PATRÓN TEMPLATE METHOD (Open/Closed):
 * El método insertar() está marcado como "final": define EL ALGORITMO
 * GENERAL (validar -> ubicar -> desplazar -> contar) y delega en la
 * subclase UN ÚNICO paso variable: calcularPosicionInsercion().
 *
 * Al eliminar, cada subclase decide qué hacer con el hueco: las contiguas
 * puras compactan; Hash lo deja vacío para no alterar direcciones.
 */
public abstract class EstructuraContigua extends EstructuraDeDatos {

    /** Arreglo interno; las posiciones libres contienen CLAVE_VACIA. */
    protected final int[] claves;

    /**
     * Crea la estructura contigua.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @param capacidad    tamaño exacto de la estructura (1..1000).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    protected EstructuraContigua(int digitosClave, int capacidad)
            throws ExcepcionEstructura {
        super(digitosClave, capacidad);
        this.claves = new int[capacidad];
        Arrays.fill(this.claves, CLAVE_VACIA);
    }

    // ========================================================================
    // INSERCIÓN (TEMPLATE METHOD)
    // ========================================================================

    /**
     * Inserta una clave aplicando SIEMPRE el mismo guion:
     *
     *   FASE 1 - Validaciones de negocio (rango, espacio, duplicados).
     *   FASE 2 - Preguntar a la subclase en qué posición va la clave.
     *   FASE 3 - Abrir hueco desplazando datos hacia la derecha.
     *   FASE 4 - Guardar la clave y actualizar el contador.
     *
     * NO es "final" a propósito: {@link EstructuraHash} redefine TODO el
     * algoritmo porque su colocación es directa en la dirección calculada
     * (sin desplazamientos). Las familias puramente contiguas heredan este
     * guion intacto.
     *
     * @param clave valor numérico a almacenar.
     * @throws ExcepcionEstructura si viola alguna regla de negocio.
     */
    @Override
    public void insertar(int clave) throws ExcepcionEstructura {

        // --- FASE 1: reglas comunes de negocio -----------------------------
        verificarPuedeInsertar(clave);

        // --- FASE 2: decisión propia de cada tipo de estructura -------------
        int posicionDestino = calcularPosicionInsercion(clave);

        // --- FASE 3: abrir hueco moviendo los datos posteriores -------------
        for (int i = cantidad; i > posicionDestino; i--) {
            claves[i] = claves[i - 1];
        }

        // --- FASE 4: guardar y contar ---------------------------------------
        claves[posicionDestino] = clave;
        cantidad++;
    }

    /**
     * PASO VARIABLE del template method: cada subclase define dónde debe
     * quedar la nueva clave suponiendo que YA pasó las validaciones.
     *
     * @param clave clave ya validada que se desea ubicar.
     * @return índice destino dentro del arreglo interno (0..cantidad).
     */
    protected abstract int calcularPosicionInsercion(int clave);

    // ========================================================================
    // ELIMINACIÓN Y CONSULTAS SOBRE EL ARREGLO
    // ========================================================================

    /**
     * Elimina compactando: cada dato posterior retrocede una posición y el
     * último espacio queda libre, manteniendo el bloque sin huecos.
     *
     * NO es "final" a propósito: {@link EstructuraHash} lo redefinir para
     * dejar la dirección vacía SIN compactar (mover datos alteraría las
     * direcciones calculadas por la función hash).
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si no hay datos.
     * @throws ClaveNoEncontradaException si la clave no existe.
     */
    @Override
    public void eliminar(int clave)
            throws EstructuraVaciaException, ClaveNoEncontradaException {

        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "No hay claves para eliminar: la estructura está vacía.");
        }
        int posicion = indiceDe(clave);
        if (posicion == -1) {
            throw new ClaveNoEncontradaException(
                    "No se puede eliminar: la clave " + clave
                            + " no existe en la estructura.");
        }
        liberarPosicion(posicion);
        cantidad--;
    }

    /**
     * Compactación al eliminar (paso variable de la eliminación): por
     * defecto cierra el hueco; EstructuraHash lo sobrescribe para dejar el
     * espacio vacío sin mover direcciones.
     *
     * @param posicion índice interno liberado.
     */
    protected void liberarPosicion(int posicion) {
        for (int i = posicion; i < cantidad - 1; i++) {
            claves[i] = claves[i + 1];
        }
        claves[cantidad - 1] = CLAVE_VACIA;
    }

    /**
     * Recorrido interno mínimo para ubicar una clave guardada.
     *
     * @param clave valor buscado.
     * @return índice donde está la clave, o -1 si no aparece.
     */
    protected int indiceDe(int clave) {
        for (int i = 0; i < getCapacidad(); i++) {
            if (claves[i] != CLAVE_VACIA && claves[i] == clave) {
                return i;
            }
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contieneClave(int clave) {
        return indiceDe(clave) != -1;
    }

    /**
     * {@inheritDoc} Recorre TODO el arreglo interno y copia solo los
     * espacios ocupados preservando su orden: así los huecos dejados por
     * Hash nunca aparecen en el listado ni en migraciones.
     */
    @Override
    public int[] obtenerClaves() {
        int[] resultado = new int[cantidad];
        int escritura = 0;
        for (int i = 0; i < getCapacidad(); i++) {
            if (claves[i] != CLAVE_VACIA) {
                resultado[escritura] = claves[i];
                escritura++;
            }
        }
        return resultado;
    }

    /** {@inheritDoc} Las familias de arreglo sí son direccionables. */
    @Override
    public int consultarPosicion(int indice) {
        if (indice < 0 || indice >= getCapacidad()) {
            return CLAVE_VACIA;
        }
        return claves[indice];
    }

    /** {@inheritDoc} Compara solo las claves ocupadas, ignorando huecos. */
    @Override
    public boolean estaOrdenadaAscendente() {
        int anterior = CLAVE_VACIA;
        for (int i = 0; i < getCapacidad(); i++) {
            int actual = claves[i];
            if (actual == CLAVE_VACIA) {
                continue;
            }
            if (anterior != CLAVE_VACIA && anterior > actual) {
                return false;
            }
            anterior = actual;
        }
        return true;
    }
}
