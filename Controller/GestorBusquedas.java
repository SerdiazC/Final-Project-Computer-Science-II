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
import Model.busquedas.BusquedaHashPlegamiento;
import Model.busquedas.BusquedaHashTruncamiento;
import Model.busquedas.BusquedaLineal;
import Model.busquedas.BusquedaResiduosDigital;
import Model.busquedas.EstrategiaBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.colisiones.ArregloAnidado;
import Model.colisiones.DobleDispersion;
import Model.colisiones.EncadenamientoSeparado;
import Model.colisiones.PruebaCuadratica;
import Model.colisiones.PruebaLineal;
import Model.colisiones.SolucionColision;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;
import Model.transformaciones.FuncionHashCuadrado;
import Model.transformaciones.FuncionHashModulo;
import Model.transformaciones.FuncionHashPlegamiento;
import Model.transformaciones.FuncionHashTruncamiento;

/**
 * ============================================================================
 * GESTOR DE BÚSQUEDAS (CONTROLADOR / FACHADA DEL SISTEMA)
 * ============================================================================
 *
 * Único punto de contacto entre la vista (consola hoy, web mañana) y el
 * modelo. Administra UNA SOLA estructura ACTIVA: el usuario configura los
 * parámetros, elige el método de búsqueda y todas las operaciones
 * (insertar, listar, buscar, eliminar) se aplican a esa estructura.
 *
 * CAMBIO DE BÚSQUEDA: al elegir otro método se construye su estructura y,
 * si el usuario lo pide, las claves actuales MIGRAN a ella en su orden de
 * almacenamiento; las que la nueva familia rechace (colisión hash o espacio
 * agotado) se reportan. Si no se conservan, la estructura inicia vacía.
 *
 * FAMILIAS DISPONIBLES:
 *
 *   SECUENCIAL           -> búsqueda LINEAL
 *   ORDENADA             -> búsqueda BINARIA
 *   HASH MOD             -> transformación MÓDULO
 *   HASH CUADRADO        -> transformación CUADRADO
 *   HASH TRUNCAMIENTO    -> transformación TRUNCAMIENTO
 *   HASH PLEGAMIENTO     -> transformación PLEGAMIENTO
 *   RESIDUOS DIGITAL     -> árbol binario guiado por los bits de la clave
 *                          (crecimiento libre)
 *
 * PRINCIPIOS SOLID AQUÍ VISIBLES:
 *
 *  - Responsabilidad Única: coordina; no implementa algoritmos.
 *  - Open/Closed: registrarEstrategia() permite sumar búsquedas nuevas sin
 *    tocar esta clase; FabricaEstructuras suma familias sin tocar el gestor.
 *  - Inversión de Dependencias: trabaja contra EstructuraDeDatos y
 *    EstrategiaBusqueda, nunca contra clases concretas.
 */
public class GestorBusquedas {

    /** Estrategias disponibles, indexadas por nombre (orden estable). */
    private final Map<String, EstrategiaBusqueda> estrategias;

    /**
     * Soluciones de colisión disponibles, indexadas por nombre (orden
     * estable). Son objetos SIN ESTADO: una sola instancia de cada una
     * sirve para todas las estructuras.
     */
    private final Map<String, SolucionColision> soluciones;

    /**
     * Funciones hash disponibles para INSERTAR las claves, indexadas por
     * nombre (orden estable: lineal, cuadrado, truncamiento, plegamiento).
     * Objetos sin estado: instancias únicas compartidas.
     */
    private final Map<String, FuncionHash> funciones;

    /** Dígitos exactos exigidos a las claves (1..7). */
    private final int digitosClave;

    /** Tamaño configurado para las estructuras acotadas (1..1000). */
    private final int tamanoEstructura;

    /** Estructura sobre la que operan actualmente TODAS las operaciones. */
    private EstructuraDeDatos estructuraActiva;

    /** Método de búsqueda activo seleccionado por el usuario. */
    private EstrategiaBusqueda busquedaActiva;

