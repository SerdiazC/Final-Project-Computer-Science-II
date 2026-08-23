package Model.busquedas;

import Model.EstructuraDeDatos;
import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashModulo;

/**
 * ============================================================================
 * BÚSQUEDA POR TRANSFORMACIÓN DE CLAVES - HASH MOD
 * ============================================================================
 *
 * Busca la clave calculando su dirección con la función MÓDULO:
 * dirección = (clave % tamaño) + 1, y accediendo directo a ella.
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash módulo a la maquinaria
 * común de {@link BusquedaTransformacion}.
 */
public class BusquedaHashModulo extends BusquedaTransformacion {

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashModulo();

    /** @return la función hash módulo. */
    @Override
    protected FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único con el que se registra ("HASH MOD"). */
    @Override
    public String getNombre() {
        return EstructuraDeDatos.TIPO_HASH_MOD;
    }

    /** @return estructura sobre la que opera esta búsqueda. */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_HASH_MOD;
    }
}
