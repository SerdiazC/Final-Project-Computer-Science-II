package Model.busquedas.externas;

import java.util.ArrayList;
import java.util.List;

import Model.busquedas.PasoBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.estructuras.externas.EstructuraCubetas;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA POR TRANSFORMACIÓN DE CLAVES (CLASE BASE)
 * ============================================================================
 *
 * La versión EXTERNA de la "búsqueda por transformación de claves": en lugar
 * de un arreglo se trabaja sobre la estructura dinámica de CUBETAS,
 * conservando sus características propias (cubeta con UN solo espacio y
 * claves enlazadas por colisión, índice base 0, crecimiento y reducción
 * dinámicos).
 *
 * Todas las búsquedas hash externas comparten el MISMO flujo:
 *
 *   PASO 1 - Transformar la clave con la FUNCIÓN HASH configurada:
 *            direccion = función(clave, númeroDeCubetas), dirección 1-based
 *            como en las búsquedas internas (la interfaz FuncionHash la
 *            garantiza en [1 .. númeroDeCubetas]).
 *   PASO 2 - Convertir la dirección a la CUBETA base 0: cubeta = dirección - 1.
 *   PASO 3 - Acceder DIRECTAMENTE a esa cubeta y revisar sus claves (la
 *            principal y las enlazadas por colisión), en su orden de llegada.
 *
 * Resultados posibles del paso 3:
 *   a) La cubeta contiene la clave -> éxito inmediato.
 *   b) La cubeta está vacía -> la clave nunca fue almacenada: fallo.
 *   c) La cubeta tiene OTRAS claves -> se recorren TODAS (la estructura
 *      guarda cada clave en la cubeta que le da SU función), si no aparece
 *      es que no existe: fallo.
 *
 * Cada subclase solo aporta SU función hash y su nombre (Strategy):
 * agregar una función nueva no toca esta lógica compartida (Open/Closed).
 */
public abstract class BusquedaExternaTransformacion {

    /**
     * @return la función hash que caracteriza esta búsqueda externa concreta.
     */
    public abstract FuncionHash getFuncionHash();

    /**
     * @return nombre único con el que se registra la búsqueda ("HASH MOD",
     *         "HASH CUADRADO", "HASH TRUNCAMIENTO", "HASH PLEGAMIENTO").
     */
    public abstract String getNombre();

    /**
     * Ejecuta la búsqueda directa por transformación de claves sobre las
     * cubetas: transforma la clave, calcula la cubeta y la revisa completa.
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
        FuncionHash funcion = getFuncionHash();

        // --- PASO 1: transformación de la clave ---------------------------------
        int direccion = funcion.calcularDireccion(claveBuscada, totalCubetas);

        // --- PASO 2: dirección 1-based -> cubeta base 0 --------------------------
        int cubeta = direccion - 1;

        numeroPaso++;
        pasos.add(new PasoBusqueda(numeroPaso, cubeta, 0, totalCubetas - 1,
                claveBuscada, funcion.describirCalculo(claveBuscada, totalCubetas)
                        + ". Como la estructura externa usa cubetas base 0, se "
                        + "accede directamente a la CUBETA " + cubeta
                        + " (direccion " + direccion + " - 1)."));

        // --- PASO 3: acceso directo y revisión de la cubeta completa ---------------
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
                                + " (principal o enlazada) contiene " + dato
                                + ", igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, cubeta, pasos);
            }
            pasos.add(new PasoBusqueda(numeroPaso, cubeta, 0, totalCubetas - 1,
                    dato, "La cubeta " + cubeta + " contiene la clave " + dato
                            + " (principal o enlazada) y NO es la buscada."));
        }

        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada + " NO existe: su cubeta " + cubeta
                        + " se revisó completa (principal y enlazadas) sin hallarla.",
                pasos);
    }
}