package Model.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Model.excepciones.ClaveInvalidaException;

/**
 * ============================================================================
 * ESTRUCTURA DEL ÁRBOL DE HUFFMAN
 * ============================================================================
 *
 * Construye el árbol de Huffman a partir de UNA PALABRA que el usuario digita.
 * La palabra solo admite LETRAS (A-Z) y el carácter especial '_' para
 * simbolizar el espacio entre palabras.
 *
 * EL PROCESO (el que muestra la vista paso a paso):
 *
 *  1. SE CUENTAN las frecuencias: cuántas veces aparece cada carácter.
 *  2. CADA frecuencia se expresa como COCIENTE (cantidad / total de caracteres).
 *  3. SE ORDENA LA TABLA de mayor a menor frecuencia. Si dos caracteres empatan
 *     en frecuencia, se colocan según el orden en que aparecieron en la palabra
 *     PERO AL REVÉS: el que el usuario escribió PRIMERO queda MÁS ABAJO (las
 *     posiciones inferiores de la tabla se procesan primero en la agrupación).
 *  4. SIEMPRE se agrupan los DOS últimos de la tabla (los de menor frecuencia):
 *     se unen en un nodo cuya frecuencia es la SUMA y su expresión es, por
 *     ejemplo, "(E + J)". El nodo se reinserta en la tabla según su nueva
 *     frecuencia (algoritmo clásico de Huffman) y se repite hasta quedar UNA
 *     sola expresión: la ECUACIÓN agrupada final (raíz del árbol).
 *  5. RECORRIENDO el árbol (izquierda = 0, derecha = 1) se obtiene el código
 *     de Huffman de cada carácter (los más frecuentes quedan más cerca de la
 *     raíz y por tanto con códigos más cortos).
 *
 * ALGORITMO: Huffman clásico. Como desempate en frecuencias iguales se usa el
 * orden de creación (secuencia): la hoja creada antes se procesa antes; los
 * nodos internos recién creados se procesan después de las hojas con la misma
 * frecuencia.
 */
public class EstructuraArbolHuffman {

    /** Palabra normalizada (mayúsculas y '_') procesada. */
    private final String palabra;

    /** Total de caracteres de la palabra (denominador del cociente). */
    private final int total;

    /** Tabla de frecuencias en orden de MAYOR a menor frecuencia. */
    private final List<FilaHuffman> tabla;

    /** Pasos del proceso completo (análisis, agrupaciones y remate). */
    private final List<PasoHuffman> pasos;

    /** Raíz del árbol de Huffman (null si no se pudo construir). */
    private final NodoHuffman raiz;

    /** Ecuación agrupada final (ej. "(A + (B + C))"). */
    private final String ecuacion;

