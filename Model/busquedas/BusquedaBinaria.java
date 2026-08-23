package Model.busquedas;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;

/**
 * ============================================================================
 * BÚSQUEDA BINARIA (DIVIDIR Y CONQUISTAR)
 * ============================================================================
 *
 * Requiere datos ORDENADOS de menor a mayor, por eso opera exclusivamente
 * sobre la EstructuraOrdenada, que se ordena en cada inserción.
 *
 * ALGORITMO (según la especificación del proyecto):
 *   1. Ubicarse directamente en la MITAD de la estructura.
 *   2. Comparar: si la clave buscada es mayor, descartar la mitad izquierda;
 *      si es menor, descartar la mitad derecha.
 *   3. Repetir sobre el rango restante, partiéndolo siempre a la mitad,
 *      hasta hallar la clave o quedarse sin rango.
 *
 * REGLA ESPECIAL: cuando el rango tiene una cantidad PAR de datos no existe
 * un dato intermedio exacto; se toma el dato de la IZQUIERDA. Esto se logra
 * con división entera: medio = (izquierda + derecha) / 2.
 *
 * Si al final el rango se vacía (izquierda > derecha) o la clave no está,
 * se devuelve el resultado fallido correspondiente ("error" visible).
 */
public class BusquedaBinaria implements EstrategiaBusqueda {

    /** Nombre con el que se identifica esta estrategia ante el sistema. */
    public static final String NOMBRE = "BINARIA";

    /** Valor centinela para pasos que aún no tienen índice explorado. */
    private static final int SIN_INDICE = -1;

    /**
     * Ejecuta la búsqueda reduciendo el problema a la mitad en cada paso:
     *
     *   FASE 0 - Validar precondiciones (datos presentes y ordenados).
     *   FASE 1 - Calcular la mitad del rango vigente y comparar.
     *   FASE 2 - Descartar la mitad que no puede contener la clave.
     *   FASE 3 - Éxito si coincide; fallo si el rango queda vacío.
     */
    @Override
    public ResultadoBusqueda buscar(EstructuraDeDatos estructura, int claveBuscada) {

        // --- FASE 0: precondiciones ------------------------------------------
        if (estructura.estaVacia()) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada
                            + ": la estructura está vacía.",
                    new ArrayList<>());
        }
        if (!estructura.estaOrdenadaAscendente()) {
            // Salvaguarda defensiva: la binaria exige orden ascendente.
            return ResultadoBusqueda.fallida(claveBuscada,
                    "La búsqueda binaria requiere datos ordenados de menor a "
                            + "mayor y la estructura no cumple esa condición.",
                    new ArrayList<>());
        }

        List<PasoBusqueda> pasos = new ArrayList<>();
        int[] claves = estructura.obtenerClaves();
        int izquierda = 0;                       // límite izquierdo del rango vivo
        int derecha = claves.length - 1;         // límite derecho del rango vivo
        int numeroPaso = 0;

        // --- FASE 1..2: partir a la mitad mientras haya rango -----------------
        while (izquierda <= derecha) {

            // División entera: con cantidad par toma el dato de la izquierda.
            int medio = (izquierda + derecha) / 2;
            numeroPaso++;

            if (claves[medio] == claveBuscada) {
                // --- FASE 3a: coincidencia -> éxito --------------------------
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        claves[medio],
                        "¡Coincidencia! El punto medio (posición " + medio
                                + ") contiene " + claves[medio] + ", igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, medio, pasos);
            }

            if (claves[medio] < claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        claves[medio],
                        "El punto medio (posición " + medio + ") contiene "
                                + claves[medio] + ", MENOR que la buscada: se descarta "
                                + "la mitad izquierda y se continúa a la derecha."));
                // --- FASE 2: descartar mitad izquierda ------------------------
                izquierda = medio + 1;
            } else {
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        claves[medio],
                        "El punto medio (posición " + medio + ") contiene "
                                + claves[medio] + ", MAYOR que la buscada: se descarta "
                                + "la mitad derecha y se continúa a la izquierda."));
                // --- FASE 2: descartar mitad derecha ---------------------------
                derecha = medio - 1;
            }
        }

        // --- FASE 3b: rango vacío -> la clave no existe -----------------------
        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe en la estructura: tras "
                        + numeroPaso + " particiones el rango de búsqueda quedó vacío.",
                pasos);
    }

    /** @return nombre único de la estrategia ("BINARIA"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /**
     * La binaria SOLO trabaja sobre la estructura ordenada; declararlo aquí
     * permite al controlador entregarle siempre los datos correctos.
     */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_ORDENADA;
    }
}
