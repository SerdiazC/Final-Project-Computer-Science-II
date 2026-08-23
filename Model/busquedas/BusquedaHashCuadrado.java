package Model.busquedas;

import Model.EstructuraDeDatos;
import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashCuadrado;

/**
 * ============================================================================
 * BÚSQUEDA POR TRANSFORMACIÓN DE CLAVES - HASH CUADRADO
 * ============================================================================
 *
 * Busca la clave calculando su dirección con la técnica del CUADRADO:
 * elevar la clave, extraer los dígitos centrales según las cifras del
 * tamaño (hacia la izquierda si el sobrante es impar) y sumar 1 al final.
 *
 * RESPONSABILIDAD ÚNICA: aportar la función hash cuadrado a la maquinaria
 * común de {@link BusquedaTransformacion}.
 */
public class BusquedaHashCuadrado extends BusquedaTransformacion {

    /** Función de transformación usada por esta búsqueda. */
    private final FuncionHash funcion = new FuncionHashCuadrado();

    /** @return la función hash cuadrado. */
    @Override
    protected FuncionHash getFuncionHash() {
        return funcion;
    }

    /** @return nombre único con el que se registra ("HASH CUADRADO"). */
    @Override
    public String getNombre() {
        return EstructuraDeDatos.TIPO_HASH_CUADRADO;
    }

    /** @return estructura sobre la que opera esta búsqueda. */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_HASH_CUADRADO;
    }
}
