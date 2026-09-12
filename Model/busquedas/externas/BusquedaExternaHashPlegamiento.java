package Model.busquedas.externas;

import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashPlegamiento;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA POR TRANSFORMACIÓN DE CLAVES - HASH PLEGAMIENTO
 * ============================================================================
 *
 * Busca la clave transformándola con la función PLEGAMIENTO (grupos sumados)
 * y accediendo directo a la cubeta resultante (base 0). Las claves se ubican
 * en la cubeta que les da SU función, así que la estructura debe haber sido
 * configurada con esta misma función (HASH PLEGAMIENTO).
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash plegamiento a la maquinaria
 * común de {@link BusquedaExternaTransformacion}.
 */
public class BusquedaExternaHashPlegamiento extends BusquedaExternaTransformacion {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "HASH PLEGAMIENTO";

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashPlegamiento();

    /** @return la función hash plegamiento. */
    @Override
    public FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único ("HASH PLEGAMIENTO"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}