    /**
     * Construye el árbol de Huffman a partir de la palabra dada. El texto ya
     * debe venir normalizado: solo letras A-Z (mayúsculas) y '_'.
     *
     * @param palabra palabra normalizada a procesar.
     * @throws ClaveInvalidaException si la palabra está vacía o lleva
     *                                caracteres no admitidos.
     */
    public EstructuraArbolHuffman(String palabra) throws ClaveInvalidaException {
        if (palabra == null || palabra.isEmpty()) {
            throw new ClaveInvalidaException(
                    "Digite una palabra para generar el árbol.");
        }
        for (int i = 0; i < palabra.length(); i++) {
            char c = palabra.charAt(i);
            if (!(c >= 'A' && c <= 'Z') && c != '_') {
                throw new ClaveInvalidaException(
                        "Palabra inválida: solo se admiten letras A-Z y el "
                                + "guion bajo '_' para los espacios.");
            }
        }

        this.palabra = palabra;
        this.total = palabra.length();

        // 1) Contar frecuencias conservando el orden de primera aparición.
        List<Entrada> conteos = contar(palabra);

        // 2) Hojas iniciales (frecuencia = cantidad / total), en el orden de
        //    primera aparición (la secuencia del desempate).
        List<NodoHuffman> hojas = new ArrayList<>();
        for (Entrada e : conteos) {
            double frecuencia = (double) e.cantidad / total;
            hojas.add(new NodoHuffman(e.caracter, frecuencia, null, null,
                    String.valueOf(e.caracter), hojas.size()));
        }

        // 3) Agrupar siempre los dos de menor frecuencia (Huffman clásico).
        List<String> pasosTexto = new ArrayList<>();
        List<NodoHuffman> lista = new ArrayList<>(hojas);
        lista.sort(comparador());
        anadirAnalisis(pasosTexto, conteos);
        int siguienteSecuencia = hojas.size();
        int numeroPar = 1;
        while (lista.size() > 1) {
            NodoHuffman a = lista.remove(0);
            NodoHuffman b = lista.remove(0);
            double suma = a.getFrecuencia() + b.getFrecuencia();
            String expresion = "(" + a.getExpresion() + " + "
                    + b.getExpresion() + ")";
            NodoHuffman interno = new NodoHuffman('\0', suma, a, b,
                    expresion, siguienteSecuencia++);
            lista.add(interno);
            lista.sort(comparador());
            pasosTexto.add("Agrupación " + numeroPar
                    + ": se toman los 2 de menor frecuencia, '"
                    + a.getExpresion() + "' (" + textoFrecuencia(a)
                    + ") y '" + b.getExpresion() + "' ("
                    + textoFrecuencia(b) + "), y se agrupan en "
                    + expresion + " con frecuencia " + texto(suma)
                    + ". El nodo se reinserta según su frecuencia.");
            numeroPar++;
        }

        this.raiz = lista.isEmpty() ? null : lista.get(0);
        this.ecuacion = (raiz == null) ? "" : raiz.getExpresion();

        // 4) Códigos de Huffman (izquierda = 0, derecha = 1).
        Map<Character, String> codigos = new LinkedHashMap<>();
        if (raiz != null) {
            asignarCodigos(raiz, "", codigos);
        }

        // 5) Tabla en orden de mayor a menor frecuencia (empates: escrito
        //    primero = más abajo).
        List<NodoHuffman> desc = new ArrayList<>(hojas);
        desc.sort((x, y) -> {
            int c = Double.compare(y.getFrecuencia(), x.getFrecuencia());
            if (c != 0) {
                return c;
            }
            return Integer.compare(y.getSecuencia(), x.getSecuencia());
        });
        this.tabla = new ArrayList<>();
        for (NodoHuffman hoja : desc) {
            String codigo = codigos.getOrDefault(hoja.getCaracter(), "");
            this.tabla.add(new FilaHuffman(hoja.getCaracter(),
                    (int) Math.round(hoja.getFrecuencia() * total),
                    hoja.getFrecuencia(), codigo));
        }

        anadirRemate(pasosTexto, numeroPar, codigos);

        this.pasos = new ArrayList<>();
        for (int i = 0; i < pasosTexto.size(); i++) {
            this.pasos.add(new PasoHuffman(i + 1, pasosTexto.get(i)));
        }
    }

    /** Representación interna de un conteo de frecuencia. */
    private static final class Entrada {
        final char caracter;
        final int cantidad;

        Entrada(char caracter, int cantidad) {
            this.caracter = caracter;
            this.cantidad = cantidad;
        }
    }

    /**
     * Cuenta las apariciones de cada carácter en el orden de primera aparición.
     *
     * @param palabra palabra normalizada.
     * @return lista de conteos en orden de aparición.
     */
    private List<Entrada> contar(String palabra) {
        Map<Character, int[]> mapa = new LinkedHashMap<>();
        for (int i = 0; i < palabra.length(); i++) {
            char c = palabra.charAt(i);
            int[] datos = mapa.get(c);
            if (datos == null) {
                mapa.put(c, new int[] { 0 });
                datos = mapa.get(c);
            }
            datos[0]++;
        }
        List<Entrada> lista = new ArrayList<>();
        for (Map.Entry<Character, int[]> e : mapa.entrySet()) {
            lista.add(new Entrada(e.getKey(), e.getValue()[0]));
        }
        return lista;
    }

    /** Compara nodos por frecuencia ascendente y secuencia de desempate. */
    private Comparator<NodoHuffman> comparador() {
        return (x, y) -> {
            int c = Double.compare(x.getFrecuencia(), y.getFrecuencia());
            if (c != 0) {
                return c;
            }
            return Integer.compare(x.getSecuencia(), y.getSecuencia());
        };
    }