    /** Nombre de la solución de colisión configurada en la estructura activa. */
    private String nombreSolucionActiva;

    /**
     * Función hash con la que se INSERTAN las claves en la estructura
     * activa. Se fija al seleccionar el método y SOLO cambia reiniciando
     * la estructura (regla "inmodificable una vez aplicada"). Es null
     * para el árbol digital, que niega la función.
     */
    private FuncionHash funcionHashActiva;

    /**
     * Construye el gestor validando la configuración y registrando el
     * catálogo de búsquedas. Aún NO hay estructura activa: el usuario debe
     * seleccionar un método con {@link #seleccionarBusqueda}.
     *
     * @param digitosClave dígitos exactos de las claves (1..7).
     * @param capacidad    tamaño exacto de las estructuras acotadas (1..1000).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public GestorBusquedas(int digitosClave, int capacidad)
            throws ExcepcionEstructura {

        // Validación temprana: cualquier creación descartable falla aquí.
        FabricaEstructuras.crear(FabricaEstructuras.TipoEstructura.SECUENCIAL,
                digitosClave, capacidad, new FuncionHashModulo(),
                new PruebaLineal());

        this.digitosClave = digitosClave;
        this.tamanoEstructura = capacidad;

        this.estrategias = new LinkedHashMap<>();
        registrarEstrategia(new BusquedaLineal());
        registrarEstrategia(new BusquedaBinaria());
        registrarEstrategia(new BusquedaHashModulo());
        registrarEstrategia(new BusquedaHashCuadrado());
        registrarEstrategia(new BusquedaHashTruncamiento());
        registrarEstrategia(new BusquedaHashPlegamiento());
        registrarEstrategia(new BusquedaResiduosDigital());

        // Soluciones de colisión (sin estado: instancias únicas compartidas).
        this.soluciones = new LinkedHashMap<>();
        soluciones.put(PruebaLineal.NOMBRE, new PruebaLineal());
        soluciones.put(PruebaCuadratica.NOMBRE, new PruebaCuadratica());
        soluciones.put(DobleDispersion.NOMBRE, new DobleDispersion());
        soluciones.put(EncadenamientoSeparado.NOMBRE, new EncadenamientoSeparado());
        soluciones.put(ArregloAnidado.NOMBRE, new ArregloAnidado());

        // Funciones hash para insertar (sin estado: instancias únicas).
        this.funciones = new LinkedHashMap<>();
        funciones.put(FuncionHashModulo.NOMBRE, new FuncionHashModulo());
        funciones.put(FuncionHashCuadrado.NOMBRE, new FuncionHashCuadrado());
        funciones.put(FuncionHashTruncamiento.NOMBRE, new FuncionHashTruncamiento());
        funciones.put(FuncionHashPlegamiento.NOMBRE, new FuncionHashPlegamiento());
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
    // SELECCIÓN DEL MÉTODO DE BÚSQUEDA (CON MIGRACIÓN OPCIONAL DE DATOS)
    // ========================================================================

    /**
     * Activa el método indicado creando SU estructura correspondiente: la
     * clave se insertará SIEMPRE con la función hash elegida (regla del
     * proyecto para TODAS las búsquedas) y las colisiones se resolverán con
     * la solución seleccionada:
     *
     *   FASE 1 - Localizar la estrategia, la función y la solución.
     *   FASE 2 - Capturar las claves actuales SOLO si se pidió conservarlas.
     *   FASE 3 - Construir la estructura nueva del método elegido.
     *   FASE 4 - Migrar las claves en su orden original; las rechazadas
     *            (sin espacio o cubeta llena) se devuelven para informar.
     *
     * La función hash queda FIJADA aquí: después solo cambia reiniciando la
     * estructura (inmodificable una vez aplicada).
     *
     * @param nombreBusqueda nombre registrado ("LINEAL", "BINARIA", ...).
     * @param funcion        función hash que inserta las claves (null solo
     *                       permitido en RESIDUOS DIGITAL, que la ignora).
     * @param nombreSolucion solución de colisión elegida (se registra para
     *                       cualquier familia; solo las hash la aplican).
     * @param mantenerDatos  true para migrar las claves existentes; false
     *                       para iniciar con la estructura vacía.
     * @return claves que NO pudieron migrarse (vacía si todas pasaron).
     * @throws IllegalArgumentException si algún nombre no está registrado
     *                                  o falta la función exigida.
     * @throws ExcepcionEstructura      si la configuración ya no es válida.
     */
    public List<Integer> seleccionarBusqueda(String nombreBusqueda,
            FuncionHash funcion, String nombreSolucion, boolean mantenerDatos)
            throws ExcepcionEstructura {

        // --- FASE 1: localizar estrategia, función y solución ------------------
        EstrategiaBusqueda estrategia = estrategias.get(nombreBusqueda);
        if (estrategia == null) {
            throw new IllegalArgumentException(
                    "Búsqueda no registrada: " + nombreBusqueda);
        }
        if (funcion == null
                && !EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL
                        .equals(estrategia.getEstructuraRequerida())) {
            throw new IllegalArgumentException(
                    "Debe elegir la función hash con la que se insertan las "
                            + "claves.");
        }
        SolucionColision solucion = soluciones.get(nombreSolucion);
        if (solucion == null) {
            throw new IllegalArgumentException(
                    "Solución de colisión no registrada: " + nombreSolucion);
        }

        // --- FASE 2: capturar datos previos si se quieren conservar -------------
        int[] previas = new int[0];
        if (mantenerDatos && estructuraActiva != null) {
            previas = estructuraActiva.obtenerClaves();
        }

        // --- FASE 3: construir la estructura del nuevo método --------------------
        EstructuraDeDatos nueva = FabricaEstructuras.crear(
                FabricaEstructuras.tipoDe(estrategia.getEstructuraRequerida()),
                digitosClave, tamanoEstructura, funcion, solucion);

        // --- FASE 4: migrar en el orden original ---------------------------------
        List<Integer> rechazadas = new ArrayList<>();
        for (int clave : previas) {
            try {
                nueva.insertar(clave);
            } catch (ExcepcionEstructura e) {
                rechazadas.add(clave);
            }
        }

        this.estructuraActiva = nueva;
        this.busquedaActiva = estrategia;
        this.nombreSolucionActiva = nombreSolucion;
        this.funcionHashActiva = funcion;
        return rechazadas;
    }

