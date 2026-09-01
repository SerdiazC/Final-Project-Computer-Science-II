package Controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.busquedas.ResultadoBusqueda;
import Model.busquedas.externas.BusquedaExternaBinaria;
import Model.busquedas.externas.BusquedaExternaHashMod;
import Model.busquedas.externas.BusquedaExternaLineal;
import Model.estructuras.externas.EstructuraCubetas;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * GESTOR DE BÚSQUEDAS EXTERNAS (CONTROLADOR / FACHADA)
 * ============================================================================
 *
 * Punto único de contacto de la VISTA EXTERNA con la estructura de cubetas.
 * Administra UNA SOLA estructura externa dinámica y las operaciones que se
 * le aplican (insertar, eliminar, buscar, listar).
 *
 * REGLA EXTERNA: el usuario SOLO decide la cantidad de dígitos de las
 * claves; la dirección siempre la calcula el hash mod tradicional. El método
 * de búsqueda elegido (LINEAL, BINARIA o HASH MOD) solo dicta CÓMO se
 * recorre la estructura para hallar una clave, pero NO cambia dónde se
 * inserta.
 *
 * Este controlador es totalmente INDEPENDIENTE del GestorBusquedas interno:
 * no toca ni reutiliza la lógica de las búsquedas internas.
 */
public class GestorBusquedasExternas {

    /** Estructura externa dinámica de cubetas. */
    private EstructuraCubetas estructura;

    /** Dígitos configurables de las claves. */
    private int digitosClave;

    /** Método de búsqueda activo ("LINEAL", "BINARIA" o "HASH MOD"). */
    private String metodoActivo;

    /** Búsqueda lineal externa (sin estado). */
    private final BusquedaExternaLineal lineal = new BusquedaExternaLineal();

    /** Búsqueda binaria externa (sin estado). */
    private final BusquedaExternaBinaria binaria = new BusquedaExternaBinaria();

    /** Búsqueda hash mod externa (sin estado). */
    private final BusquedaExternaHashMod hashMod = new BusquedaExternaHashMod();

    /** Resultado de la última inserción: "directa" o "colision". */
    private String ultimoTipoInsercion;

    /** Cubeta protagonista de la última inserción (base 0). */
    private int ultimaCubetaInsercion;

    /**
     * Crea el gestor externo con la cifra de claves indicada y un número de
     * cubetas inicial acorde.
     *
     * @param digitosClave cifras exactas de las claves (1..7).
     * @throws ExcepcionEstructura si la cifra queda fuera de rango.
     */
    public GestorBusquedasExternas(int digitosClave) throws ExcepcionEstructura {
        configurar(digitosClave);
    }

    /** @return true si ya hay un método de búsqueda externa activo. */
    public boolean isSeleccionHecha() {
        return metodoActivo != null;
    }

    /**
     * (Re)configura la cantidad de dígitos, creando UNA estructura externa
     * nueva y vacía con el número de cubetas inicial acorde al método.
     * No requiere un método seleccionado.
     *
     * @param digitos cifras exactas (1..7).
     * @throws ExcepcionEstructura si la cifra queda fuera de rango.
     */
    public void configurar(int digitos) throws ExcepcionEstructura {
        int cubetasIniciales = (digitos == 1) ? 4 : digitos * 5;
        configurar(digitos, cubetasIniciales);
    }

    /**
     * (Re)configura la cantidad de dígitos Y el número inicial de cubetas,
     * creando UNA estructura externa nueva y vacía. El usuario elige cuántas
     * cubetas quiere al empezar.
     *
     * @param digitos          cifras exactas (1..7).
     * @param cubetasIniciales cantidad de cubetas con la que se inicia.
     * @throws ExcepcionEstructura si la cifra o la cantidad de cubetas queda
     *                             fuera de rango.
     */
    public void configurar(int digitos, int cubetasIniciales)
            throws ExcepcionEstructura {
        this.estructura = new EstructuraCubetas(digitos, cubetasIniciales);
        this.digitosClave = digitos;
        this.metodoActivo = null;
        this.ultimoTipoInsercion = null;
    }

