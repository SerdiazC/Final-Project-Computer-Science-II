package Model.estructuras.externas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Model.excepciones.ClaveDuplicadaException;
import Model.excepciones.ClaveInvalidaException;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA DE CUBETAS - BÚSQUEDA EXTERNA (HASHING DINÁMICO)
 * ============================================================================
 *
 * Familia de búsqueda EXTERNA: los datos no viven en un arreglo de tamaño
 * fijo sino en un conjunto de CUBETAS que crece y encoge dinámicamente
 * según la carga, como si cada cubeta fuera una página de memoria
 * secundaria cargada bajo demanda.
 *
 * REGLA DEL PROYECTO (EXTERNA): el usuario SOLO decide la cantidad de
 * dígitos de la clave. La DIRECCIÓN siempre se calcula con el HASH MOD
 * tradicional sin el +1 del cierre interno:
 *
 *      cubeta = | clave % numeroDeCubetas |
 *
 * (base 0, para que la cubeta 0 sea la primera; se toma valor absoluto por
 * si acaso el operador módulo de Java devolviera un negativo).
 *
 * CRECIMIENTO DINÁMICO:
 *   - Si una cubeta se LLENA al insertar, primero se hace una EXPANSIÓN
 *     PARCIAL: esa única cubeta duplica su capacidad y se reintenta.
 *   - Luego se verifica la densidad global; si supera el límite de
 *     expansión (0.75) se hace una EXPANSIÓN TOTAL: se duplica el número
 *     de cubetas y se RE-CALCULA el hash de todas las claves (rehash).
 *     Por eso una colisión "enlazada" en una cubeta tiende a resolverse
 *     en la siguiente expansión: las claves se reparten entre más cubetas.
 *   - Al eliminar se verifica la reducción: si la densidad por cubeta baja,
 *     se pueden encoger cubetas o reducir el número de cubetas.
 *
 * COLISIONES: cuando dos claves caen en la misma cubeta, AMBAS conviven en
 * ella (quedan "enlazadas"). La estructura REGISTRA cada colisión y cada
 * expansión para que la interfaz las muestre visualmente.
 */
public class EstructuraCubetas {

    /** Límite de densidad que dispara una expansión total. */
    public static final double LIMITE_EXPANSION = 0.75;

    /** Límite de densidad que dispara una reducción. */
    public static final double LIMITE_REDUCCION = 0.35;

    /** Cubetas de la estructura (cada una es una "página"). */
    private Cubeta[] cubetas;

    /** Cantidad total de claves almacenadas (incluye las enlazadas). */
    private int cantidadRegistros;

    /** Dígitos exactos exigidos a las claves. */
    private final int digitosClave;

    /** Número de cubetas (visible para la interfaz). */
    private int numeroCubetas;

    /** Filas en que se organizan las cubetas (para visualización/reducción). */
    private int filas;

    /** Cubetas que hay en cada fila (número de columnas). */
    private int cubetasPorFila;

    /** Última expansión total registrada (para la visualización). */
    private int ultimaExpansionTotal;

    /** Última expansión parcial registrada (cubeta índice base 0). */
    private int ultimaExpansionParcial = -1;

    /**
     * Crea la estructura externa con la cifra de claves indicada y el número
     * inicial de cubetas pedido (una sola fila).
     *
     * @param digitosClave cifras exactas de las claves (1..7).
     * @param cubetasIniciales número inicial de cubetas (mínimo 2).
     * @throws ExcepcionEstructura si la cifra queda fuera de rango.
     */
    public EstructuraCubetas(int digitosClave, int cubetasIniciales)
            throws ExcepcionEstructura {
        this(digitosClave, Math.min(cubetasIniciales, 64), 1);
    }

    /**
     * Crea la estructura externa organizando las cubetas en FILAS: cubetas
     * por fila × número de filas = total de cubetas. Ejemplo: 4 cubetas por
     * fila y 2 filas → 8 cubetas. Esta forma de organizar es la que usará la
     * reducción dinámica cuando haya que encoger filas.
     *
     * @param digitosClave   cifras exactas de las claves (1..7).
     * @param cubetasPorFila cubetas que caben en cada fila (mínimo 2).
     * @param filas          número de filas de cubetas (mínimo 1).
     * @throws ExcepcionEstructura si la cifra queda fuera de rango.
     */
    public EstructuraCubetas(int digitosClave, int cubetasPorFila, int filas)
            throws ExcepcionEstructura {
        if (digitosClave < 1 || digitosClave > 7) {
            throw new ClaveInvalidaException(
                    "La cantidad de dígitos debe estar entre 1 y 7, se recibió: "
                            + digitosClave);
        }
        this.digitosClave = digitosClave;
        int total = Math.max(2,
                Math.min(Math.max(2, cubetasPorFila) * Math.max(1, filas), 64));
        this.filas = Math.max(1, Math.min(Math.max(1, filas), total));
        this.cubetasPorFila = total / this.filas;
        this.numeroCubetas = total;
        this.cubetas = new Cubeta[total];
        crearCubetas(total);
        this.cantidadRegistros = 0;
        this.ultimaExpansionTotal = 0;
        this.ultimaExpansionParcial = -1;
    }

