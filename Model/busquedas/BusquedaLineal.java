package Model.busquedas;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;
import Model.estructuras.EstructuraHash;

/**
 * ============================================================================
 * BÚSQUEDA LINEAL (SECUENCIAL)
 * ============================================================================
 *
 * Recorre la estructura DESDE LA PRIMERA POSICIÓN FÍSICA hasta encontrar la
 * clave solicitada. Si termina el recorrido sin hallarla, devuelve un
 * resultado fallido (el "error" visible para el usuario).
 *
 * REGLA DEL PROYECTO: las claves se insertan con la función hash elegida,
 * así que la estructura puede tener VACÍOS entre posiciones ocupadas. La
 * lineal avanza por el arreglo físico completo (0..capacidad-1): cuando una
 * posición está vacía lo registra y sigue, y cuando la posición es la
 * dirección de entrada de una cubeta de desbordes, revisa también esa
 * cubeta. Así NINGUNA clave almacenada queda fuera del recorrido.
 *
 * En cada comparación registra un {@link PasoBusqueda} (con su ÍNDICE
 * FÍSICO) para que la vista web coloree la celda correcta.
 *
 * RESPONSABILIDAD ÚNICA: ejecutar y narrar la búsqueda lineal.
 */
public class BusquedaLineal implements EstrategiaBusqueda {

    /** Nombre con el que se identifica esta estrategia ante el sistema. */
    public static final String NOMBRE = "LINEAL";

    /**
     * Ejecuta la búsqueda línea a línea sobre las posiciones físicas:
     *
     *   FASE 0 - Validar que existan datos.
     *   FASE 1 - Comparar la clave con cada posición desde la física 0,
     *            incluyendo los desbordes de cada dirección hash.
     *   FASE 2 - Al coincidir: éxito con la posición actual.
     *   FASE 3 - Si el recorrido termina: fallo ("no encontrado").
     */
    @Override
    public ResultadoBusqueda buscar(EstructuraDeDatos estructura, int claveBuscada) {

        // --- FASE 0: sin datos no hay nada que recorrer ----------------------
        if (estructura.estaVacia()) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada + ": la estructura está vacía.",
                    new ArrayList<>());
        }

        List<PasoBusqueda> pasos = new ArrayList<>();
        int capacidad = estructura.getCapacidad();
        int limiteSuperior = capacidad - 1;
        int numeroPaso = 0;

        // --- FASE 1: revisión secuencial por POSICIÓN FÍSICA -----------------
        for (int indice = 0; indice < capacidad; indice++) {
            int ocupante = estructura.consultarPosicion(indice);

            if (ocupante == EstructuraDeDatos.CLAVE_VACIA) {
                numeroPaso++;
                pasos.add(new PasoBusqueda(numeroPaso, indice, 0, limiteSuperior,
                        EstructuraDeDatos.CLAVE_VACIA,
                        "La posición física " + indice + " está VACÍA: no puede "
                                + "contener la buscada; se continúa con la siguiente."));
                continue;
            }

            numeroPaso++;
            if (ocupante == claveBuscada) {
                // --- FASE 2a: coincidencia en el arreglo principal -----------
                pasos.add(new PasoBusqueda(numeroPaso, indice, 0, limiteSuperior,
                        ocupante,
                        "¡Coincidencia! La posición física " + indice + " contiene "
                                + ocupante + ", igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, indice, pasos);
            }

            pasos.add(new PasoBusqueda(numeroPaso, indice, 0, limiteSuperior,
                    ocupante,
                    "La posición física " + indice + " contiene " + ocupante
                            + " y NO es la buscada; se revisa su cubeta de "
                            + "desbordes (si existe) y luego el siguiente dato."));

            // --- FASE 2b: revisar también la cubeta de esa dirección ----------
            if (estructura instanceof EstructuraHash) {
                int[] desbordadas = ((EstructuraHash) estructura)
                        .consultarDesbordes(indice);
                for (int desbordada : desbordadas) {
                    numeroPaso++;
                    if (desbordada == claveBuscada) {
                        pasos.add(new PasoBusqueda(numeroPaso, indice, 0,
                                limiteSuperior, desbordada,
                                "¡Coincidencia! La dirección " + (indice + 1)
                                        + " tiene desbordada la clave "
                                        + desbordada + ", igual a la buscada."));
                        return ResultadoBusqueda.exitosa(claveBuscada, indice, pasos);
                    }
                    pasos.add(new PasoBusqueda(numeroPaso, indice, 0,
                            limiteSuperior, desbordada,
                            "La cubeta de la dirección " + (indice + 1)
                                    + " contiene desbordada la clave " + desbordada
                                    + " y NO es la buscada."));
                }
            }
        }

        // --- FASE 3: recorrido completo sin éxito ----------------------------
        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe en la estructura: se "
                        + "recorrieron las " + numeroPaso + " posiciones del "
                        + "arreglo (incluyendo cubetas de desborde) sin hallarla.",
                pasos);
    }

    /** @return nombre único de la estrategia ("LINEAL"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /**
     * La lineal opera sobre la estructura física sin supuestos sobre el
     * orden de los datos: puede buscar en cualquier estructura de arreglo.
     */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_SECUENCIAL;
    }
}