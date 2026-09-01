package Model.busquedas.externas;

import java.util.ArrayList;
import java.util.List;

import Model.busquedas.PasoBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.estructuras.externas.EstructuraCubetas;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA HASH MOD (DIRECTA SOBRE LA CUBETA)
 * ============================================================================
 *
 * La búsqueda "natural" de la estructura externa: se calcula la cubeta con
 * el hash mod tradicional (|clave % numCubetas|) y se accede DIRECTAMENTE a
 * esa cubeta. Las claves que colisionaron y quedaron enlazadas en esa
 * misma cubeta se revisan una a una.
 *
 * Es la más eficiente: en un solo salto ubica la cubeta candidata y solo
 * recorre sus enlazadas.
 */
public class BusquedaExternaHashMod {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "HASH MOD";

    /**
     * Ejecuta la búsqueda directa por hash mod sobre las cubetas.
     *
     * @param estructura estructura externa de cubetas.
     * @param claveBuscada valor solicitado.
     * @return resultado con pasos y desenlace.
     */
    public ResultadoBusqueda buscar(EstructuraCubetas estructura, int claveBuscada) {
        if (estructura.getCantidad() == 0) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada
                            + ": la estructura externa está vacía.",
                    new ArrayList<>());
        }

        List<PasoBusqueda> pasos = new ArrayList<>();
        int numeroPaso = 0;
        int totalCubetas = estructura.getNumeroCubetas();
        int cubeta = Math.abs(claveBuscada % totalCubetas);

        numeroPaso++;
        pasos.add(new PasoBusqueda(numeroPaso, cubeta, 0, totalCubetas - 1,
                claveBuscada, "HASH MOD tradicional: cubeta = |" + claveBuscada
                        + " % " + totalCubetas + "| = " + cubeta + ". Se accede "
                        + "directamente a la cubeta " + cubeta + "."));

        int[] datos = estructura.consultarCubeta(cubeta).getDatos();
        if (datos.length == 0) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "La clave " + claveBuscada + " NO existe: su cubeta " + cubeta
                            + " está vacía.",
                    pasos);
        }

        for (int dato : datos) {
            numeroPaso++;
            if (dato == claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, cubeta, 0, totalCubetas - 1,
                        dato, "¡Coincidencia! La cubeta " + cubeta
                                + " (enlazada) contiene " + dato
                                + ", igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, cubeta, pasos);
            }
            pasos.add(new PasoBusqueda(numeroPaso, cubeta, 0, totalCubetas - 1,
                    dato, "La cubeta " + cubeta + " contiene (enlazada) la clave "
                            + dato + " y NO es la buscada."));
        }

        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe: se revisaron todas las "
                        + "claves enlazadas de la cubeta " + cubeta + " sin hallarla.",
                pasos);
    }

    /** @return nombre único ("HASH MOD"). */
    public String getNombre() {
        return NOMBRE;
    }
}
