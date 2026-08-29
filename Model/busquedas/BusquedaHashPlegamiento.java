package Model.busquedas;

import Model.EstructuraDeDatos;
import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashPlegamiento;

/**
 * ============================================================================
 * BÚSQUEDA POR TRANSFORMACIÓN DE CLAVES - HASH PLEGAMIENTO
 * ============================================================================
 *
 * Busca la clave calculando su dirección con la técnica de PLEGAMIENTO:
 * partir la clave en grupos según el tamaño de la estructura, sumar los
 * pliegues (tomando solo los últimos dígitos si la suma excede el límite)
 * y aplicar el cierre estándar con +1 al final.
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash plegamiento a la
 * maquinaria común de {@link BusquedaTransformacion}.
 */
public class BusquedaHashPlegamiento extends BusquedaTransformacion {

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashPlegamiento();

    /** @return la función hash plegamiento. */
    @Override
    protected FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único con el que se registra ("HASH PLEGAMIENTO"). */
    @Override
    public String getNombre() {
        return EstructuraDeDatos.TIPO_HASH_PLEGAMIENTO;
    }

    /** @return estructura sobre la que opera esta búsqueda. */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_HASH_PLEGAMIENTO;
    }
}
