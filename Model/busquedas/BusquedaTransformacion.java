package Model.busquedas;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * BÚSQUEDA POR TRANSFORMACIÓN DE CLAVES (CLASE BASE)
 * ============================================================================
 *
 * Todas las búsquedas hash comparten el MISMO flujo de dos movimientos:
 *
 *   PASO 1 - Calcular la dirección de la clave con la función hash:
 *            dirección = función(clave, tamaño)  en [1 .. tamaño].
 *   PASO 2 - Ir DIRECTAMENTE a esa dirección y comparar una sola vez.
 *
 * Resultados posibles del paso 2:
 *   a) La dirección contiene la clave -> éxito inmediato (¡una sola
 *      comparación, sin recorrer nada!).
 *   b) La dirección está vacía -> la clave nunca fue almacenada: fallo.
 *   c) La dirección contiene OTRA clave -> fallo informando quién la ocupa
 *      (consecuencia de la política anti-colisión elegida en inserción).
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
     * Ejecuta la búsqueda directa por transformación de claves.
     */
    @Override
    public final ResultadoBusqueda buscar(EstructuraDeDatos estructura, int claveBuscada) {

        List<PasoBusqueda> pasos = new ArrayList<>();

        if (estructura.estaVacia()) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada + ": la estructura está vacía.",
                    pasos);
        }

        // --- PASO 1: transformación de la clave --------------------------------
        int tamano = estructura.getCapacidad();
        FuncionHash funcion = getFuncionHash();
        int direccion = funcion.calcularDireccion(claveBuscada, tamano);

        pasos.add(new PasoBusqueda(1, SIN_INDICE, SIN_INDICE, SIN_INDICE,
                claveBuscada, funcion.describirCalculo(claveBuscada, tamano)));

        // --- PASO 2: acceso directo y comparación única -------------------------
        int indiceInterno = direccion - 1; // las direcciones son 1-based
        int ocupante = estructura.consultarPosicion(indiceInterno);

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
        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe: su dirección "
                        + direccion + " está ocupada por otra clave (" + ocupante + ").",
                pasos);
    }
}
