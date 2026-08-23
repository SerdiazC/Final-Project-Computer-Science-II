package Model.busquedas;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;

/**
 * ============================================================================
 * BÚSQUEDA LINEAL (SECUENCIAL)
 * ============================================================================
 *
 * Recorre la estructura DESDE EL PRIMER DATO hasta encontrar la clave
 * solicitada. Si termina el recorrido sin hallarla, devuelve un resultado
 * fallido (el "error" visible para el usuario).
 *
 * Es la búsqueda natural para la EstructuraSecuencial: al estar los datos en
 * orden de llegada no hay suposiciones que aceleren el proceso.
 *
 * En cada comparación registra un {@link PasoBusqueda} para que la futura
 * vista web muestre gráficamente cómo avanza dato por dato.
 *
 * RESPONSABILIDAD ÚNICA: ejecutar y narrar la búsqueda lineal.
 */
public class BusquedaLineal implements EstrategiaBusqueda {

    /** Nombre con el que se identifica esta estrategia ante el sistema. */
    public static final String NOMBRE = "LINEAL";

    /**
     * Ejecuta la búsqueda línea a línea:
     *
     *   FASE 0 - Validar que existan datos.
     *   FASE 1 - Comparar la clave con cada dato desde la posición 0.
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
        int[] claves = estructura.obtenerClaves();
        int limiteSuperior = claves.length - 1;
        int numeroPaso = 0;

        // --- FASE 1: revisión secuencial desde el primer dato ----------------
        for (int indice = 0; indice < claves.length; indice++) {
            numeroPaso++;

            if (claves[indice] == claveBuscada) {
                // --- FASE 2: coincidencia -> éxito --------------------------
                pasos.add(new PasoBusqueda(numeroPaso, indice, 0, limiteSuperior,
                        claves[indice],
                        "¡Coincidencia! La posición " + indice + " contiene "
                                + claves[indice] + ", igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, indice, pasos);
            }

            pasos.add(new PasoBusqueda(numeroPaso, indice, 0, limiteSuperior,
                    claves[indice],
                    "La posición " + indice + " contiene " + claves[indice]
                            + " y NO es la buscada; continúa con el siguiente dato."));
        }

        // --- FASE 3: recorrido completo sin éxito ----------------------------
        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe en la estructura: se "
                        + "recorrieron las " + claves.length + " posiciones sin hallarla.",
                pasos);
    }

    /** @return nombre único de la estrategia ("LINEAL"). */
    @Override
    public String getNombre() {
        return NOMBRE;
    }

    /**
     * La lineal opera sobre la estructura en orden de llegada: al no haber
     * supuestos sobre el orden de los datos, puede buscar en cualquier
     * estructura sin restricciones.
     */
    @Override
    public String getEstructuraRequerida() {
        return EstructuraDeDatos.TIPO_SECUENCIAL;
    }
}