    // ========================================================================
    // CAMBIO DE SOLUCIÓN DE COLISIÓN (SOLO REINICIANDO LA ESTRUCTURA)
    // ========================================================================

    /**
     * Cambia la solución de colisiones de la estructura activa. REGLA DEL
     * PROYECTO: la única manera de cambiarla es REINICIAR la estructura,
     * así que este método crea una NUEVA y VACÍA del mismo tipo de búsqueda
     * activo; los datos NO se conservan.
     *
     * @param nombreSolucion nombre de la solución deseada.
     * @throws IllegalArgumentException si el nombre no está registrado o no
     *                                  hay método de búsqueda activo.
     */
    public void cambiarSolucion(String nombreSolucion) throws ExcepcionEstructura {
        requerirSeleccion();

        SolucionColision solucion = soluciones.get(nombreSolucion);
        if (solucion == null) {
            throw new IllegalArgumentException(
                    "Solución de colisión no registrada: " + nombreSolucion);
        }

        EstructuraDeDatos reiniciada = FabricaEstructuras.crear(
                FabricaEstructuras.tipoDe(busquedaActiva.getEstructuraRequerida()),
                digitosClave, tamanoEstructura, funcionHashActiva, solucion);

        this.estructuraActiva = reiniciada;
        this.nombreSolucionActiva = nombreSolucion;
    }

    // ========================================================================
    // OPERACIONES SOBRE LAS CLAVES (DELEGADAS EN LA ESTRUCTURA ACTIVA)
    // ========================================================================

