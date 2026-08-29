package Model.busquedas;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;
import Model.colisiones.SolucionColision;
import Model.estructuras.EstructuraHash;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * BÚSQUEDA POR TRANSFORMACIÓN DE CLAVES (CLASE BASE)
 * ============================================================================
 *
 * Todas las búsquedas hash comparten el MISMO flujo:
 *
 *   PASO 1 - Calcular la dirección de la clave con la función hash:
 *            dirección = función(clave, tamaño)  en [1 .. tamaño].
 *   PASO 2 - Ir DIRECTAMENTE a esa dirección y comparar.
 *
 * Resultados posibles del paso 2:
 *   a) La dirección contiene la clave -> éxito inmediato.
 *   b) La dirección está vacía -> la clave nunca fue almacenada: fallo.
 *   c) La dirección tiene OTRA clave -> COLISIÓN en búsqueda: se aplica la
 *      MISMA solución configurada en la estructura:
 *
 *        SONDEO (lineal/cuadrática/doble): se prueban las posiciones
 *        alternas una a una hasta hallar la clave, toparse con un espacio
 *        libre (la clave no está: convención de las implementaciones de
 *        referencia) o agotar los intentos.
 *
 *        CUBETAS (encadenamiento/anidado): se recorren las claves
 *        desbordadas de ESA dirección en su orden de llegada.
 *
 * Cada subclase solo aporta SU función hash y su nombre (Strategy):
 * agregar una función nueva no toca esta lógica compartida (Open/Closed).
 */
public abstract class BusquedaTransformacion implements EstrategiaBusqueda {

    /** Valor centinela para pasos sin índice explorado aún. */
    protected static final int SIN_INDICE = -1;

    /**
     * @return la función hash que caracteriza esta búsqueda concreta.
     */
    protected abstract FuncionHash getFuncionHash();

