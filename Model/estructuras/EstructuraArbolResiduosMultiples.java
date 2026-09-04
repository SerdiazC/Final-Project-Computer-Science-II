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
 * ESTRUCTURA DEL ÁRBOL DE BÚSQUEDA POR RESIDUOS MÚLTIPLES
 * ============================================================================
 *
 * Árbol que ubica LETRAS (A-Z) por su posición en el alfabeto inglés y el
 * binario FIJO a 5 cifras de esa posición (mismo mecanismo de las búsquedas
 * de residuos anteriores). Lo que cambia es la CREACIÓN del árbol: cada
 * nivel agrupa VARIAS cifras binarias (de ahí "residuos múltiples").
 *
 * La estructura del árbol es SIEMPRE fija:
 *
 *   Nivel 0  RAÍZ ......... vacía (nodo de enlace permanente; no guarda letra)
 *   Nivel 1  4 nodos .............. etiquetas 00, 01, 10, 11 (2 primeras cifras)
 *   Nivel 2  4 nodos por cada nivel 1 .... etiquetas 00, 01, 10, 11 (2 siguientes)
 *   Nivel 3  2 nodos por cada nivel 2 .... etiquetas 0, 1 (última cifra)
 *   Nivel 4  la LETRA .............. cuelga del nodo del nivel 3 correspondiente
 *
 * Al recorrer desde la raíz, las etiquetas de los enlaces se concatenan y
 * forman el binario FIJO de 5 cifras de la letra insertada. Cada letra tiene
 * una ruta única (los binarios de A-Z son todos distintos), de modo que cada
 * hoja del nivel 3 aloja a lo sumo UNA letra.
 *
 *  MÉTODOS: insertar, buscar (con pasos), eliminar y consultas.
 */
public class EstructuraArbolResiduosMultiples {

    /** CIFRAS BINARIAS FIJAS de toda clave letra (reutiliza la del digital). */
    public static final int CIFRAS_BINARIAS =
            EstructuraArbolDigitalLetras.CIFRAS_BINARIAS;

    /** Raíz del árbol: SIEMPRE vacía (nivel 0). */
    private final NodoArbolResiduosMultiples raiz;

    /** Cantidad de letras actualmente insertadas. */
    private int cantidad;

    /**
     * Crea la estructura construyendo TODO el esqueleto fijo: raíz vacía y
     * todos los nodos de los niveles 1, 2 y 3 con sus etiquetas.
     */
    public EstructuraArbolResiduosMultiples() {
        this.raiz = new NodoArbolResiduosMultiples("", 0, 4);
        construirEsqueleto(this.raiz);
    }

    /**
     * Construye (recursivamente) el esqueleto fijo del árbol a partir de un
     * nodo dado: crea todos sus hijos con la profundidad correspondiente.
     *
     * @param nodo nodo cuyo esqueleto se completa.
     */
    private void construirEsqueleto(NodoArbolResiduosMultiples nodo) {
        if (nodo.esHoja()) {
            return;
        }
        int cantidadHijosHijo = (nodo.getNivel() == 2) ? 0 : (
                (nodo.getNivel() == 1) ? 2 : 4);
        for (int i = 0; i < nodo.cantidadHijos(); i++) {
            String etiqueta = etiquetaHijo(nodo.getNivel(), i);
            int nivelHijo = nodo.getNivel() + 1;
            NodoArbolResiduosMultiples hijo = (nivelHijo == 3)
                    ? new NodoArbolResiduosMultiples(etiqueta, 3, 0)
                    : new NodoArbolResiduosMultiples(etiqueta, nivelHijo,
                            (nivelHijo == 2) ? 2 : 4);
            nodo.setHijo(i, hijo);
            construirEsqueleto(hijo);
        }
    }