    /**
     * Inserta la clave en la estructura activa aplicando sus reglas propias
     * (rango exacto, unicidad, espacio o colisión según la familia).
     *
     * @param clave valor numérico ingresado por el usuario.
     * @throws ExcepcionEstructura si la estructura activa rechaza la clave.
     * @throws IllegalStateException si aún no hay método seleccionado.
     */
    public void insertarClave(int clave) throws ExcepcionEstructura {
        requerirSeleccion();
        estructuraActiva.insertar(clave);
    }

    /**
     * Elimina la clave de la estructura activa (contiguas compactan; hash
     * deja su dirección vacía; el árbol vacía el nodo conservando hijos).
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si no hay datos almacenados.
     * @throws ClaveNoEncontradaException si la clave no existe.
     * @throws IllegalStateException      si aún no hay método seleccionado.
     */
    public void eliminarClave(int clave)
            throws EstructuraVaciaException, ClaveNoEncontradaException {
        requerirSeleccion();
        estructuraActiva.eliminar(clave);
    }

    /**
     * Ejecuta la búsqueda ACTIVA sobre la estructura ACTIVA, produciendo
     * el resultado con todos sus pasos visualizables.
     *
     * @param claveBuscada clave solicitada por el usuario.
     * @return resultado con desenlace y pasos para animar en la interfaz.
     * @throws IllegalStateException si aún no hay método seleccionado.
     */
    public ResultadoBusqueda buscar(int claveBuscada) {
        requerirSeleccion();
        return busquedaActiva.buscar(estructuraActiva, claveBuscada);
    }

    /**
     * Salvaguarda interna: ninguna operación es válida antes de elegir
     * método de búsqueda.
     */
    private void requerirSeleccion() {
        if (busquedaActiva == null || estructuraActiva == null) {
            throw new IllegalStateException(
                    "Primero debe seleccionar un método de búsqueda.");
        }
    }

    // ========================================================================
    // EXPORTACIÓN / CARGA DE ESTRUCTURAS
    // ========================================================================

    /**
     * Entrega copia densa de las claves de la estructura activa en su orden
     * interno natural. Es la base del "exportar estructura" de la web: la
     * vista empaqueta estas claves junto a la configuración vigente.
     *
     * @return claves ocupadas (vacía si la estructura no está seleccionada).
     * @throws IllegalStateException si aún no hay método seleccionado.
     */
    public int[] obtenerClavesActivas() {
        requerirSeleccion();
        return estructuraActiva.obtenerClaves();
    }

    /**
     * REEMPLAZA el contenido de la estructura activa con las claves dadas,
     * dejando intactos el método y la solución de colisiones vigentes:
     *
     *   FASE 1 - Construir la estructura del método activo, nueva y vacía.
     *   FASE 2 - Insertar cada clave en su orden; las que la familia rechace
     *            (fuera de rango, duplicadas o sin espacio) se reportan.
     *
     * Así el usuario puede "usar la misma estructura en diferentes tipos de
     * búsqueda": exporta desde un método, entra a otro, y carga las claves
     * aquí reconstruidas con la técnica del método actual.
     *
     * @param claves lista de claves a cargar (puede estar vacía).
     * @return claves que NO pudieron insertarse.
     * @throws IllegalStateException si aún no hay método seleccionado.
     * @throws ExcepcionEstructura   si la configuración ya no es válida.
     */
    public List<Integer> cargarClavesEnActiva(List<Integer> claves)
            throws ExcepcionEstructura {
        requerirSeleccion();

        SolucionColision solucion = soluciones.get(nombreSolucionActiva);
        EstructuraDeDatos reconstruida = FabricaEstructuras.crear(
                FabricaEstructuras.tipoDe(busquedaActiva.getEstructuraRequerida()),
                digitosClave, tamanoEstructura, funcionHashActiva, solucion);

        List<Integer> rechazadas = new ArrayList<>();
        for (int clave : claves) {
            try {
                reconstruida.insertar(clave);
            } catch (ExcepcionEstructura e) {
                rechazadas.add(clave);
            }
        }

        this.estructuraActiva = reconstruida;
        return rechazadas;
    }