    /** @return nombres de las búsquedas externas disponibles. */
    public List<String> getNombresBusquedas() {
        return Collections.unmodifiableList(
                Arrays.asList(lineal.getNombre(), binaria.getNombre(),
                        hashMod.getNombre()));
    }

    /**
     * (Re)configura la cantidad de dígitos Y la ORGANIZACIÓN EN FILAS de las
     * cubetas: cubetas por fila × filas = total de cubetas. El usuario elige
     * cuántas cubetas van en cada fila y cuántas filas hay (ej. 4 cubetas por
     * fila y 2 filas → 8 cubetas). Esta estructura fila×columna es la que la
     * reducción dinámica usará para encoger filas más adelante.
     *
     * @param digitosClave    cifras exactas (1..7).
     * @param cubetasPorFila  cubetas que caben en cada fila (mínimo 2).
     * @param filas           número de filas de cubetas (mínimo 1).
     * @throws ExcepcionEstructura si la cifra o las cantidades quedan fuera de
     *                             rango permitido.
     */
    public void configurar(int digitosClave, int cubetasPorFila, int filas)
            throws ExcepcionEstructura {
        this.estructura = new EstructuraCubetas(
                digitosClave, cubetasPorFila, filas);
        this.digitosClave = digitosClave;
        this.metodoActivo = null;
        this.ultimoTipoInsercion = null;
    }

    /**
     * Selecciona la técnica de búsqueda externa que recorre la estructura.
     */
    public void seleccionar(String metodo) {
        if (metodo == null
                || !getNombresBusquedas().contains(metodo)) {
            throw new IllegalArgumentException(
                    "Búsqueda externa no registrada: " + metodo);
        }
        this.metodoActivo = metodo;
    }

    /** @return método externo activo, o null si no se ha elegido. */
    public String getMetodoActivo() {
        return metodoActivo;
    }

    /**
     * Inserta la clave en la estructura externa registrando si hubo colisión
     * (la cubeta destino ya tenía otra clave enlazada) o si fue directa.
     *
     * @param clave valor a insertar.
     * @throws ExcepcionEstructura si viola rango o unicidad.
     */
    public void insertarClave(int clave) throws ExcepcionEstructura {
        int cubeta = Math.abs(clave % estructura.getNumeroCubetas());
        boolean habiaDatos = estructura.consultarCubeta(cubeta).getCantidad() > 0;

        estructura.insertar(clave);

        this.ultimaCubetaInsercion = cubeta;
        this.ultimoTipoInsercion = habiaDatos ? "colision" : "directa";
    }

    /**
     * Elimina la clave de la estructura externa.
     *
     * @param clave valor a eliminar.
     * @throws ExcepcionEstructura si no existe o está vacía.
     */
    public void eliminarClave(int clave) throws ExcepcionEstructura {
        estructura.eliminar(clave);
    }

    /**
     * Ejecuta la búsqueda externa ACTIVA sobre la estructura.
     *
     * @param clave valor a buscar.
     * @return resultado con pasos y desenlace.
     */
    public ResultadoBusqueda buscar(int clave) {
        if (metodoActivo == null) {
            throw new IllegalStateException(
                    "Primero debe seleccionar un método de búsqueda externa.");
        }
        switch (metodoActivo) {
            case "BINARIA":
                return binaria.buscar(estructura, clave);
            case "HASH MOD":
                return hashMod.buscar(estructura, clave);
            case "LINEAL":
            default:
                return lineal.buscar(estructura, clave);
        }
    }

    /** @return la estructura externa activa. */
    public EstructuraCubetas getEstructura() {
        return estructura;
    }

    /** @return dígitos configurables actuales. */
    public int getDigitosClave() {
        return digitosClave;
    }

    /** @return tipo de la última inserción ("directa", "colision") o null. */
    public String getUltimoTipoInsercion() {
        return ultimoTipoInsercion;
    }

    /** @return cubeta protagonista de la última inserción (base 0). */
    public int getUltimaCubetaInsercion() {
        return ultimaCubetaInsercion;
    }
}
