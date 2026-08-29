package Model.estructuras;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.EstructuraDeDatos;
import Model.colisiones.SolucionColision;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.ColisionHashException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * ESTRUCTURA HASH (TRANSFORMACIÓN DE CLAVES + SOLUCIÓN DE COLISIONES)
 * ============================================================================
 *
 * Familia de estructuras donde cada clave NO se guarda junto a las demás,
 * sino en la DIRECCIÓN que calcula una {@link FuncionHash}:
 *
 *   dirección = funciónHash(clave, tamaño)   con dirección en [1 .. tamaño]
 *
 * Cuando dos claves caen en la MISMA dirección (colisión), la clave que
 * llega tarde se ubica según la {@link SolucionColision} elegida al crear
 * la estructura:
 *
 *   - POR SONDEO (lineal, cuadrática o doble dispersión): prueba posiciones
 *     alternas del arreglo principal hasta hallar una libre; si tras agotar
 *     los intentos no hay espacio, la inserción se rechaza.
 *   - POR CUBETAS (encadenamiento o arreglo anidado): la clave se agrega al
 *     contenedor de SU dirección; el arreglo anidado limita la fila a
 *     "tamaño" claves por dirección.
 *
 * REGLA DEL PROYECTO: la solución se fija al crear la estructura y SOLO
 * puede cambiarse reiniciándola (estructura nueva y vacía); así todas las
 * colisiones de una misma estructura usan siempre la misma técnica.
 *
 * La misma clase sirve para HASH MOD/CUADRADO/TRUNCAMIENTO/PLEGAMIENTO:
 * solo cambia la función inyectada (COMPOSICIÓN sobre herencia).
 *
 * NOTA ACADÉMICA: al eliminar deja el espacio VACÍO (hueco). Como en las
 * implementaciones de referencia, la búsqueda por sondeo se detiene al
 * encontrar un espacio libre, por lo que un hueco intermedio puede cortar
 * el rastreo de claves sondeadas más allá.
 */
public class EstructuraHash extends EstructuraContigua {

    /** Función de transformación de claves que gobierna las direcciones. */
    private final FuncionHash funcionHash;

    /** Solución con la que TODAS las colisiones de esta estructura se resuelven. */
    private final SolucionColision solucionColision;

    /**
     * Desbordes por cubetas: índice interno (base 0) -> claves que llegaron
     * tarde a esa dirección, en orden de llegada. Solo lo llenan las
     * soluciones que NO sondean (encadenamiento / arreglo anidado).
     */
    private final Map<Integer, List<Integer>> desbordes = new LinkedHashMap<>();

    /**
     * Pasos de la ÚLTIMA inserción (colisión y su resolución), para que la
     * vista web anime dónde se presentó la colisión y cómo la resolvió la
     * solución elegida. Se regenera en cada {@link #insertar(int)}.
     */
    private List<PasoInsercion> pasosInsercion = new ArrayList<>();

    /**
     * Crea la estructura dispersa con su solución de colisiones definitiva.
     *
     * @param digitosClave     dígitos exactos que tendrán las claves (1..7).
     * @param capacidad        tamaño exacto de la estructura (1..1000).
     * @param funcionHash      estrategia que calcula las direcciones.
     * @param solucionColision técnica para TODAS las colisiones (no null).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    public EstructuraHash(int digitosClave, int capacidad, FuncionHash funcionHash,
            SolucionColision solucionColision) throws ExcepcionEstructura {
        super(digitosClave, capacidad);
        this.funcionHash = funcionHash;
        this.solucionColision = solucionColision;
    }

    /** @return la función hash asociada a esta estructura. */
    public FuncionHash getFuncionHash() {
        return funcionHash;
    }

    /** @return la solución con la que se resuelven TODAS las colisiones. */
    public SolucionColision getSolucionColision() {
        return solucionColision;
    }

