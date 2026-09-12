package Model.busquedas.externas;

import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashCuadrado;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA POR TRANSFORMACIÓN DE CLAVES - HASH CUADRADO
 * ============================================================================
 *
 * Busca la clave transformándola con la función CUADRADO (dígitos centrales)
 * y accediendo directo a la cubeta resultante (base 0). Las claves se ubican
 * en la cubeta que les da SU función, así que la estructura debe haber sido
 * configurada con esta misma función (HASH CUADRADO).
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash cuadrado a la maquinaria
 * común de {@link BusquedaExternaTransformacion}.
 */
public class BusquedaExternaHashCuadrado extends BusquedaExternaTransformacion {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "HASH CUADRADO";

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashCuadrado();

    /** @return la función hash cuadrado. */
    @Override
    public FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único ("HASH CUADRADO"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}