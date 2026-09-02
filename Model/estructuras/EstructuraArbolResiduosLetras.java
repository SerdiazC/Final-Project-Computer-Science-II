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
 * ESTRUCTURA DE ÁRBOL DE BÚSQUEDA POR RESIDUOS (VARIANTE DE LETRAS)
 * ============================================================================
 *
 * Árbol binario que ubica LETRAS (A-Z) según los BITS de la POSICIÓN de cada
 * letra en el alfabeto inglés (A=1, B=2, ..., Z=26), usando el binario SIEMPRE
 * fijo a 5 cifras (misma estandarización que el árbol digital de letras: las
 * posiciones cortas se rellenan con 0 al inicio).
 *
 * DIFERENCIAS CLAVE frente al árbol digital de letras:
 *
 *  1. RAÍZ SIEMPRE VACÍA: la raíz es un nodo de ENLACE permanente; ningún
 *     número/letra se ubica jamás en ella. Es solo el punto de partida para
 *     descender por las cifras binarias.
 *
 *  2. NODOS DE ENLACE POR COLISIÓN: cuando dos letras colisionan en un nodo
 *     (comparten el prefijo de bits), ese nodo pasa a convertirse en un NODO
 *     DE ENLACE: queda vacío, bloqueado y ninguna letra futura podrá ubicarse
 *     en él. El proceso es:
 *        - Se mira la siguiente cifra binaria de la letra que ya estaba
 *          insertada y SE MUEVE a esa posición.
 *        - El nodo donde estaba queda como NODO DE ENLACE.
 *        - Con la letra que colisionó también se revisa su siguiente cifra
 *          binaria y se inserta al lado.
 *        - Si sigue habiendo colisión, se repite el proceso hasta que ambas
 *          rutas tengan una cifra distinta y puedan separarse.
 *
 *  BÚSQUEDA Y ELIMINACIÓN (abajo) siguen el mismo descenso por bits. Al
 *  ELIMINAR una letra, su nodo queda vacío y podrá ser reutilizado por otra
 *  ruta (pero NUNCA un nodo de enlace, que permanece bloqueado).
 */
public class EstructuraArbolResiduosLetras {

    /** CIFRAS BINARIAS FIJAS de toda clave letra (reutiliza la del digital). */
    public static final int CIFRAS_BINARIAS =
            EstructuraArbolDigitalLetras.CIFRAS_BINARIAS;

    /** Raíz del árbol: SIEMPRE vacía (nodo de enlace permanente). */
    private final NodoArbolResiduosLetras raiz;

    /** Cantidad de letras actualmente colocadas (no vacías). */
    private int cantidad;

    /**
     * Crea la estructura con la raíz vacía marcada como enlace: así ninguna
     * letra podrá ubicarse en ella y todo descenso parte de las cifras
     * binarias de cada letra.
     */
    public EstructuraArbolResiduosLetras() {
        this.raiz = new NodoArbolResiduosLetras();
        this.raiz.convertirEnEnlace();
    }

    // ========================================================================
    // VALIDACIÓN Y UTILIDADES
    // ========================================================================

    /** @return posicion de la letra en el alfabeto (A=1..Z=26). */
    private int posicionDe(char letra) {
        return letra - 'A' + 1;
    }

    /** @return binario fijado a 5 cifras de la posición de la letra. */
    private String binarioDe(char letra) {
        return EstructuraArbolDigitalLetras.binarioDePosicion(posicionDe(letra));
    }

    /**
     * Rellena con los pasos que MUESTRAN cómo se calcula la posición de una
     * letra (conversión al alfabeto, binario sin relleno y fijado a 5 cifras)
     * y la ruta de bits que guía el descenso. Solo describe; no modifica.
     *
     * @param letra letra A-Z analizada.
     * @return lista con los pasos de conversión.
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
                "La RAÍZ está vacía y NO acepta letras. Se desciende desde "
                        + "ella por la ruta de bits del binario " + binario
                        + " (0 = izquierda, 1 = derecha): " + ruta + "."));
        return pasos;
    }

    // ========================================================================
    // INSERCIÓN CON DESGLOSE POR COLISIÓN
    // ========================================================================

    /**
     * Inserta la letra siguiendo el guion de la variante:
     *
     *  - La raíz (siempre vacía) es el punto de partida.
     *  - Se desciende por los bits del binario fijo.
     *  - Si el punto está vacío y NO es enlace: se ubica ahí la letra.
     *  - Si el punto es un NODO DE ENLACE (bloqueado): se sigue bajando.
     *  - Si el punto está OCUPADO por otra letra: COLISIÓN. El nodo se
     *    convierte en enlace, la letra existente se mueve según su siguiente
     *    bit y la letra nueva se inserta según el suyo; se repite hasta que
     *    ambas rutas difieran en un bit y puedan separarse.
     *
     * @param letra letra A-Z a insertar.
     * @throws ExcepcionEstructura si la letra no es válida o ya existe.
     */
    public void insertar(char letra) throws ExcepcionEstructura {
        letra = Character.toUpperCase(letra);
        if (letra < 'A' || letra > 'Z') {
            throw new ClaveInvalidaException(
                    "Letra inválida: solo se admiten letras del alfabeto "
                            + "inglés (A-Z).");
        }
        if (contieneOcupada(letra)) {
            throw new ClaveDuplicadaException(
                    "La letra '" + letra + "' ya existe en el árbol; "
                            + "cada letra se coloca una sola vez.");
        }
        insertarAux(raiz, letra, 0);
        cantidad++;
    }