    /**
     * Devuelve la etiqueta del enlace de un hijo según el nivel del padre y
     * la posición del hijo (las etiquetas se reúnen para formar el binario).
     *
     * @param nivelPadre nivel del nodo padre (0 o 1 dan etiquetas de 2 bits;
     *                   el nivel 2 da etiquetas de 1 bit).
     * @param indice     posición del hijo (0..3, o 0..1 en nivel 2).
     * @return etiqueta del enlace (ej. "01" o "1").
     */
    private String etiquetaHijo(int nivelPadre, int indice) {
        if (nivelPadre == 2) {
            return (indice == 0) ? "0" : "1";
        }
        String bits = (indice >> 1) + "" + (indice & 1);
        return bits;
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
     * Devuelve la ruta (secuencia de índices de hijos) que debe seguir una
     * letra: [nivel1][nivel2][nivel3] según los grupos de cifras del binario.
     *
     * @param letra letra A-Z.
     * @return arreglo de 3 índices: rama de nivel 1, de nivel 2 y de nivel 3.
     */
    private int[] rutaDeIndices(char letra) {
        String bin = binarioDe(letra);
        return new int[] {
            Integer.parseInt(bin.substring(0, 2), 2),
            Integer.parseInt(bin.substring(2, 4), 2),
            Integer.parseInt(bin.substring(4, 5), 2)
        };
    }

    /**
     * Navega por el esqueleto siguiendo los índices de la ruta de la letra y
     * devuelve el nodo HOJA (nivel 3) donde se alojaría o se aloja.
     *
     * @param letra letra A-Z.
     * @return nodo del nivel 3 de la ruta de la letra.
     */
    private NodoArbolResiduosMultiples nodoRuta(char letra) {
        int[] idx = rutaDeIndices(letra);
        NodoArbolResiduosMultiples actual = raiz.getHijo(idx[0]);
        actual = actual.getHijo(idx[1]);
        return actual.getHijo(idx[2]);
    }

    /**
     * Rellena con los pasos que MUESTRAN cómo se calcula la posición/binario
     * de una letra y cómo se agrupan sus cifras en los 3 enlaces del árbol.
     * Solo describe; no modifica.
     *
     * @param letra letra A-Z analizada.
     * @return lista con los pasos de conversión.
     */
    private List<PasoBusquedaMultiples> pasosDeConversion(char letra) {
        int posicion = posicionDe(letra);
        String binarioCorto = Integer.toBinaryString(posicion);
        String binario = binarioDe(letra);
        int[] idx = rutaDeIndices(letra);

        List<PasoBusquedaMultiples> pasos = new ArrayList<>();
        int numero = 1;
        pasos.add(new PasoBusquedaMultiples(numero++, "", "", '\0',
                "Letra objetivo: '" + letra + "'."));
        pasos.add(new PasoBusquedaMultiples(numero++, "", "", '\0',
                "Se ubica su posición en el alfabeto inglés (A=1, B=2, ..., "
                        + "Z=26): pos = letra − 'A' + 1 = " + posicion + "."));
        pasos.add(new PasoBusquedaMultiples(numero++, "", "", '\0',
                "La posición " + posicion + " se convierte a binario: "
                        + binarioCorto + "."));
        int faltan = CIFRAS_BINARIAS - binarioCorto.length();
        String complemento = (faltan > 0)
                ? "Faltan " + faltan + " cifra(s) para " + CIFRAS_BINARIAS
                        + " y se rellenan con 0 AL INICIO: "
                : "Ya posee las " + CIFRAS_BINARIAS + " cifras: ";
        pasos.add(new PasoBusquedaMultiples(numero++, "", "", '\0',
                "Binario FIJADO a " + CIFRAS_BINARIAS + " cifras: "
                        + complemento + binario + "."));
        pasos.add(new PasoBusquedaMultiples(numero++, "", "", '\0',
                "Las 5 cifras se agrupan en 3 ENLACES por niveles: primer "
                        + "enlace [" + binario.substring(0, 2) + "] (cifras "
                        + "1-2), segundo enlace [" + binario.substring(2, 4)
                        + "] (cifras 3-4) y tercer enlace ["
                        + binario.substring(4, 5) + "] (cifra 5)."));
        return pasos;
    }

    // ========================================================================
    // INSERCIÓN
    // ========================================================================

    /**
     * Inserta la letra en su hoja del nivel 3, siguiendo la ruta que forman
     * sus enlaces (cuyas etiquetas reunidas dan el binario de 5 cifras).
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
        NodoArbolResiduosMultiples hoja = nodoRuta(letra);
        if (hoja.tieneLetra()) {
            throw new ClaveDuplicadaException(
                    "La letra '" + letra + "' ya existe en el árbol; "
                            + "cada letra se coloca una sola vez.");
        }
        hoja.guardarLetra(letra);
        cantidad++;
    }

    // ========================================================================
    // BÚSQUEDA (CON PASOS)
    // ========================================================================

    /**
     * Busca la letra recorriendo su ruta de enlaces y registra en pasos cada
     * nodo visitado, para animar la búsqueda en la vista.
     *
     * @param letra letra solicitada.
     * @return pasos de la búsqueda (con remate de hallada/no).
     * @throws EstructuraVaciaException si no hay letras.
     * @throws ClaveInvalidaException   si la letra no es A-Z.
     */
    public List<PasoBusquedaMultiples> buscarPasos(char letra)
            throws EstructuraVaciaException, ClaveInvalidaException {
        letra = Character.toUpperCase(letra);
        if (letra < 'A' || letra > 'Z') {
            throw new ClaveInvalidaException(
                    "Letra inválida: solo se admiten letras del alfabeto "
                            + "inglés (A-Z).");
        }
        if (cantidad == 0) {
            throw new EstructuraVaciaException(
                    "El árbol de residuos múltiples está vacío: inserte "
                            + "letras primero.");
        }

        List<PasoBusquedaMultiples> pasos = pasosDeConversion(letra);
        int[] idx = rutaDeIndices(letra);

        pasos.add(new PasoBusquedaMultiples(pasos.size() + 1, "", "", '\0',
                "Se parte de la RAÍZ (siempre vacía). Primer enlace ["
                        + binarioDe(letra).substring(0, 2)
                        + "] guía la rama."));
        NodoArbolResiduosMultiples n1 = raiz.getHijo(idx[0]);
        pasos.add(new PasoBusquedaMultiples(pasos.size() + 1,
                n1.getEtiqueta(), n1.getEtiqueta(), '\0',
                "Nodo de nivel 1 con enlace [" + n1.getEtiqueta()
                        + "]: de paso. Segundo enlace ["
                        + binarioDe(letra).substring(2, 4) + "] guía la rama."));
        NodoArbolResiduosMultiples n2 = n1.getHijo(idx[1]);
        pasos.add(new PasoBusquedaMultiples(pasos.size() + 1,
                n1.getEtiqueta() + n2.getEtiqueta(), n2.getEtiqueta(), '\0',
                "Nodo de nivel 2 con enlace [" + n2.getEtiqueta()
                        + "]: de paso. Tercer enlace ["
                        + binarioDe(letra).substring(4, 5) + "] guía la rama."));
        NodoArbolResiduosMultiples n3 = n2.getHijo(idx[2]);
        String ruta = n1.getEtiqueta() + n2.getEtiqueta() + n3.getEtiqueta();

        if (n3.tieneLetra()) {
            if (n3.getLetra() == letra) {
                pasos.add(new PasoBusquedaMultiples(pasos.size() + 1, ruta,
                        n3.getEtiqueta(), letra,
                        "Llegada al nodo de nivel 3 con enlace ["
                                + n3.getEtiqueta() + "]: la letra '" + letra
                                + "' HALLADA. (Los enlaces recorridos forman "
                                + "el binario " + binarioDe(letra) + ")."));
            } else {
                pasos.add(new PasoBusquedaMultiples(pasos.size() + 1, ruta,
                        n3.getEtiqueta(), n3.getLetra(),
                        "El nodo de nivel 3 guarda '" + n3.getLetra()
                                + "' (binario " + binarioDe(n3.getLetra())
                                + ") y no coincide con '" + letra
                                + "': la letra NO está en el árbol."));
            }
        } else {
            pasos.add(new PasoBusquedaMultiples(pasos.size() + 1, ruta,
                    n3.getEtiqueta(), '\0',
                    "Nodo de nivel 3 sin letra (espacio libre): la letra '"
                            + letra + "' NO está en el árbol."));
        }
        return pasos;
    }

    // ========================================================================
    // ELIMINACIÓN
    // ========================================================================

    /**
     * Elimina la letra dejando su hoja del nivel 3 vacía (el esqueleto se
     * conserva intacto).
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
        NodoArbolResiduosMultiples hoja = nodoRuta(letra);
        if (!hoja.tieneLetra() || hoja.getLetra() != letra) {
            throw new ClaveNoEncontradaException(
                    "No se puede eliminar: la letra '" + letra
                            + "' no existe en el árbol.");
        }
        hoja.guardarLetra('\0');
        cantidad--;
    }

    // ========================================================================
    // CONSULTAS Y UTILIDADES PARA LA VISTA
    // ========================================================================

    /** @return raíz del árbol (siempre vacía). */
    public NodoArbolResiduosMultiples getRaiz() {
        return raiz;
    }

    /** @return cantidad de letras actualmente insertadas. */
    public int getCantidad() {
        return cantidad;
    }

    /** @return true si hay al menos una letra insertada. */
    public boolean tieneLetras() {
        return cantidad > 0;
    }

    /** @return identificador técnico de este tipo de estructura. */
    public String getTipo() {
        return "RESIDUOS MULTIPLES";
    }

    /**
     * Devuelve los pasos de CONVERSIÓN de la letra (posición, binario y
     * agrupación en enlaces), que la vista registra de forma estática al
     * insertar o eliminar.
     *
     * @param letra letra A-Z.
     * @return pasos que describen la conversión de la letra.
     */
    public List<PasoBusquedaMultiples> pasosInsercion(char letra) {
        return pasosDeConversion(Character.toUpperCase(letra));
    }
}
