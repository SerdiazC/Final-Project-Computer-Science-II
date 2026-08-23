package Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.EstructuraDeDatos;
import Model.busquedas.BusquedaBinaria;
import Model.busquedas.BusquedaHashCuadrado;
import Model.busquedas.BusquedaHashModulo;
import Model.busquedas.BusquedaHashTruncamiento;
import Model.busquedas.BusquedaLineal;
import Model.busquedas.EstrategiaBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.excepciones.ClaveInvalidaException;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * GESTOR DE BÚSQUEDAS (CONTROLADOR / FACHADA DEL SISTEMA)
 * ============================================================================
 *
 * Único punto de contacto entre la vista (consola hoy, web mañana) y el
 * modelo. Orquesta las CINCO estructuras del proyecto, todas con el mismo
 * tamaño elegido por el usuario:
 *
 *   SECUENCIAL          -> búsqueda LINEAL
 *   ORDENADA            -> búsqueda BINARIA
 *   HASH MOD            -> transformación de claves MÓDULO
 *   HASH CUADRADO       -> transformación de claves CUADRADO
 *   HASH TRUNCAMIENTO   -> transformación de claves TRUNCAMIENTO
 *
 * PRINCIPIOS SOLID AQUÍ VISIBLES:
 *
 *  - Responsabilidad Única: coordina; no implementa algoritmos.
 *  - Open/Closed: registrarEstrategia() permite sumar búsquedas nuevas sin
 *    tocar esta clase; la vista genera su menú a partir del catálogo.
 *  - Inversión de Dependencias: trabaja contra EstructuraDeDatos y
 *    EstrategiaBusqueda, nunca contra clases concretas.
 */
public class GestorBusquedas {

    /** Estructuras administradas, indexadas por su tipo (orden estable). */
    private final Map<String, EstructuraDeDatos> estructuras;

    /** Estrategias disponibles, indexadas por nombre (orden estable). */
    private final Map<String, EstrategiaBusqueda> estrategias;

    /**
     * Construye el gestor creando TODAS las estructuras con la configuración
     * elegida por el usuario y registrando las búsquedas disponibles.
     *
     * @param digitosClave dígitos exactos de las claves (1..7).
     * @param capacidad    tamaño exacto de cada estructura (1..1000).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public GestorBusquedas(int digitosClave, int capacidad) throws ExcepcionEstructura {
        this.estructuras = new LinkedHashMap<>();

        estructuras.put(EstructuraDeDatos.TIPO_SECUENCIAL,
                FabricaEstructuras.crear(FabricaEstructuras.TipoEstructura.SECUENCIAL,
                        digitosClave, capacidad));
        estructuras.put(EstructuraDeDatos.TIPO_ORDENADA,
                FabricaEstructuras.crear(FabricaEstructuras.TipoEstructura.ORDENADA,
                        digitosClave, capacidad));
        estructuras.put(EstructuraDeDatos.TIPO_HASH_MOD,
                FabricaEstructuras.crear(FabricaEstructuras.TipoEstructura.HASH_MOD,
                        digitosClave, capacidad));
        estructuras.put(EstructuraDeDatos.TIPO_HASH_CUADRADO,
                FabricaEstructuras.crear(FabricaEstructuras.TipoEstructura.HASH_CUADRADO,
                        digitosClave, capacidad));
        estructuras.put(EstructuraDeDatos.TIPO_HASH_TRUNCAMIENTO,
                FabricaEstructuras.crear(FabricaEstructuras.TipoEstructura.HASH_TRUNCAMIENTO,
                        digitosClave, capacidad));

        this.estrategias = new LinkedHashMap<>();
        registrarEstrategia(new BusquedaLineal());
        registrarEstrategia(new BusquedaBinaria());
        registrarEstrategia(new BusquedaHashModulo());
        registrarEstrategia(new BusquedaHashCuadrado());
        registrarEstrategia(new BusquedaHashTruncamiento());
    }

    // ========================================================================
    // REGISTRO DE ESTRATEGIAS (EXTENSIBILIDAD)
    // ========================================================================

    /**
     * Registra (o reemplaza) una estrategia de búsqueda. Punto único de
     * extensión: nuevas búsquedas entran al sistema por aquí.
     *
     * @param estrategia implementación de EstrategiaBusqueda a publicar.
     */
    public void registrarEstrategia(EstrategiaBusqueda estrategia) {
        estrategias.put(estrategia.getNombre(), estrategia);
    }