    /**
     * Calcula la dirección (1-based) que esta estructura asignaría a la clave.
     *
     * @param clave valor a transformar.
     * @return dirección resultante entre 1 y getCapacidad().
     */
    public int calcularDireccion(int clave) {
        return funcionHash.calcularDireccion(clave, getCapacidad());
    }

    // ========================================================================
    // VALIDACIÓN DE INSERCIÓN SEGÚN LA SOLUCIÓN ELEGIDA
    // ========================================================================

    /**
     * Fase de reserva: además de las reglas comunes, comprueba que exista
     * LUGAR para la clave según la solución configurada:
     *
     *   - Sondeo: debe haber al menos un índice libre en su secuencia; si
     *     todos los intentos fallan se rechaza informando la solución.
     *   - Encadenamiento y arreglo anidado: siempre hay lugar mientras la
     *     estructura global no esté llena (el techo de "tamaño" claves de
     *     la estructura prevalece sobre el límite por fila del prototipo
     *     NestedArray, pues es matemáticamente más restrictivo).
     *
     * @param clave valor que se pretende almacenar.
     * @throws ExcepcionEstructura regla común incumplida o sin lugar.
     */
    @Override
    public void verificarPuedeInsertar(int clave) throws ExcepcionEstructura {
        super.verificarPuedeInsertar(clave);

        if (claves[calcularDireccion(clave) - 1] != CLAVE_VACIA
                && solucionColision.esPorSondeo()
                && encontrarIndiceLibrePorSondeo(clave)
                        == SolucionColision.SIN_ESPACIO) {
            throw new ColisionHashException(
                    "Colisión con " + solucionColision.getNombre()
                            + ": no quedó ningún espacio libre tras sondear "
                            + "toda la estructura.");
        }
    }

    /**
     * Recorre la secuencia de sondeo de la clave buscando un índice libre.
     *
     * @param clave clave cuya dirección original ya está ocupada.
     * @return índice libre hallado, o SIN_ESPACIO si no existe.
     */
    private int encontrarIndiceLibrePorSondeo(int clave) {
        int base = calcularDireccion(clave) - 1;
        for (int intento = 1; intento <= getCapacidad() - 1; intento++) {
            int candidato = solucionColision.calcularIndice(base, intento,
                    clave, getCapacidad());
            if (claves[candidato] == CLAVE_VACIA) {
                return candidato;
            }
        }
        return SolucionColision.SIN_ESPACIO;
    }

    // ========================================================================
    // OPERACIONES PRINCIPALES
    // ========================================================================

