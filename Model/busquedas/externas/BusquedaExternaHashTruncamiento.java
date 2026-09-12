package Model.busquedas.externas;

import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashTruncamiento;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA POR TRANSFORMACIÓN DE CLAVES - HASH TRUNCAMIENTO
 * ============================================================================
 *
 * Busca la clave transformándola con la función TRUNCAMIENTO (primeras
 * cifras) y accediendo directo a la cubeta resultante (base 0). Las claves
 * se ubican en la cubeta que les da SU función, así que la estructura debe
 * haber sido configurada con esta misma función (HASH TRUNCAMIENTO).
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash truncamiento a la maquinaria
 * común de {@link BusquedaExternaTransformacion}.
 */
public class BusquedaExternaHashTruncamiento extends BusquedaExternaTransformacion {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "HASH TRUNCAMIENTO";

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashTruncamiento();

    /** @return la función hash truncamiento. */
    @Override
    public FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único ("HASH TRUNCAMIENTO"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }
}