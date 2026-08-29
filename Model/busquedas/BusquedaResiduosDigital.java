package Model.busquedas;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;
import Model.estructuras.EstructuraArbolDigital;
import Model.estructuras.NodoArbolDigital;

/**
 * ============================================================================
 * BÚSQUEDA POR RESIDUOS DIGITAL (ÁRBOL DIGITAL)
 * ============================================================================
 *
 * Busca una clave DESCENDIENDO por el árbol digital siguiendo los BITS de
 * su conversión a binario, exactamente igual que hizo su inserción:
 *
 *   1. Convertir la clave buscada a código binario (sin ceros a la
 *      izquierda).
 *   2. Empezar en la raíz y leer los bits uno a uno (del más significativo
 *      al menos significativo):
 *        - bit '0' -> bajar a la IZQUIERDA.
 *        - bit '1' -> bajar a la DERECHA.
 *   3. En cada nodo visitado se compara el valor guardado con la clave:
 *        - Coincidencia -> éxito; la posición reportada es el NIVEL del
 *          nodo dentro del árbol (la raíz es el nivel 1).
 *        - Nodo vacío (clave eliminada) -> no coincide, se continúa.
 *   4. Si los bits se agotan, el descenso continúa con CEROS implícitos,
 *      igual que en la inserción.
 *   5. Terminar en un espacio libre significa que la clave NO existe.
 */
public class BusquedaResiduosDigital implements EstrategiaBusqueda {

    /** Nombre con el que se identifica esta estrategia ante el sistema. */
    public static final String NOMBRE = "ARBOL DIGITAL";

    /** Valor centinela para campos de paso sin dato comparable. */
    private static final int SIN_DATO = -1;

    /**
     * Ejecuta la búsqueda descendiendo por el árbol:
     *
     *   FASE 0 - Validar precondiciones (árbol correcto y con datos).
     *   FASE 1 - Narrar la conversión de la clave a binario.
     *   FASE 2 - Comparar el nodo visitado y avanzar según el bit vigente.
     *   FASE 3 - Éxito al coincidir; fallo al llegar a un espacio libre.
     */
    @Override
    public ResultadoBusqueda buscar(EstructuraDeDatos estructura, int claveBuscada) {

        // --- FASE 0: precondiciones ------------------------------------------
        if (!(estructura instanceof EstructuraArbolDigital)) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "La búsqueda digital requiere una estructura de RESIDUOS "
                            + "DIGITAL y se recibió otra familia.",
                    new ArrayList<>());
        }
        EstructuraArbolDigital arbol = (EstructuraArbolDigital) estructura;
        if (arbol.estaVacia()) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada
                            + ": la estructura está vacía.",
                    new ArrayList<>());
        }

        List<PasoBusqueda> pasos = new ArrayList<>();

        // --- FASE 1: conversión inmediata a binario ----------------------------
        String bits = Integer.toBinaryString(claveBuscada);
        pasos.add(new PasoBusqueda(1, SIN_DATO, SIN_DATO, SIN_DATO, SIN_DATO,
                "La clave " + claveBuscada + " convertida a binario es '" + bits
                        + "': se desciende desde la raíz leyendo sus bits "
                        + "('0' hacia la izquierda, '1' hacia la derecha)."));

        // --- FASE 2: descenso comparando cada nodo visitado --------------------
        int numeroPaso = 1;
        NodoArbolDigital actual = arbol.getRaiz();
        int nivel = 1;
        int indiceBit = 0;

        while (actual != null) {

            numeroPaso++;

            if (!actual.estaVacio() && actual.getClave() == claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, nivel, SIN_DATO, SIN_DATO,
                        actual.getClave(),
                        "¡Coincidencia! El nodo del nivel " + nivel
                                + " contiene " + actual.getClave()
                                + ", igual a la clave buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, nivel, pasos);
            }

            // Bit vigente; agotados los propios, se continúa con ceros implícitos.
            char bit = (indiceBit < bits.length())
                    ? bits.charAt(indiceBit)
                    : '0';
            String origenBit = (indiceBit < bits.length())
                    ? "el bit " + (indiceBit + 1) + " ('" + bit + "')"
                    : "un cero implícito ('0')";

            String estadoNodo = actual.estaVacio()
                    ? "El nodo actual está VACÍO (su clave fue eliminada)"
                    : "El nodo actual contiene " + actual.getClave()
                            + ", distinto de la buscada";

            pasos.add(new PasoBusqueda(numeroPaso, nivel, SIN_DATO, SIN_DATO,
                    actual.estaVacio() ? SIN_DATO : actual.getClave(),
                    estadoNodo + "; con " + origenBit + " se baja a la "
                            + ((bit == '0') ? "IZQUIERDA." : "DERECHA.")));

            actual = (bit == '0') ? actual.getIzquierda() : actual.getDerecha();
            indiceBit++;
            nivel++;
        }

        // --- FASE 3b: espacio libre -> la clave no existe -----------------------
        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe en el árbol: el descenso "
                        + "terminó en un espacio libre tras " + numeroPaso + " pasos.",
                pasos);
    }

    /** @return nombre único de la estrategia ("ARBOL DIGITAL"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /** @return estructura sobre la que opera esta búsqueda. */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL;
    }
}