    private void crearCubetas(int cantidad) {
        cubetas = new Cubeta[cantidad];
        for (int i = 0; i < cantidad; i++) {
            cubetas[i] = new Cubeta();
        }
    }

    // ========================================================================
    // HASH MOD TRADICIONAL (SIN +1, BASE 0)
    // ========================================================================

    /**
     * Calcula la cubeta destino mediante el hash mod tradicional (base 0).
     *
     * @param dato valor a transformar.
     * @return índice base 0 de la cubeta.
     */
    private int obtenerPosicion(int dato) {
        int posicion = dato % cubetas.length;
        if (posicion < 0) {
            posicion = posicion * -1;
        }
        return posicion;
    }

    // ========================================================================
    // INSERCIÓN (CON EXPANSIÓN DINÁMICA)
    // ========================================================================

    /**
     * Inserta una clave aplicando la expansión dinámica:
     *
     *   FASE 1 - Validar rango y unicidad.
     *   FASE 2 - Calcular la cubeta con hash mod.
     *   FASE 3 - Guardar la clave: si la cubeta ya tiene su elemento principal,
     *            la clave se ENLAZA (colisión) en memoria encadenada.
     *   FASE 4 - Verificar la ocupación; si alcanza el límite (75%), EXPANSIÓN
     *            TOTAL (duplicar cubetas + rehash con el nuevo total).
     *
     * @param clave valor a almacenar.
     * @throws ExcepcionEstructura si viola rango o unicidad.
     */
    public void insertar(int clave) throws ExcepcionEstructura {
        validar(clave);

        int posicion = obtenerPosicion(clave);

        cubetas[posicion].insertar(clave);

        cantidadRegistros++;
        verificarExpansion();
    }

    private void validar(int clave) throws ExcepcionEstructura {
        long minimo = (digitosClave == 1) ? 0 : (long) Math.pow(10, digitosClave - 1);
        long maximo = (long) Math.pow(10, digitosClave) - 1;
        if (clave < minimo || clave > maximo) {
            throw new ClaveInvalidaException(
                    "La clave " + clave + " no tiene " + digitosClave
                            + " dígito(s). Debe estar entre " + minimo + " y "
                            + maximo + ".");
        }
        if (contieneClave(clave)) {
            throw new ClaveDuplicadaException(
                    "La clave " + clave + " ya existe en la estructura; "
                            + "no se permiten duplicados.");
        }
    }

    /**
     * Verifica la densidad global: si alcanza el límite, duplica el número
     * de cubetas y vuelve a disponer TODAS las claves (rehash). La nueva
     * distribución suele eliminar colisiones previas.
     */
    private void verificarExpansion() {
        double densidad = densidadExpansion();
        if (densidad >= LIMITE_EXPANSION) {
            expansionTotal();
        }
    }

    private void expansionTotal() {
        int[] registros = obtenerClavesPlanas();
        int nuevaCantidad = cubetas.length * 2;
        numeroCubetas = nuevaCantidad;
        crearCubetas(nuevaCantidad);
        // Mantiene la organizacion en filas: las mismas filas y mas columnas.
        cubetasPorFila = nuevaCantidad / filas;
        cantidadRegistros = 0;
        for (int r : registros) {
            int pos = obtenerPosicion(r);
            cubetas[pos].insertar(r);
            cantidadRegistros++;
        }
        ultimaExpansionTotal = nuevaCantidad;
        ultimaExpansionParcial = -1;
    }

    // ========================================================================
    // ELIMINACIÓN (CON REDUCCIÓN DINÁMICA)
    // ========================================================================

    /**
     * Elimina la clave de su cubeta y verifica la reducción.
     *
     * @param clave valor a eliminar.
     * @throws ClaveNoEncontradaException si no existe.
     * @throws ExcepcionEstructura si la estructura no tiene clave.
     */
    public void eliminar(int clave) throws ExcepcionEstructura {
        if (cantidadRegistros == 0) {
            throw new ClaveNoEncontradaException(
                    "No hay claves para eliminar: la estructura está vacía.");
        }
        int posicion = obtenerPosicion(clave);
        if (!cubetas[posicion].eliminar(clave)) {
            // Redistribución: la clave pudo vivir en la cubeta de su
            // dirección actual; si no está, se recorre todo.
            boolean hallada = false;
            for (int i = 0; i < cubetas.length && !hallada; i++) {
                if (cubetas[i].contiene(clave)) {
                    cubetas[i].eliminar(clave);
                    hallada = true;
                }
            }
            if (!hallada) {
                throw new ClaveNoEncontradaException(
                        "No se puede eliminar: la clave " + clave
                                + " no existe en la estructura.");
            }
        }
        cantidadRegistros--;
        verificarReduccion();
    }

