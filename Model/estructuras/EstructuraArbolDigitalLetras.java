package Model.estructuras;

import java.util.ArrayList;
import java.util.List;

import Model.excepciones.ClaveDuplicadaException;
import Model.excepciones.ClaveInvalidaException;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA DE ÁRBOL DIGITAL DE LETRAS (BÚSQUEDA POR RESIDUOS DIGITAL)
 * ============================================================================
 *
 * Árbol binario donde la UBICACIÓN de cada LETRA depende de los BITS de la
 * POSICIÓN de la letra en el alfabeto inglés (A=1, B=2, ..., Z=26), según el
 * ORDEN DE INSERCIÓN:
 *
 *   1. La letra se convierte a su posición en el alfabeto, y esa posición a
 *      código binario FIJADO SIEMPRE a 5 cifras: las posiciones cuyo binario
 *      no alcanza 5 cifras (p. ej. 3 -> "11") se rellenan con 0 AL INICIO
 *      (3 -> "00011"). Las cifras binarias de la posición quedan SIEMPRE en
 *      las últimas posiciones del patrón. La misma cantidad de cifras para
 *      todas: A=00001, B=00010, C=00011, ..., Z=11010.
 *   2. La PRIMERA letra se coloca en la RAÍZ (siempre que el árbol esté
 *      vacío).
 *   3. Cada letra siguiente desciende desde la raíz leyendo los bits del
 *      binario fijo de 5 cifras UNO A UNO (del más significativo al menor):
 *        - bit '0' -> se desplaza a la IZQUIERDA.
 *        - bit '1' -> se desplaza a la DERECHA.
 *   4. Se usan el número de bits NECESARIO: se detiene en el primer espacio
 *      libre y ahí se coloca la letra. Si el punto ya está ocupado por otra
 *      letra, se mira el siguiente bit de su posición y continúa.
 *
 * GARANTÍA: al tener todas las letras la misma cantidad de cifras binarias,
 * ninguna letra queda "flotando": toda inserción termina en un nodo.
 *
 * TAMAÑO FIJO: esta familia NO admite tamaño configurado ni dígitos de
 * clave; su espacio de claves es el alfabeto A-Z (26 letras), cada una
 * colocada UNA sola vez.
 *
 * AL ELIMINAR: el nodo queda VACÍO pero conserva sus hijos, pues la ruta
 * de otras letras puede pasar por él.
 */
public class EstructuraArbolDigitalLetras {

    /**
     * CIFRAS BINARIAS FIJAS de toda clave letra.
     *
     * La posición máxima del alfabeto es 26, cuyo binario es "11010" (5
     * cifras). Para que TODAS las letras tengan la misma cantidad de cifras
     * binarias, las posiciones de 1 cifra (1..9) y las de binario corto se
     * rellenan con 0 AL INICIO: A=00001, B=00010, C=00011, ..., Z=11010.
     * Así la posición binaria siempre ocupa las ÚLTIMAS cifras del patrón
     * que guía la inserción.
     */
    public static final int CIFRAS_BINARIAS = 5;

    /** Nodo raíz del árbol (null mientras esté vacía la estructura). */
    private NodoArbolDigitalLetras raiz;

    /** Cantidad de letras actualmente colocadas (no vacías). */
    private int cantidad;

    /** Pasos detallados de la última inserción (para el panel de proceso). */
    private List<PasoBusquedaLetras> ultimosPasosInsercion;

    /** Pasos detallados de la última eliminación (para el panel de proceso). */
    private List<PasoBusquedaLetras> ultimosPasosEliminacion;

    // ========================================================================
    // VALIDACIÓN DE LETRAS
    // ========================================================================

    /**
     * Normaliza la letra a mayúscula y valida que pertenezca al alfabeto
     * inglés A-Z.
     *
     * @param letra carácter digitado por el usuario.
     * @return la letra en mayúscula lista para operar.
     * @throws ClaveInvalidaException si no es una letra A-Z.
     */
    private char normalizarLetra(char letra) throws ClaveInvalidaException {
        char mayuscula = Character.toUpperCase(letra);
        if (mayuscula < 'A' || mayuscula > 'Z') {
            throw new ClaveInvalidaException(
                    "Letra inválida: solo se admiten letras del alfabeto "
                            + "inglés (A-Z).");
        }
        return mayuscula;
    }

    /** @return posición de la letra en el alfabeto (A=1..Z=26). */
    private int posicionDe(char letra) {
        return letra - 'A' + 1;
    }