    /**
     * Ejecuta la búsqueda directa por transformación de claves, resolviendo
     * cualquier colisión con la solución de la estructura.
     */
    @Override
    public final ResultadoBusqueda buscar(EstructuraDeDatos estructura, int claveBuscada) {

        List<PasoBusqueda> pasos = new ArrayList<>();

        if (!(estructura instanceof EstructuraHash)) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "La búsqueda por transformación requiere una estructura HASH.",
                    pasos);
        }
        if (estructura.estaVacia()) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada + ": la estructura está vacía.",
                    pasos);
        }

        EstructuraHash hash = (EstructuraHash) estructura;

        // --- PASO 1: transformación de la clave --------------------------------
        int tamano = hash.getCapacidad();
        FuncionHash funcion = getFuncionHash();
        int direccion = funcion.calcularDireccion(claveBuscada, tamano);

        pasos.add(new PasoBusqueda(1, SIN_INDICE, SIN_INDICE, SIN_INDICE,
                claveBuscada, funcion.describirCalculo(claveBuscada, tamano)));

        // --- PASO 2: acceso directo y comparación -------------------------------
        int indiceInterno = direccion - 1; // las direcciones son 1-based
        int ocupante = hash.consultarPosicion(indiceInterno);

        pasos.add(new PasoBusqueda(2, indiceInterno, SIN_INDICE, SIN_INDICE,
                ocupante, "Se accede DIRECTAMENTE a la direccion " + direccion
                        + " (posicion interna " + indiceInterno + ") y contiene: "
                        + (ocupante == EstructuraDeDatos.CLAVE_VACIA
                                ? "ESPACIO VACIO"
                                : String.valueOf(ocupante))));

        if (ocupante == claveBuscada) {
            pasos.add(new PasoBusqueda(3, indiceInterno, SIN_INDICE, SIN_INDICE,
                    ocupante, "¡Coincidencia! La direccion " + direccion
                            + " contiene justamente la clave buscada."));
            return ResultadoBusqueda.exitosa(claveBuscada, indiceInterno, pasos);
        }
        if (ocupante == EstructuraDeDatos.CLAVE_VACIA) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "La clave " + claveBuscada + " NO existe: su dirección "
                            + direccion + " está vacía.",
                    pasos);
        }

        // --- PASO 3+: la dirección está ocupada por OTRA clave -> colisión ------
        return resolverColision(hash, claveBuscada, direccion, indiceInterno, pasos);
    }

    /**
     * Aplica la solución de colisiones configurada en la estructura para
     * continuar el rastreo de la clave buscada.
     *
     * @param hash          estructura hash activa.
     * @param claveBuscada  clave solicitada.
     * @param direccion     dirección original calculada (1-based).
     * @param indiceInterno índice interno (base 0) de esa dirección.
     * @param pasos         lista donde se registran los pasos adicionales.
     * @return resultado final tras aplicar la solución.
     */
    private ResultadoBusqueda resolverColision(EstructuraHash hash, int claveBuscada,
            int direccion, int indiceInterno, List<PasoBusqueda> pasos) {

        SolucionColision solucion = hash.getSolucionColision();
        int numeroPaso = pasos.size();

        pasos.add(new PasoBusqueda(++numeroPaso, indiceInterno, SIN_INDICE,
                SIN_INDICE, hash.consultarPosicion(indiceInterno),
                "COLISION: la direccion " + direccion + " está ocupada por otra "
                        + "clave. Se aplica la solución configurada: "
                        + solucion.getNombre() + "."));

        if (!solucion.esPorSondeo()) {
            return rastrearCubeta(hash, claveBuscada, direccion, indiceInterno,
                    numeroPaso, pasos);
        }
        return rastrearSondeo(hash, claveBuscada, direccion, indiceInterno,
                numeroPaso, pasos);
    }

    /**
     * Rastreo por cubetas: recorre los desbordes de la dirección original.
     */
    private ResultadoBusqueda rastrearCubeta(EstructuraHash hash, int claveBuscada,
            int direccion, int indiceInterno, int numeroPasoBase,
            List<PasoBusqueda> pasos) {

        int[] desbordadas = hash.consultarDesbordes(indiceInterno);

        if (desbordadas.length == 0) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "La clave " + claveBuscada + " NO existe: su dirección "
                            + direccion + " no tiene claves desbordadas.",
                    pasos);
        }

        StringBuilder listado = new StringBuilder("[");
        for (int i = 0; i < desbordadas.length; i++) {
            if (i > 0) {
                listado.append(", ");
            }
            listado.append(desbordadas[i]);
        }
        listado.append("]");

        int numeroPaso = numeroPasoBase;
        for (int i = 0; i < desbordadas.length; i++) {
            int candidata = desbordadas[i];
            numeroPaso++;
            if (candidata == claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, indiceInterno,
                        SIN_INDICE, SIN_INDICE, candidata,
                        "Se recorre la cadena de la direccion " + direccion
                                + ": ¡Coincidencia! Contiene " + candidata + "."));
                return ResultadoBusqueda.exitosa(claveBuscada, indiceInterno, pasos);
            }
            pasos.add(new PasoBusqueda(numeroPaso, indiceInterno,
                    SIN_INDICE, SIN_INDICE, candidata,
                    "Se recorre la cadena de la direccion " + direccion
                            + " (" + listado + "): " + candidata
                            + " no es la buscada."));
        }

        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe: se recorrió toda la "
                        + "cadena de la dirección " + direccion + " sin hallarla.",
                pasos);
    }

    /**
     * Rastreo por sondeo: prueba posiciones alternas con la solución.
     */
    private ResultadoBusqueda rastrearSondeo(EstructuraHash hash, int claveBuscada,
            int direccion, int indiceInterno, int numeroPasoBase,
            List<PasoBusqueda> pasos) {

        SolucionColision solucion = hash.getSolucionColision();
        int capacidad = hash.getCapacidad();
        int numeroPaso = numeroPasoBase;

        for (int intento = 1; intento <= capacidad - 1; intento++) {
            int candidato = solucion.calcularIndice(indiceInterno, intento,
                    claveBuscada, capacidad);
            int ocupante = hash.consultarPosicion(candidato);
            numeroPaso++;

            String avance = "Intento " + intento + " con " + solucion.getNombre()
                    + ": desde la direccion " + direccion + " se llega a la "
                    + "posicion interna " + candidato;

            if (ocupante == claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, candidato, SIN_INDICE,
                        SIN_INDICE, ocupante,
                        avance + ", que SÍ contiene la clave buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, candidato, pasos);
            }
            if (ocupante == EstructuraDeDatos.CLAVE_VACIA) {
                pasos.add(new PasoBusqueda(numeroPaso, candidato, SIN_INDICE,
                        SIN_INDICE, SIN_INDICE,
                        avance + ", que está LIBRE."));
                return ResultadoBusqueda.fallida(claveBuscada,
                        "La clave " + claveBuscada + " NO existe: el sondeo se "
                                + "detuvo al hallar un espacio libre en el intento "
                                + intento + ".",
                        pasos);
            }
            pasos.add(new PasoBusqueda(numeroPaso, candidato, SIN_INDICE,
                    SIN_INDICE, ocupante,
                    avance + ", ocupada por " + ocupante + ": se continúa."));
        }

        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe: se agotaron los "
                        + (capacidad - 1) + " intentos del sondeo con "
                        + solucion.getNombre() + ".",
                pasos);
    }
}