    /** Asigna los códigos de Huffman recorriendo el árbol (0 = izquierda). */
    private void asignarCodigos(NodoHuffman nodo, String prefijo,
            Map<Character, String> codigos) {
        if (nodo.esHoja()) {
            codigos.put(nodo.getCaracter(),
                    prefijo.isEmpty() ? "0" : prefijo);
            return;
        }
        asignarCodigos(nodo.getIzquierda(), prefijo + "0", codigos);
        asignarCodigos(nodo.getDerecha(), prefijo + "1", codigos);
    }

    /** Agrega los pasos de análisis inicial (conteo y orden de la tabla). */
    private void anadirAnalisis(List<String> pasosTexto, List<Entrada> conteos) {
        StringBuilder conteo = new StringBuilder();
        for (int i = 0; i < conteos.size(); i++) {
            if (i > 0) {
                conteo.append(", ");
            }
            Entrada e = conteos.get(i);
            conteo.append(muestra(e.caracter)).append('=').append(e.cantidad);
        }
        pasosTexto.add("Palabra normalizada: '" + palabra + "'. Total de "
                + "caracteres N = " + total + ".");
        pasosTexto.add("Conteo de frecuencias: " + conteo + ".");
        pasosTexto.add("La tabla se ordena de MAYOR a menor frecuencia; en "
                + "caso de empate, el carácter escrito PRIMERO en la palabra "
                + "queda MÁS ABAJO:");
        for (Entrada e : conteos) {
            double frecuencia = (double) e.cantidad / total;
            pasosTexto.add("\t" + muestra(e.caracter) + " → "
                    + e.cantidad + "/" + total + " ("
                    + texto(frecuencia) + ")");
        }
    }

    /** Agrega el remate final: ecuación única y códigos asignados. */
    private void anadirRemate(List<String> pasosTexto, int numeroPar,
            Map<Character, String> codigos) {
        pasosTexto.add("Queda un solo nodo (ECUACIÓN agrupada final): "
                + ecuacion + " = " + texto(raiz.getFrecuencia()) + ".");
        StringBuilder codigoTexto = new StringBuilder();
        for (FilaHuffman fila : tabla) {
            if (codigoTexto.length() > 0) {
                codigoTexto.append(", ");
            }
            codigoTexto.append(muestra(fila.getCaracter())).append('=')
                    .append(fila.getCodigo().isEmpty() ? "—"
                            : fila.getCodigo());
        }
        pasosTexto.add("Códigos de Huffman recorriendo el árbol (izquierda = "
                + "0, derecha = 1): " + codigoTexto + ".");
    }

    /** Muestra un carácter: los espacios '_' se dibujan como "␣". */
    private String muestra(char c) {
        return (c == '_') ? "'_'" : "'" + c + "'";
    }

    /** Texto de una frecuencia con 3 decimales (punto como separador). */
    private String texto(double valor) {
        return String.format(Locale.ROOT, "%.3f", valor);
    }

    /** Texto de frecuencia de un nodo (hoja: además muestra el cociente). */
    private String textoFrecuencia(NodoHuffman nodo) {
        if (nodo.esHoja()) {
            int cantidad = (int) Math.round(nodo.getFrecuencia() * total);
            return cantidad + "/" + total + " = " + texto(nodo.getFrecuencia());
        }
        return texto(nodo.getFrecuencia());
    }

    // ========================================================================
    // CONSULTAS PARA LA VISTA
    // ========================================================================

    /** @return palabra normalizada (mayúsculas y '_') procesada. */
    public String getPalabra() {
        return palabra;
    }

    /** @return total de caracteres de la palabra (denominador). */
    public int getTotal() {
        return total;
    }

    /** @return tabla de frecuencias ordenada de mayor a menor frecuencia. */
    public List<FilaHuffman> getTabla() {
        return tabla;
    }

    /** @return pasos del proceso completo (análisis y agrupaciones). */
    public List<PasoHuffman> getPasos() {
        return pasos;
    }

    /** @return raíz del árbol de Huffman (null si no se pudo construir). */
    public NodoHuffman getRaiz() {
        return raiz;
    }

    /** @return ecuación agrupada final (raíz del árbol). */
    public String getEcuacion() {
        return ecuacion;
    }
}