    /**
     * Convierte la posición de una letra a su binario FIJADO a 5 cifras.
     *
     * {@code CIFRAS_BINARIAS} es 5 (el binario de 26, la posición máxima).
     * Los binarios que no lleguen a 5 cifras se rellenan con 0 al inicio:
     * 2 -> "00010", 3 -> "00011", 8 -> "01000", etc. Todas las letras
     * operan con la MISMA cantidad de cifras binarias.
     *
     * @return binario de 5 cifras (ej.: 'A' -> "00001").
     */
    public static String binarioDePosicion(int posicion) {
        String binario = Integer.toBinaryString(posicion);
        StringBuilder fijo = new StringBuilder();
        for (int i = 0; i < CIFRAS_BINARIAS - binario.length(); i++) {
            fijo.append('0');
        }
        return fijo.append(binario).toString();
    }

    /** @return binario fijado a 5 cifras de la posición de la letra. */
    private String binarioDe(char letra) {
        return binarioDePosicion(posicionDe(letra));
    }

    // ========================================================================
    // PASOS DETALLADOS DEL CÁLCULO DE POSICIÓN
    // ========================================================================

    /**
     * Construye los pasos que MUESTRAN cómo se calcula la posición de la
     * letra: conversión al alfabeto inglés, binario sin relleno y fijado
     * siempre a 5 cifras. Solo describe; no modifica el árbol.
     *
     * @param letra letra A-Z analizada.
     * @return lista con los pasos de conversión (letra y bit vacíos).
     */
    private List<PasoBusquedaLetras> pasosDeConversion(char letra) {
        int posicion = posicionDe(letra);
        String binarioCorto = Integer.toBinaryString(posicion);
        String binario = binarioDe(letra);

        StringBuilder ruta = new StringBuilder();
        for (int i = 0; i < binario.length(); i++) {
            if (i > 0) {
                ruta.append(" → ");
            }
            ruta.append(binario.charAt(i));
        }

        List<PasoBusquedaLetras> pasos = new ArrayList<>();
        int numero = 1;
        pasos.add(new PasoBusquedaLetras(numero++, '\0', '\0',
                "Letra objetivo: '" + letra + "'."));
        pasos.add(new PasoBusquedaLetras(numero++, '\0', '\0',
                "Se ubica su posición en el alfabeto inglés (A=1, B=2, ..., "
                        + "Z=26): pos = letra − 'A' + 1 = " + posicion + "."));
        pasos.add(new PasoBusquedaLetras(numero++, '\0', '\0',
                "La posición " + posicion + " se convierte a binario: "
                        + binarioCorto + "."));
        int faltan = CIFRAS_BINARIAS - binarioCorto.length();
        String complemento = (faltan > 0)
                ? "Faltan " + faltan + " cifra(s) para " + CIFRAS_BINARIAS
                        + " y se rellenan con 0 AL INICIO: "
                : "Ya posee las " + CIFRAS_BINARIAS + " cifras: ";
        pasos.add(new PasoBusquedaLetras(numero++, '\0', '\0',
                "Binario FIJADO a " + CIFRAS_BINARIAS + " cifras para todas "
                        + "las letras (la posición queda en las últimas "
                        + "cifras): " + complemento + binario + "."));
        pasos.add(new PasoBusquedaLetras(numero, '\0', '\0',
                "Ruta de bits del binario " + binario
                        + " (0 = izquierda, 1 = derecha): " + ruta + "."));
        return pasos;
    }

    /**
     * Recorre el árbol replicando el camino que la inserción habría seguido
     * para la posición de la letra, comparando la letra guardada en cada
     * nodo visitado.
     *
     * @param letra letra buscada.
     * @return nodo que contiene la letra, o null si no está.
     */
    private NodoArbolDigitalLetras localizarNodo(char letra) {
        return localizarNodo(letra, null);
    }

    /**
     * Igual que {@link #localizarNodo(char)} pero, si se pasa una lista de
     * pasos, registra en ella cada comparación/movimiento del recorrido.
     *
     * @param letra letra buscada.
     * @param pasos lista donde registrar el recorrido (null si no interesa).
     * @return nodo que contiene la letra, o null si no está.
     */
    private NodoArbolDigitalLetras localizarNodo(char letra,
            List<PasoBusquedaLetras> pasos) {
        String bits = binarioDe(letra);
        NodoArbolDigitalLetras actual = raiz;
        int indiceBit = 0;

        while (actual != null) {
            if (!actual.estaVacio() && actual.getLetra() == letra) {
                if (pasos != null) {
                    pasos.add(new PasoBusquedaLetras(pasos.size() + 1,
                            letra, '\0',
                            "Letra '" + letra + "' HALLADA en el nodo con "
                                    + "posición " + posicionDe(letra)
                                    + " (binario " + binarioDe(letra) + ")."));
                }
                return actual;
            }

            char bit = (indiceBit < bits.length())
                    ? bits.charAt(indiceBit)
                    : '0';

            if (pasos != null) {
                if (actual.estaVacio()) {
                    pasos.add(new PasoBusquedaLetras(pasos.size() + 1,
                            letra, '\0',
                            "Nodo vacío (su letra fue eliminada antes); se "
                                    + "continúa el descenso con el bit "
                                    + (indiceBit < bits.length()
                                            ? "'" + bit + "'"
                                            : "implícito '0'") + "."));
                } else {
                    pasos.add(new PasoBusquedaLetras(pasos.size() + 1,
                            actual.getLetra(), bit,
                            "Nodo con '" + actual.getLetra() + "' (posición "
                                    + actual.getPosicion() + ", binario "
                                    + actual.getBinario() + ") no coincide "
                                    + "con '" + letra + "'. Bit "
                                    + (indiceBit < bits.length()
                                            ? "'" + bit + "'"
                                            : "implícito '0'") + " → "
                                    + (bit == '0' ? "izquierda" : "derecha")
                                    + "."));
                }
            }

            actual = (bit == '0') ? actual.getIzquierda()
                    : actual.getDerecha();
            indiceBit++;
        }
        return null;
    }