    /**
     * Intenta colocar la letra en el subárbol que cuelga de {@code actual},
     * habiendo consumido ya los bits de índices [0..indice-1].
     *
     * @param actual nodo sobre el que se trabaja.
     * @param letra  letra a insertar.
     * @param indice siguiente bit del binario de la letra que se evalúa.
     */
    private void insertarAux(NodoArbolResiduosLetras actual, char letra,
            int indice) {
        if (actual.estaVacio() && !actual.esEnlace()) {
            // Espacio libre: una letra puede ocuparlo.
            actual.guardarLetra(letra);
            return;
        }
        if (actual.esEnlace()) {
            // Nodo de enlace (raíz o fruto de colisión): solo de paso.
            NodoArbolResiduosLetras hijo = hijoPorBit(actual, letra, indice);
            insertarAux(hijo, letra, indice + 1);
            return;
        }
        // Nodo ocupado por otra letra -> COLISIÓN: se desglosa.
        char existente = actual.getLetra();
        desglosar(actual, existente, letra, indice);
    }

    /**
     * Resuelve la colisión entre la letra ya ocupante (existente) y la letra
     * nueva en el nodo {@code nodo} (que está ocupado). El nodo pasa a ser de
     * enlace y ambas letras se mueven cada una según su siguiente bit.
     *
     * @param nodo       nodo ocupado donde ocurrió la colisión.
     * @param existente  letra que ya vivía aquí.
     * @param nueva      letra que chocó.
     * @param indice     índice del siguiente bit para ambas letras.
     */
    private void desglosar(NodoArbolResiduosLetras nodo, char existente,
            char nueva, int indice) {
        nodo.convertirEnEnlace();

        NodoArbolResiduosLetras hijoExistente =
                hijoPorBit(nodo, existente, indice);
        insertarAux(hijoExistente, existente, indice + 1);

        NodoArbolResiduosLetras hijoNueva =
                hijoPorBit(nodo, nueva, indice);
        insertarAux(hijoNueva, nueva, indice + 1);
    }

    /**
     * Devuelve (creando si hace falta y enlazándolo) el hijo de {@code nodo}
     * correspondiente al bit del binario de {@code letra} en el índice dado.
     *
     * @param nodo   nodo padre.
     * @param letra  letra cuyo bit guía la rama.
     * @param indice índice del bit.
     * @return hijo izquierdo (bit 0) o derecho (bit 1), nunca null.
     */
    private NodoArbolResiduosLetras hijoPorBit(
            NodoArbolResiduosLetras nodo, char letra, int indice) {
        char bit = bitDe(letra, indice);
        NodoArbolResiduosLetras hijo = (bit == '0')
                ? nodo.getIzquierda() : nodo.getDerecha();
        if (hijo == null) {
            hijo = new NodoArbolResiduosLetras();
            if (bit == '0') {
                nodo.setIzquierda(hijo);
            } else {
                nodo.setDerecha(hijo);
            }
        }
        return hijo;
    }

    /** @return bit (índice) del binario fijo de la letra; '0' si se sale. */
    private char bitDe(char letra, int indice) {
        String binario = binarioDe(letra);
        if (indice < 0 || indice >= binario.length()) {
            return '0';
        }
        return binario.charAt(indice);
    }

    // ========================================================================
    // BÚSQUEDA (CON PASOS) Y CONSULTAS
    // ========================================================================