    private void verificarReduccion() {
        if (cantidadRegistros > 0 && cubetas.length > 2) {
            int nuevaCantidad = Math.max(2, cubetas.length / 2);
            double densidadTrasReduc
                    = (double) cantidadRegistros / nuevaCantidad;
            if (densidadTrasReduc <= 1.05) {
                // Simula la rehash para contar cuántas cubetas quedarían
                // ocupadas; si ≥ 75 % se dispararía expansión de vuelta.
                Set<Integer> ocupadasSimuladas = new HashSet<>();
                for (int i = 0; i < cubetas.length; i++) {
                    for (int clave : cubetas[i].getDatos()) {
                        int pos = Math.abs(clave % nuevaCantidad);
                        ocupadasSimuladas.add(pos);
                    }
                }
                double densidadOcupadas
                        = (double) ocupadasSimuladas.size() / nuevaCantidad;
                if (densidadOcupadas < LIMITE_EXPANSION) {
                    int[] registros = obtenerClavesPlanas();
                    numeroCubetas = nuevaCantidad;
                    while (filas > 1 && nuevaCantidad % filas != 0) {
                        filas--;
                    }
                    cubetasPorFila = nuevaCantidad / filas;
                    crearCubetas(nuevaCantidad);
                    cantidadRegistros = 0;
                    for (int r : registros) {
                        cubetas[obtenerPosicion(r)].insertar(r);
                        cantidadRegistros++;
                    }
                }
            }
        }
    }

    // ========================================================================
    // DENSIDADES
    // ========================================================================

    /**
     * @return ocupación global = cubetas OCCUPADAS / total de cubetas.
     *         Cada cubeta tiene UN solo espacio: está ocupada si guarda su
     *         clave principal (las colisiones ENLAZADAS no ocupan otro
     *         espacio de cubeta, solo memoria encadenada). Cuando esta
     *         ocupación llega al 75% se dispara la expansión total.
     */
    public double densidadExpansion() {
        int ocupadas = 0;
        for (Cubeta c : cubetas) {
            if (c.getCantidad() > 0) {
                ocupadas++;
            }
        }
        return (cubetas.length == 0) ? 0 : (double) ocupadas / cubetas.length;
    }

    /** @return ocupación por cubeta = cubetas ocupadas / total de cubetas. */
    public double densidadReduccion() {
        return densidadExpansion();
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    /** @return true si la clave existe en alguna cubeta. */
    public boolean contieneClave(int clave) {
        for (Cubeta c : cubetas) {
            if (c.contiene(clave)) {
                return true;
            }
        }
        return false;
    }

    /** @return cantidad total de claves. */
    public int getCantidad() {
        return cantidadRegistros;
    }

    /** @return número actual de cubetas. */
    public int getNumeroCubetas() {
        return cubetas.length;
    }

    /** @return la cubeta indicada (índice base 0). */
    public Cubeta consultarCubeta(int indice) {
        return cubetas[indice];
    }

    /** @return copia de todas las claves, en orden de cubeta. */
    public int[] obtenerClavesPlanas() {
        List<Integer> acumulo = new ArrayList<>();
        for (Cubeta c : cubetas) {
            for (int d : c.getDatos()) {
                acumulo.add(d);
            }
        }
        int[] resultado = new int[acumulo.size()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = acumulo.get(i);
        }
        return resultado;
    }

    /** @return índice base 0 de la cubeta que contendría la clave, o -1. */
    public int direccionDe(int clave) {
        for (int i = 0; i < cubetas.length; i++) {
            if (cubetas[i].contiene(clave)) {
                return i;
            }
        }
        return obtenerPosicion(clave);
    }

    /** @return dígitos configurables de las claves. */
    public int getDigitosClave() {
        return digitosClave;
    }

    /** @return límite inferior aceptable (1 dígito -> 0). */
    public long getValorMinimo() {
        return (digitosClave == 1) ? 0 : (long) Math.pow(10, digitosClave - 1);
    }

    /** @return límite superior aceptable. */
    public long getValorMaximo() {
        return (long) Math.pow(10, digitosClave) - 1;
    }

    /** @return índice base 0 de la última cubeta expandida parcialmente, o -1. */
    public int getUltimaExpansionParcial() {
        return ultimaExpansionParcial;
    }

    /** @return nº de cubetas tras la última expansión total (0 si ninguna). */
    public int getUltimaExpansionTotal() {
        return ultimaExpansionTotal;
    }

    /** @return capacidad base de las cubetas: cada cubeta guarda 1 elemento. */
    public int getCapacidadBase() {
        return 1;
    }

    /** @return número de filas en que se organizan las cubetas. */
    public int getFilas() {
        return filas;
    }

    /** @return cubetas que hay en cada fila (columnas). */
    public int getCubetasPorFila() {
        return cubetasPorFila;
    }
}