    // ========================================================================
    // INSERCIÓN GUIADA POR BITS
    // ========================================================================

    /**
     * Inserta la letra siguiendo el guion de la especificación:
     *
     *   FASE 1 - Validación (letra A-Z + no duplicada).
     *   FASE 2 - Convertir la posición de la letra a binario FIJADO a 5
     *            cifras (las posiciones cortas se rellenan con 0 al inicio).
     *   FASE 3 - Si no hay raíz, colocar la letra allí.
     *   FASE 4 - Descender leyendo los bits ('0' izquierda, '1' derecha);
     *            usar solo los bits necesarios: detenerse en el primer
     *            espacio libre. Al tener todas las letras 5 cifras, toda
     *            inserción encuentra su nodo.
     *   FASE 5 - Registrar la letra y contarla.
     *
     * @param letra letra A-Z a insertar.
     * @throws ExcepcionEstructura si la letra no es válida o ya existe.
     */
    public void insertar(char letra) throws ExcepcionEstructura {

        // --- FASE 1: reglas de negocio ---------------------------------------
        letra = normalizarLetra(letra);
        if (contieneLetra(letra)) {
            throw new ClaveDuplicadaException(
                    "La letra '" + letra + "' ya existe en el árbol; "
                            + "cada letra se coloca una sola vez.");
        }

        // --- FASE 2: transformación inmediata a binario ------------------------
        String bits = binarioDe(letra);
        List<PasoBusquedaLetras> pasos = pasosDeConversion(letra);

        // --- FASE 3: primera letra -> raíz --------------------------------------
        if (raiz == null) {
            raiz = new NodoArbolDigitalLetras(letra);
            pasos.add(new PasoBusquedaLetras(pasos.size() + 1, '\0', '\0',
                    "Árbol vacío: la PRIMERA letra se coloca como RAÍZ del "
                            + "árbol."));
        } else {
            // --- FASE 4: descenso guiado por los bits ---------------------------
            NodoArbolDigitalLetras actual = raiz;
            int indiceBit = 0;
            while (true) {

                // Bit vigente: el binario fijo de 5 cifras nunca se agota dentro
                // del descenso de la letra (salvaguarda con '0' si ocurriera).
                char bit = (indiceBit < bits.length())
                        ? bits.charAt(indiceBit)
                        : '0';

                NodoArbolDigitalLetras siguiente = (bit == '0')
                        ? actual.getIzquierda()
                        : actual.getDerecha();

                if (siguiente == null) {
                    // Espacio libre encontrado: aquí vive la nueva letra.
                    NodoArbolDigitalLetras nuevo =
                            new NodoArbolDigitalLetras(letra);
                    if (bit == '0') {
                        actual.setIzquierda(nuevo);
                    } else {
                        actual.setDerecha(nuevo);
                    }
                    pasos.add(new PasoBusquedaLetras(pasos.size() + 1,
                            letra, bit,
                            "Bit " + (indiceBit + 1) + " = '" + bit + "' → "
                                    + (bit == '0' ? "izquierda" : "derecha")
                                    + ": el punto está LIBRE. La letra '"
                                    + letra + "' se coloca aquí."));
                    break;
                }
                // Punto ocupado por otra letra: usar el siguiente bit.
                pasos.add(new PasoBusquedaLetras(pasos.size() + 1,
                        actual.getLetra(), bit,
                        "Nodo con '" + actual.getLetra() + "' (posición "
                                + actual.getPosicion() + ") está OCUPADO. "
                                + "Bit " + (indiceBit + 1) + " = '" + bit
                                + "' → " + (bit == '0' ? "izquierda"
                                        : "derecha")
                                + " hacia '" + siguiente.getLetra() + "'."));
                actual = siguiente;
                indiceBit++;
            }
        }

        // --- FASE 5: registro ----------------------------------------------------
        ultimosPasosInsercion = pasos;
        cantidad++;
    }

