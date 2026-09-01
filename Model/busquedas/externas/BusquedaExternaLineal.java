package Model.busquedas.externas;

import java.util.ArrayList;
import java.util.List;

import Model.busquedas.PasoBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.estructuras.externas.EstructuraCubetas;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA LINEAL (SECUENCIAL SOBRE CUBETAS)
 * ============================================================================
 *
 * Recorre las cubetas en su orden físico (cubeta 0, cubeta 1, ...) y dentro
 * de cada cubeta revisa sus claves en el orden en que quedaron "enlazadas"
 * (incluidas las que llegaron por colisión). Es la búsqueda externa más
 * simple: no aprovecha el hash, pero garantiza revisar TODO.
 *
 * Cada paso registra la CUBETA (índice base 0) que se está examinando, para
 * que la interfaz la ilumine. Dentro de una cubeta, cada clave comparada
 * se registra como consulta a esa misma cubeta.
 */
public class BusquedaExternaLineal {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "LINEAL";

    /**
     * Ejecuta la búsqueda lineal sobre las cubetas.
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

        for (int i = 0; i < totalCubetas; i++) {
            int[] datos = estructura.consultarCubeta(i).getDatos();
            if (datos.length == 0) {
                numeroPaso++;
                pasos.add(new PasoBusqueda(numeroPaso, i, 0, totalCubetas - 1,
                        -1, "La cubeta " + i + " está VACÍA: no puede contener "
                                + "la buscada; se pasa a la siguiente."));
                continue;
            }
            for (int dato : datos) {
                numeroPaso++;
                if (dato == claveBuscada) {
                    pasos.add(new PasoBusqueda(numeroPaso, i, 0, totalCubetas - 1,
                            dato, "¡Coincidencia! La cubeta " + i + " (enlazada) "
                                    + "contiene " + dato + ", igual a la buscada."));
                    return ResultadoBusqueda.exitosa(claveBuscada, i, pasos);
                }
                pasos.add(new PasoBusqueda(numeroPaso, i, 0, totalCubetas - 1,
                        dato, "La cubeta " + i + " contiene (enlazada) la clave "
                                + dato + " y NO es la buscada; se continúa."));
            }
        }

        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada
                        + " NO existe: se recorrieron las " + totalCubetas
                        + " cubetas (y sus enlazadas) sin hallarla.",
                pasos);
    }

    /** @return nombre único ("LINEAL"). */
    public String getNombre() {
        return NOMBRE;
    }
}