    /** @return nombres de las búsquedas disponibles, en orden de registro. */
    public List<String> getNombresBusquedas() {
        return Collections.unmodifiableList(new ArrayList<>(estrategias.keySet()));
    }

    // ========================================================================
    // OPERACIONES SOBRE LAS CLAVES
    // ========================================================================

    /**
     * Inserta la clave en TODAS las estructuras en dos fases para que la
     * operación sea ATÓMICA:
     *
     *   FASE DE RESERVA - cada estructura valida si podría recibirla
     *                     (rango, espacio, duplicado o colisión hash) sin
     *                     modificar nada.
     *   FASE DE APLICACIÓN - solo si todas aceptaron, se inserta en cada una.
     *
     * Así nunca quedan estructuras desincronizadas: o la clave vive en las
     * cinco o en ninguna.
     *
     * @param clave valor numérico ingresado por el usuario.
     * @throws ExcepcionEstructura si alguna estructura rechaza la clave.
     */
    public void insertarClave(int clave) throws ExcepcionEstructura {

        // --- FASE DE RESERVA: validación global sin mutaciones -----------------
        for (EstructuraDeDatos estructura : estructuras.values()) {
            estructura.verificarPuedeInsertar(clave);
        }

        // --- FASE DE APLICACIÓN: inserción garantizada --------------------------
        for (EstructuraDeDatos estructura : estructuras.values()) {
            estructura.insertar(clave);
        }
    }

    /**
     * Elimina la clave de TODAS las estructuras (las contiguas se compactan;
     * las dispersas dejan su dirección vacía).
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si no hay datos almacenados.
     * @throws ClaveNoEncontradaException si la clave no existe.
     */
    public void eliminarClave(int clave)
            throws EstructuraVaciaException, ClaveNoEncontradaException {
        for (EstructuraDeDatos estructura : estructuras.values()) {
            estructura.eliminar(clave);
        }
    }

    /**
     * Ejecuta la búsqueda indicada por su nombre. El gestor entrega a la
     * estrategia la estructura que ella declara requerir.
     *
     * @param nombreBusqueda nombre registrado ("LINEAL", "BINARIA",
     *                       "HASH MOD", "HASH CUADRADO", "HASH TRUNCAMIENTO").
     * @param claveBuscada   clave solicitada por el usuario.
     * @return resultado con desenlace y pasos visualizables.
     * @throws IllegalArgumentException si el nombre no está registrado.
     */
    public ResultadoBusqueda buscar(String nombreBusqueda, int claveBuscada) {
        EstrategiaBusqueda estrategia = estrategias.get(nombreBusqueda);
        if (estrategia == null) {
            throw new IllegalArgumentException(
                    "Búsqueda no registrada: " + nombreBusqueda);
        }
        EstructuraDeDatos objetivo = estructuras.get(estrategia.getEstructuraRequerida());
        return estrategia.buscar(objetivo, claveBuscada);
    }

    // ========================================================================
    // CONSULTA DE ESTADO PARA LA VISTA
    // ========================================================================

    /**
     * @return vista de solo lectura del catálogo completo de estructuras
     *         (nombre técnico -> estructura), en su orden de creación.
     */
    public Map<String, EstructuraDeDatos> getEstructuras() {
        return Collections.unmodifiableMap(estructuras);
    }

    /**
     * @return la primera estructura creada (la secuencial); útil para que
     *         la vista consulte la configuración común (dígitos y rangos).
     */
    public EstructuraDeDatos getEstructuraReferencia() {
        return estructuras.get(EstructuraDeDatos.TIPO_SECUENCIAL);
    }

    /**
     * @return copia defensiva de las claves de la estructura solicitada,
     *         identificada por su tipo (constantes TIPO_*).
     */
    public int[] obtenerClavesDe(String tipoEstructura) {
        EstructuraDeDatos estructura = estructuras.get(tipoEstructura);
        if (estructura == null) {
            throw new IllegalArgumentException(
                    "Tipo de estructura desconocido: " + tipoEstructura);
        }
        return estructura.obtenerClaves();
    }
}
