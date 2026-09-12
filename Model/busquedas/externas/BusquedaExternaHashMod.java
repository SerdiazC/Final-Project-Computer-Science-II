package Model.busquedas.externas;

import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashModulo;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA POR TRANSFORMACIÓN DE CLAVES - HASH MOD
 * ============================================================================
 *
 * Busca la clave transformándola con la función MÓDULO y accediendo directo
 * a la cubeta resultante (base 0). Es la búsqueda "natural" de la estructura
 * externa, que por defecto ubica sus claves con el hash mod tradicional
 * (|clave % númeroDeCubetas|).
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash módulo a la maquinaria
 * común de {@link BusquedaExternaTransformacion}.
 */
public class BusquedaExternaHashMod extends BusquedaExternaTransformacion {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "HASH MOD";

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashModulo();

    /** @return la función hash módulo. */
    @Override
    public FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único ("HASH MOD"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}