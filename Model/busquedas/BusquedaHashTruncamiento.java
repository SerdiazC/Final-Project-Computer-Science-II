package Model.busquedas;

import Model.EstructuraDeDatos;
import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashTruncamiento;

/**
 * ============================================================================
 * BÚSQUEDA POR TRANSFORMACIÓN DE CLAVES - HASH TRUNCAMIENTO
 * ============================================================================
 *
 * Busca la clave calculando su dirección con la técnica de TRUNCAMIENTO:
 * tomar tantas posiciones iniciales de la clave como cifras tenga el
 * tamaño, formar el número y sumar 1 al final (tras el módulo).
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash truncamiento a la
 * maquinaria común de {@link BusquedaTransformacion}.
 */
public class BusquedaHashTruncamiento extends BusquedaTransformacion {

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashTruncamiento();

    /** @return la función hash truncamiento. */
    @Override
    protected FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único con el que se registra ("HASH TRUNCAMIENTO"). */
    @Override
    public String getNombre() {
        return EstructuraDeDatos.TIPO_HASH_TRUNCAMIENTO;
    }

    /** @return estructura sobre la que opera esta búsqueda. */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_HASH_TRUNCAMIENTO;
    }
}