    /**
     * Guarda la clave aplicando la solución de colisiones configurada y
     * REGISTRA los pasos de la inserción en {@link #pasosInsercion} para
     * que la vista pueda animar:
     *
     *   FASE 1 - Validaciones comunes + lugar según la solución.
     *   FASE 2 - Calcular la dirección con la función hash.
     *   FASE 3 - Ubicar: directa si está libre; si hay colisión, registrar
     *            DÓNDE se presentó ("colision") y luego sondea o agrega a la
     *            cubeta de la dirección ("sondeo"/"desborde") hasta el éxito
     *            final ("exito").
     *
     * @param clave valor numérico a almacenar.
     * @throws ExcepcionEstructura si viola alguna regla de negocio.
     */
    @Override
    public void insertar(int clave) throws ExcepcionEstructura {

        // --- FASE 1: mismas garantías de siempre ----------------------------
        verificarPuedeInsertar(clave);
        pasosInsercion = new ArrayList<>();

        // --- FASE 2: transformación de la clave ------------------------------
        int indiceBase = calcularDireccion(clave) - 1;
        int direccion = indiceBase + 1;
        int numeroPaso = 0;

        if (claves[indiceBase] == CLAVE_VACIA) {
            // --- FASE 3a: colocación DIRECTA (sin colisión) ------------------
            numeroPaso++;
            pasosInsercion.add(new PasoInsercion(numeroPaso, "directa",
                    indiceBase, direccion, clave,
                    "La dirección " + direccion + " calculada por "
                            + funcionHash.getNombre() + " está LIBRE: la clave "
                            + clave + " se coloca directamente allí."));
            claves[indiceBase] = clave;

        } else if (solucionColision.esPorSondeo()) {
            // --- FASE 3b: COLISIÓN resuelta por SONDEO -----------------------
            numeroPaso++;
            pasosInsercion.add(new PasoInsercion(numeroPaso, "colision",
                    indiceBase, direccion, claves[indiceBase],
                    "¡COLISIÓN! La dirección " + direccion + " ya está ocupada "
                            + "por " + claves[indiceBase] + ". La solución "
                            + solucionColision.getNombre() + " sondea posiciones "
                            + "alternas del arreglo."));

            int destino = SolucionColision.SIN_ESPACIO;
            for (int intento = 1; intento <= getCapacidad() - 1; intento++) {
                int candidato = solucionColision.calcularIndice(indiceBase,
                        intento, clave, getCapacidad());
                numeroPaso++;
                if (claves[candidato] == CLAVE_VACIA) {
                    pasosInsercion.add(new PasoInsercion(numeroPaso, "exito",
                            candidato, candidato + 1, clave,
                            "Intento " + intento + " de "
                                    + solucionColision.getNombre() + ": la posición "
                                    + (candidato + 1) + " está LIBRE. La clave "
                                    + clave + " se coloca allí."));
                    destino = candidato;
                    break;
                }
                pasosInsercion.add(new PasoInsercion(numeroPaso, "sondeo",
                        candidato, candidato + 1, claves[candidato],
                        "Intento " + intento + " de "
                                + solucionColision.getNombre() + ": la posición "
                                + (candidato + 1) + " también está ocupada por "
                                + claves[candidato] + "; se continúa sondeando."));
            }
            claves[destino] = clave;

        } else {
            // --- FASE 3c: COLISIÓN resuelta por CUBETA -----------------------
            numeroPaso++;
            pasosInsercion.add(new PasoInsercion(numeroPaso, "colision",
                    indiceBase, direccion, claves[indiceBase],
                    "¡COLISIÓN! La dirección " + direccion + " ya está ocupada "
                            + "por " + claves[indiceBase] + ". La solución "
                            + solucionColision.getNombre() + " agrega la clave a "
                            + "la cubeta de SU dirección."));

            desbordes.computeIfAbsent(indiceBase, k -> new ArrayList<>()).add(clave);
            numeroPaso++;
            pasosInsercion.add(new PasoInsercion(numeroPaso, "desborde",
                    indiceBase, direccion, clave,
                    "La clave " + clave + " se ENCADENÓ en la cubeta de la "
                            + "dirección " + direccion + " (hoy con "
                            + desbordes.get(indiceBase).size() + " clave(s))."));
        }
        cantidad++;
    }

    /**
     * @return copia de los pasos registrados en la ÚLTIMA inserción, para
     *         que la vista los anime (lista vacía si aún no se insertó nada).
     */
    public List<PasoInsercion> obtenerUltimosPasosInsercion() {
        return new ArrayList<>(pasosInsercion);
    }

