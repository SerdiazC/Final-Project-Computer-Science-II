package Model.estructuras;

import Model.EstructuraDeDatos;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA CONTIGUA (BASE DE LAS ESTRUCTURAS SIN HUECOS)
 * ============================================================================
 *
 * Familia de estructuras donde los datos viven SIEMPRE uno tras otro, sin
 * espacios libres intermedios: o en orden de llegada (Secuencial) o en
 * orden ascendente (Ordenada).
 *
 * PATRÓN TEMPLATE METHOD (Open/Closed):
 * El método insertar() está marcado como "final": define EL ALGORITMO
 * GENERAL (validar -> ubicar -> desplazar -> contar) y delega en la
 * subclase UN ÚNICO paso variable: calcularPosicionInsercion().
 *
 * Al eliminar, los datos posteriores se compactan para cerrar el hueco,
 * preservando la densidad que la búsqueda binaria exige.
 */
public abstract class EstructuraContigua extends EstructuraDeDatos {

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
    }

    /**
     * Inserta una clave aplicando SIEMPRE el mismo guion:
     *
     *   FASE 1 - Validaciones de negocio (rango, espacio, duplicados).
     *   FASE 2 - Preguntar a la subclase en qué posición va la clave.
     *   FASE 3 - Abrir hueco desplazando datos hacia la derecha.
     *   FASE 4 - Guardar la clave y actualizar el contador.
     *
     * Es "final" para que ninguna subclase pueda romper el contrato.
     *
     * @param clave valor numérico a almacenar.
     * @throws ExcepcionEstructura si viola alguna regla de negocio.
     */
    @Override
    public final void insertar(int clave) throws ExcepcionEstructura {

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

    /**
     * Compactación al eliminar: cada dato posterior retrocede una posición
     * y el último espacio queda libre, manteniendo el bloque sin huecos.
     *
     * @param posicion índice interno liberado por la clase base.
     */
    @Override
    protected void liberarPosicion(int posicion) {
        for (int i = posicion; i < cantidad - 1; i++) {
            claves[i] = claves[i + 1];
        }
        claves[cantidad - 1] = CLAVE_VACIA;
    }
}