    // ========================================================================
    // LOCALIZACIÓN, BÚSQUEDA Y ELIMINACIÓN
    // ========================================================================

    // ========================================================================
    // LOCALIZACIÓN, BÚSQUEDA Y ELIMINACIÓN
    // ========================================================================

    /**
     * Busca la letra recorriendo el árbol nodo a nodo y registrando en PASOS
     * cada comparación/movimiento, para que la vista pueda reproducir la
     * búsqueda animada.
     *
     * @param letra letra solicitada.
     * @return pasos de la búsqueda (siempre con el remate de hallada/no).
     * @throws EstructuraVaciaException si el árbol aún no tiene letras.
     * @throws ClaveInvalidaException   si la letra no es A-Z.
     */
    public List<PasoBusquedaLetras> buscarPasos(char letra)
            throws EstructuraVaciaException, ClaveInvalidaException {

        letra = Character.toUpperCase(letra);
        if (letra < 'A' || letra > 'Z') {
            throw new ClaveInvalidaException(
                    "Letra inválida: solo se admiten letras del alfabeto "
                            + "inglés (A-Z).");
        }
        if (raiz == null) {
            throw new EstructuraVaciaException(
                    "El árbol de letras está vacío: inserte letras primero.");
        }

        // Encabezado: cómo se calcula la posición de la letra objetivo, y
        // luego el descenso nodo a nodo replicando su ruta de bits.
        List<PasoBusquedaLetras> pasos = pasosDeConversion(letra);
        NodoArbolDigitalLetras nodo = localizarNodo(letra, pasos);

        if (nodo == null) {
            pasos.add(new PasoBusquedaLetras(pasos.size() + 1, '\0', '\0',
                    "Se llegó a un espacio libre: la letra '" + letra
                            + "' NO está en el árbol (posición "
                            + posicionDe(letra) + ", binario "
                            + binarioDe(letra) + ")."));
        }
        return pasos;
    }

    /**
     * Elimina la letra vaciando SU nodo (los hijos se conservan porque la
     * ruta de otras letras puede pasar por él).
     *
     * @param letra letra a eliminar.
     * @throws EstructuraVaciaException   si el árbol no tiene letras.
     * @throws ClaveNoEncontradaException si la letra no existe.
     * @throws ClaveInvalidaException     si la letra no es A-Z.
     */
    public void eliminar(char letra)
            throws EstructuraVaciaException, ClaveNoEncontradaException,
            ClaveInvalidaException {

        letra = Character.toUpperCase(letra);
        if (letra < 'A' || letra > 'Z') {
            throw new ClaveInvalidaException(
                    "Letra inválida: solo se admiten letras del alfabeto "
                            + "inglés (A-Z).");
        }
        if (raiz == null) {
            throw new EstructuraVaciaException(
                    "No hay letras que eliminar: el árbol está vacío.");
        }

        // Detalle del cálculo de posición + recorrido hasta el nodo.
        List<PasoBusquedaLetras> pasos = pasosDeConversion(letra);
        NodoArbolDigitalLetras nodo = localizarNodo(letra, pasos);
        if (nodo == null) {
            throw new ClaveNoEncontradaException(
                    "No se puede eliminar: la letra '" + letra
                            + "' no existe en el árbol.");
        }
        nodo.vaciar();
        pasos.add(new PasoBusquedaLetras(pasos.size() + 1, letra, '\0',
                "Se VACÍA el nodo de '" + letra
                        + "': conserva sus hijos porque la ruta de otras "
                        + "letras puede pasar por él."));
        ultimosPasosEliminacion = pasos;
        cantidad--;
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    /** @return true si la letra está colocada en algún nodo no vacío. */
    public boolean contieneLetra(char letra) {
        return localizarNodo(Character.toUpperCase(letra)) != null;
    }

    /** @return nodo raíz del árbol (null si está vacía la estructura). */
    public NodoArbolDigitalLetras getRaiz() {
        return raiz;
    }

    /** @return cantidad de letras actualmente colocadas. */
    public int getCantidad() {
        return cantidad;
    }

    /** @return pasos detallados de la última inserción (para el panel). */
    public List<PasoBusquedaLetras> getUltimosPasosInsercion() {
        return ultimosPasosInsercion;
    }

    /** @return pasos detallados de la última eliminación (para el panel). */
    public List<PasoBusquedaLetras> getUltimosPasosEliminacion() {
        return ultimosPasosEliminacion;
    }

    /** @return identificador técnico de este tipo de estructura. */
    public String getTipo() {
        return "ARBOL DIGITAL LETRAS";
    }
}