    /**
     * Elimina la clave estés donde esté (espacio principal o cubeta):
     *
     *   - En el espacio principal: la dirección queda vacía (hueco), pues
     *     mover datos alteraría las direcciones calculadas.
     *   - En una cubeta: sale de la lista/fila; si la cubeta queda vacía,
     *     se elimina el contenedor.
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si no hay datos.
     * @throws ClaveNoEncontradaException si la clave no existe.
     */
    @Override
    public void eliminar(int clave)
            throws EstructuraVaciaException, ClaveNoEncontradaException {

        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "No hay claves para eliminar: la estructura está vacía.");
        }

        for (int i = 0; i < getCapacidad(); i++) {
            if (claves[i] == clave) {
                claves[i] = CLAVE_VACIA;
                cantidad--;
                return;
            }
        }
        for (Map.Entry<Integer, List<Integer>> cubeta : desbordes.entrySet()) {
            List<Integer> fila = cubeta.getValue();
            if (fila.remove(Integer.valueOf(clave))) {
                if (fila.isEmpty()) {
                    desbordes.remove(cubeta.getKey());
                }
                cantidad--;
                return;
            }
        }
        throw new ClaveNoEncontradaException(
                "No se puede eliminar: la clave " + clave
                        + " no existe en la estructura.");
    }

    // ========================================================================
    // CONSULTAS (INCLUYEN LAS CUBETAS)
    // ========================================================================

    /** {@inheritDoc} Busca también dentro de las cubetas de desborde. */
    @Override
    public boolean contieneClave(int clave) {
        return indiceDe(clave) != -1 || buscarCubetaDe(clave) != -1;
    }

    /**
     * Localiza la cubeta que contiene la clave indicada.
     *
     * @param clave valor buscado.
     * @return índice base de su cubeta, o -1 si ninguna la contiene.
     */
    private int buscarCubetaDe(int clave) {
        for (Map.Entry<Integer, List<Integer>> cubeta : desbordes.entrySet()) {
            if (cubeta.getValue().contains(clave)) {
                return cubeta.getKey();
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc} Recorre el arreglo principal y, tras cada clave ocupada,
     * emite las claves desbordadas de esa dirección en su orden de llegada:
     * el resultado es determinista para migraciones y listados.
     */
    @Override
    public int[] obtenerClaves() {
        List<Integer> acumulo = new ArrayList<>();
        for (int i = 0; i < getCapacidad(); i++) {
            if (claves[i] != CLAVE_VACIA) {
                acumulo.add(claves[i]);
            }
            List<Integer> fila = desbordes.get(i);
            if (fila != null) {
                acumulo.addAll(fila);
            }
        }
        int[] resultado = new int[acumulo.size()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = acumulo.get(i);
        }
        return resultado;
    }

    /** {@inheritDoc} Las direcciones calculadas no garantizan orden. */
    @Override
    public boolean estaOrdenadaAscendente() {
        return false;
    }

    /**
     * Entrega copia de las claves desbordadas de una dirección (para que
     * la búsqueda las narre y la vista las dibuje).
     *
     * @param indiceBase índice interno (base 0) de la dirección.
     * @return copia de la fila de desbordes (vacía si no tiene).
     */
    public int[] consultarDesbordes(int indiceBase) {
        List<Integer> fila = desbordes.get(indiceBase);
        if (fila == null) {
            return new int[0];
        }
        int[] copia = new int[fila.size()];
        for (int i = 0; i < copia.length; i++) {
            copia[i] = fila.get(i);
        }
        return copia;
    }

    /**
     * Construye el resumen textual de los desbordes actuales (para el
     * listado de la vista).
     *
     * @return texto "direccion X -> [k1, k2]; ..." o cadena vacía si no
     *         hay desbordes.
     */
    public String describirDesbordes() {
        StringBuilder texto = new StringBuilder();
        for (Map.Entry<Integer, List<Integer>> cubeta : desbordes.entrySet()) {
            if (texto.length() > 0) {
                texto.append("; ");
            }
            texto.append("direccion ").append(cubeta.getKey() + 1).append(" -> ")
                    .append(cubeta.getValue());
        }
        return texto.toString();
    }

    /**
     * PASO VARIABLE del guion contiguo: para esta familia la posición
     * destino es exactamente la dirección calculada menos uno. No se usa
     * en la práctica porque {@link #insertar} redefine el algoritmo
     * completo (colocación directa sin desplazamientos), pero se conserva
     * el contrato con la clase base.
     *
     * @param clave clave ya validada y sin colisión.
     * @return índice interno (base 0) de su dirección hash.
     */
    @Override
    protected int calcularPosicionInsercion(int clave) {
        return calcularDireccion(clave) - 1;
    }

    /** @return el nombre de la función hash como tipo de estructura. */
    @Override
    public String getTipo() {
        return funcionHash.getNombre();
    }
}