    // ========================================================================
    // CONSULTA DE ESTADO PARA LA VISTA
    // ========================================================================

    /** @return true si ya hay un método de búsqueda activo. */
    public boolean isSeleccionHecha() {
        return busquedaActiva != null && estructuraActiva != null;
    }

    /** @return nombre del método activo, o null si no se ha elegido. */
    public String getNombreBusquedaActiva() {
        return (busquedaActiva == null) ? null : busquedaActiva.getNombre();
    }

    /** @return tipo técnico de la estructura activa, o null si no hay. */
    public String getTipoEstructuraActiva() {
        return (estructuraActiva == null) ? null : estructuraActiva.getTipo();
    }

    /**
     * @return la estructura activa para consultas de solo lectura
     *         (listados, rangos válidos, árbol, etc.), o null si no hay.
     */
    public EstructuraDeDatos getEstructuraActiva() {
        return estructuraActiva;
    }

    /**
     * @return tipo técnico de la estructura que usa la búsqueda indicada
     *         (para etiquetar el menú de selección).
     */
    public String getEstructuraDe(String nombreBusqueda) {
        EstrategiaBusqueda estrategia = estrategias.get(nombreBusqueda);
        return (estrategia == null) ? null : estrategia.getEstructuraRequerida();
    }

    /** @return nombres de las soluciones de colisión, en orden de registro. */
    public List<String> getNombresSoluciones() {
        return Collections.unmodifiableList(new ArrayList<>(soluciones.keySet()));
    }

    /** @return nombre de la solución configurada en la estructura activa. */
    public String getNombreSolucionActiva() {
        return nombreSolucionActiva;
    }

    /** @return nombres de las funciones hash para insertar, en orden (4). */
    public List<String> getNombresFunciones() {
        return Collections.unmodifiableList(new ArrayList<>(funciones.keySet()));
    }

    /**
     * @param nombre nombre de la función hash ("HASH MOD", "HASH CUADRADO",
     *               "HASH TRUNCAMIENTO", "HASH PLEGAMIENTO").
     * @return instancia de la función, o null si no está registrada.
     */
    public FuncionHash getFuncionDe(String nombre) {
        return funciones.get(nombre);
    }

    /** @return nombre de la función hash activa, o null si no hay o es árbol. */
    public String getNombreFuncionActiva() {
        return (funcionHashActiva == null) ? null : funcionHashActiva.getNombre();
    }

    /**
     * Indica si la familia del método indicado APLICA soluciones de
     * colisión. Con la regla vigente TODAS las familias de arreglo
     * (SECUENCIAL, ORDENADA y las cuatro HASH) insertan por función hash y
     * pueden colisionar, así que todas la usan; solo el árbol digital
     * resuelve sus puntos ocupados con su regla nativa (siguiente bit).
     *
     * @param tipoEstructura tipo técnico de la estructura.
     * @return true si la familia usa las soluciones de colisión.
     */
    public static boolean familiaUsaColisiones(String tipoEstructura) {
        return EstructuraDeDatos.TIPO_SECUENCIAL.equals(tipoEstructura)
                || EstructuraDeDatos.TIPO_ORDENADA.equals(tipoEstructura)
                || EstructuraDeDatos.TIPO_HASH_MOD.equals(tipoEstructura)
                || EstructuraDeDatos.TIPO_HASH_CUADRADO.equals(tipoEstructura)
                || EstructuraDeDatos.TIPO_HASH_TRUNCAMIENTO.equals(tipoEstructura)
                || EstructuraDeDatos.TIPO_HASH_PLEGAMIENTO.equals(tipoEstructura);
    }

    /** @return dígitos exactos exigidos a las claves (1..7). */
    public int getDigitosClave() {
        return digitosClave;
    }

    /** @return tamaño configurado para estructuras acotadas (1..1000). */
    public int getTamanoEstructura() {
        return tamanoEstructura;
    }
}
