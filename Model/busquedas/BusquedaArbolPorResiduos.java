package Model.busquedas;

import Model.EstructuraDeDatos;

/**
 * ============================================================================
 * ÁRBOL DE BÚSQUEDA POR RESIDUOS (ALIAS DEL ÁRBOL DIGITAL)
 * ============================================================================
 *
 * En la clasificación de la asignatura el "árbol de búsqueda por residuos"
 * se resuelve con la MISMA lógica que el árbol digital: la ubicación de
 * cada clave depende de los BITS (residuos sucesivos de dividir entre 2)
 * de su conversión a binario. Por eso esta búsqueda REUTILIZA por herencia
 * todo el comportamiento de {@link BusquedaResiduosDigital}, que continúa
 * siendo el árbol binario guiado por los bits de la clave
 * (EstructuraArbolDigital), solo que se publica bajo otro nombre.
 *
 * Si más adelante se implementara una variante PROPIA (por ejemplo, leer
 * los residuos del menos significativo al más significativo), bastaría con
 * reemplazar la herencia por una implementación independiente sin tocar el
 * resto del sistema (Open/Closed).
 */
public class BusquedaArbolPorResiduos extends BusquedaResiduosDigital {

    /** Nombre con el que se identifica ante el sistema ("ARBOL POR RESIDUOS"). */
    public static final String NOMBRE = "ARBOL POR RESIDUOS";

    /** @return nombre único de la estrategia ("ARBOL POR RESIDUOS"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /** @return estructura sobre la que opera (el árbol de residuos digital). */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL;
    }
}