    /**
     * Busca la letra recorriendo el árbol por sus bits y registra en PASOS
     * cada comparación/movimiento, para animar la búsqueda en la vista.
     *
     * @param letra letra solicitada.
     * @return pasos de la búsqueda (con remate de hallada/no).
     * @throws EstructuraVaciaException si no hay letras.
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
        if (cantidad == 0) {
            throw new EstructuraVaciaException(
                    "El árbol de residuos está vacío: inserte letras primero.");
        }

        List<PasoBusquedaLetras> pasos = pasosDeConversion(letra);

        // La raíz es un nodo de enlace: el primer bit (índice 0) elige el
        // primer hijo y desde ahí se desciende con los bits siguientes.
        char bitInicial = bitDe(letra, 0);
        NodoArbolResiduosLetras actual = (bitInicial == '0')
                ? raiz.getIzquierda() : raiz.getDerecha();
        int indice = 1;

        while (actual != null) {
            if (actual.estaOcupado()) {
                if (actual.getLetra() == letra) {
                    pasos.add(new PasoBusquedaLetras(pasos.size() + 1, letra,
                            '\0', "Letra '" + letra + "' HALLADA en el nodo "
                                    + "con posición " + posicionDe(letra)
                                    + " (binario " + binarioDe(letra) + ")."));
                    return pasos;
                }
                pasos.add(new PasoBusquedaLetras(pasos.size() + 1,
                        actual.getLetra(), actual.bitEn(indice - 1),
                        "Nodo con '" + actual.getLetra() + "' (posición "
                                + actual.getPosicion() + ", binario "
                                + actual.getBinario() + ") no coincide con '"
                                + letra + "': la letra NO está en el árbol."));
                return pasos;
            }
            if (actual.esEnlace()) {
                pasos.add(new PasoBusquedaLetras(pasos.size() + 1, letra, '\0',
                        "Nodo de ENLACE (raíz o fruto de una colisión): se "
                                + "continúa el descenso por el bit "
                                + (indice < binarioDe(letra).length()
                                        ? "'" + bitDe(letra, indice) + "'"
                                        : "implícito '0'") + "."));
            } else {
                pasos.add(new PasoBusquedaLetras(pasos.size() + 1, letra, '\0',
                        "Nodo VACÍO (espacio libre): la letra '" + letra
                                + "' NO está en el árbol."));
                return pasos;
            }
            actual = hijoPorRuta(actual, letra, indice);
            indice++;
        }

        pasos.add(new PasoBusquedaLetras(pasos.size() + 1, '\0', '\0',
                "Se llegó a un espacio libre: la letra '" + letra
                        + "' NO está en el árbol."));
        return pasos;
    }

    /** Hijo del nodo según el bit de la letra en el índice (puede ser null). */
    private NodoArbolResiduosLetras hijoPorRuta(
            NodoArbolResiduosLetras nodo, char letra, int indice) {
        char bit = bitDe(letra, indice);
        NodoArbolResiduosLetras hijo = (bit == '0')
                ? nodo.getIzquierda() : nodo.getDerecha();
        return hijo;
    }

    /**
     * Elimina la letra dejando su nodo VACÍO (reutilizable). Los nodos de
     * enlace se conservan intactos (nunca se reutilizan).
     *
     * @param letra letra a eliminar.
     * @throws EstructuraVaciaException   si no hay letras.
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
        if (cantidad == 0) {
            throw new EstructuraVaciaException(
                    "No hay letras que eliminar: el árbol está vacío.");
        }
        NodoArbolResiduosLetras nodo = localizarOcupado(letra);
        if (nodo == null) {
            throw new ClaveNoEncontradaException(
                    "No se puede eliminar: la letra '" + letra
                            + "' no existe en el árbol.");
        }
        nodo.guardarLetra('\0');
        // guardarLetra('\0') deja posicion -1 y binario null (coincide).
        cantidad--;
    }

    /**
     * Localiza el nodo ocupado con la letra siguiendo su ruta de bits.
     *
     * @param letra letra a buscar.
     * @return nodo ocupado con la letra, o null si no está.
     */
    private NodoArbolResiduosLetras localizarOcupado(char letra) {
        NodoArbolResiduosLetras actual = raiz;
        int indice = 0;
        while (actual != null) {
            if (actual.estaOcupado() && actual.getLetra() == letra) {
                return actual;
            }
            actual = hijoPorRuta(actual, letra, indice);
            indice++;
        }
        return null;
    }

    /** @return true si hay una letra ocupada igual a la indicada. */
    private boolean contieneOcupada(char letra) {
        return localizarOcupado(Character.toUpperCase(letra)) != null;
    }

    /**
     * Devuelve los pasos de CONVERSIÓN de la letra (posición y binario fijo),
     * que la vista registra de forma estática al insertar o eliminar.
     *
     * @param letra letra A-Z.
     * @return pasos que describen la conversión de la letra.
     */
    public List<PasoBusquedaLetras> pasosInsercion(char letra) {
        return pasosDeConversion(Character.toUpperCase(letra));
    }

    // ========================================================================
    // CONSULTAS EXTERNAS
    // ========================================================================

    /** @return raíz del árbol (siempre vacía / nodo de enlace). */
    public NodoArbolResiduosLetras getRaiz() {
        return raiz;
    }

    /** @return cantidad de letras actualmente colocadas (no vacías). */
    public int getCantidad() {
        return cantidad;
    }

    /** @return true si hay al menos una letra colocada. */
    public boolean tieneLetras() {
        return cantidad > 0;
    }

    /** @return identificador técnico de este tipo de estructura. */
    public String getTipo() {
        return "RESIDUOS LETRAS";
    }
}

