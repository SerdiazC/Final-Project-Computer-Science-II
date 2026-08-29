package Model.busquedas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import Model.EstructuraDeDatos;
import Model.estructuras.EstructuraHash;

/**
 * ============================================================================
 * BÚSQUEDA BINARIA (DIVIDIR Y CONQUISTAR)
 * ============================================================================
 *
 * ALGORITMO (según la especificación del proyecto):
 *   1. Ubicarse directamente en la MITAD del rango de POSICIONES FÍSICAS.
 *   2. Comparar el contenido con la buscada para decidir hacia qué mitad
 *      continuar.
 *   3. Repetir sobre el rango restante, partiéndolo siempre a la mitad,
 *      hasta hallar la clave o agotar los rangos pendientes.
 *
 * REGLA DEL PROYECTO (cambio aceptado por el usuario): al insertar con la
 * función hash elegida (se pregunta al iniciar en TODA búsqueda, incluso la
 * binaria), el arreglo ya no queda ordenado por valor dentro de las
 * posiciones físicas. La binaria clásica descarta una mitad COMPARANDO
 * valores, lo que requiere datos ordenados; sobre una tabla direccionada por
 * hash esa decisión podría descartar justamente la mitad donde vive la
 * clave. Para conservar la partición en mitades SIN dejar de encontrar las
 * claves insertadas, esta variante procede así:
 *
 *   • En cada partición se examina el punto medio.
 *   • Si la celda contiene la buscada -> ÉXITO.
 *   • Si la celda está VACÍA, no hay información para decidir: la mitad
 *     contraria queda en una PILA de rangos pendientes y se continúa
 *     partiendo la mitad elegida.
 *   • Si la celda contiene OTRO valor, la comparación orienta la
 *     exploración (mayor -> derecha, menor -> izquierda) y la mitad
 *     contraria también queda en la pila como respaldo.
 *   • Al agotar el rango elegido se desapila el siguiente rango pendiente.
 *
 * De este modo el arreglo físico se visita por COMPLETO (cada posición
 * exactamente una vez, en orden de partición), así que garantiza hallar
 * cualquier clave insertada, incluidas las ubicadas en las cubetas de
 * desbordes de cada dirección hash (que se revisan junto con su celda de
 * entrada, igual que en la búsqueda lineal).
 *
 * Nótese la lección pedagógica: sin datos ordenados por valor, la binaria
 * pierde su poda clásica O(log n) (consulta todas las posiciones físicas);
 * conserva sin embargo su procedimiento de partición en mitades, que es lo
 * que esta estrategia muestra paso a paso.
 */
public class BusquedaBinaria implements EstrategiaBusqueda {

    /** Nombre con el que se identifica esta estrategia ante el sistema. */
    public static final String NOMBRE = "BINARIA";

    /**
     * Ejecuta la búsqueda partiendo el rango de posiciones físicas a la
     * mitad, con la mitad contraria encolada como pendiente:
     *
     *   FASE 0 - Validar que existan datos.
     *   FASE 1 - Partir el rango vigente por la mitad y probar el punto medio.
     *   FASE 2 - Según el contenido: éxito, o dejar la mitad contraria
     *            pendiente y seguir por la elegida (vacío: sin decisión).
     *   FASE 3 - Agotados los rangos pendientes -> fallo ("no encontrado").
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

        List<PasoBusqueda> pasos = new ArrayList<>();
        int numeroPaso = 0;

        // Rango inicial: todas las posiciones físicas son candidatas.
        Deque<int[]> pendientes = new ArrayDeque<>();
        pendientes.push(new int[] { 0, estructura.getCapacidad() - 1 });

        // --- FASE 1..2: partir rangos pendientes hasta cubrirlos todos --------
        while (!pendientes.isEmpty()) {

            int[] rango = pendientes.pop();
            int izquierda = rango[0];
            int derecha = rango[1];
            if (izquierda > derecha) {
                continue;
            }

            // División entera: con cantidad par toma la posición de la izquierda.
            int medio = (izquierda + derecha) / 2;
            numeroPaso++;

            int ocupante = estructura.consultarPosicion(medio);

            if (ocupante == EstructuraDeDatos.CLAVE_VACIA) {
                // Sin valor no se puede decidir: ambas mitades siguen candidatas.
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        EstructuraDeDatos.CLAVE_VACIA,
                        "El punto medio (posición física " + medio + ") está VACÍO: "
                                + "no permite comparar, así que ninguna mitad puede "
                                + "descartarse; se parte el rango " + izquierda + ".."
                                + derecha + " y se exploran sus dos mitades."));
                pendientes.push(new int[] { izquierda, medio - 1 });
                pendientes.push(new int[] { medio + 1, derecha });
                continue;
            }

            if (ocupante == claveBuscada) {
                // --- FASE 2a: coincidencia -> éxito --------------------------
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        ocupante,
                        "¡Coincidencia! El punto medio (posición física " + medio
                                + ") contiene " + ocupante + ", igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, medio, pasos);
            }

            // Comparación orienta la continuación; la mitad contraria queda
            // pendiente en la pila (la tabla no garantiza orden por valor).
            if (ocupante < claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        ocupante,
                        "El punto medio (posición física " + medio + ") contiene "
                                + ocupante + ", MENOR que la buscada: se continúa "
                                + "por la mitad derecha y la izquierda "
                                + izquierda + ".." + (medio - 1) + " queda pendiente."));
                pendientes.push(new int[] { izquierda, medio - 1 });
                pendientes.push(new int[] { medio + 1, derecha });
            } else {
                pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda, derecha,
                        ocupante,
                        "El punto medio (posición física " + medio + ") contiene "
                                + ocupante + ", MAYOR que la buscada: se continúa "
                                + "por la mitad izquierda y la derecha "
                                + (medio + 1) + ".." + derecha + " queda pendiente."));
                pendientes.push(new int[] { medio + 1, derecha });
                pendientes.push(new int[] { izquierda, medio - 1 });
            }

            // --- FASE 2b: revisar la cubeta de la dirección probada ----------
            if (estructura instanceof EstructuraHash) {
                int[] desbordadas = ((EstructuraHash) estructura)
                        .consultarDesbordes(medio);
                for (int desbordada : desbordadas) {
                    numeroPaso++;
                    if (desbordada == claveBuscada) {
                        pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda,
                                derecha, desbordada,
                                "¡Coincidencia! La dirección " + (medio + 1)
                                        + " tiene desbordada la clave " + desbordada
                                        + ", igual a la buscada."));
                        return ResultadoBusqueda.exitosa(claveBuscada, medio, pasos);
                    }
                    pasos.add(new PasoBusqueda(numeroPaso, medio, izquierda,
                            derecha, desbordada,
                            "La cubeta de la dirección " + (medio + 1)
                                    + " contiene desbordada la clave " + desbordada
                                    + " y NO es la buscada."));
                }
            }
        }

        // --- FASE 3: todo el arreglo físico cubierto sin éxito ----------------
        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe en la estructura: se "
                        + "partieron todas las posiciones físicas del arreglo "
                        + "(incluyendo cubetas de desborde) sin hallarla.",
                pasos);
    }

    /** @return nombre único de la estrategia ("BINARIA"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /**
     * La binaria opera sobre las posiciones físicas de la estructura
     * ordenada (hoy hash por inserción); declarar su estructura permite al
     * controlador construir la correcta.
     */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_ORDENADA;
    }
}