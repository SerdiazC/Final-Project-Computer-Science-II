package View;

import java.util.List;

/**
 * ============================================================================
 * SERIALIZADOR JSON MÍNIMO (LECTORAS Y ARREGLOS, SIN LIBRERÍAS EXTERNAS)
 * ============================================================================
 *
 * El proyecto usa SOLO librerías estándar de Java, así que la interfaz web
 * necesita construir sus respuestas JSON a mano. Esta clase ofrece las
 * piezas mínimas para escribir JSON seguro (escapar cadenas y serializar
 * arreglos numéricos) sin depender de Jackson/Gson.
 *
 * RESPONSABILIDAD ÚNICA: transformar datos simples de Java a texto JSON.
 * La composición de objetos (clave: valor...) se hace en ServidorWeb.
 */
public final class SerializadorJson {

    /**
     * Constructor privado: es una clase utilitaria y no debe instanciarse.
     */
    private SerializadorJson() {
        // Sin estado; evita instancias accidentales.
    }

    /**
     * Escapa un texto para poder embekerlo dentro de una cadena JSON:
     * añade las comillas exteriores y protege comillas, barras invertidas y
     * caracteres de control con sus secuencias de escape estándar.
     *
     * @param texto texto original (puede ser null).
     * @return literal JSON de cadena: "texto_escapado" o la palabra null.
     */
    public static String cadena(String texto) {
        if (texto == null) {
            return "null";
        }
        StringBuilder salida = new StringBuilder("\"");
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"':
                    salida.append("\\\"");
                    break;
                case '\\':
                    salida.append("\\\\");
                    break;
                case '\n':
                    salida.append("\\n");
                    break;
                case '\r':
                    salida.append("\\r");
                    break;
                case '\t':
                    salida.append("\\t");
                    break;
                case '\b':
                    salida.append("\\b");
                    break;
                case '\f':
                    salida.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        salida.append(String.format("\\u%04x", (int) c));
                    } else {
                        salida.append(c);
                    }
            }
        }
        return salida.append('"').toString();
    }

    /**
     * Serializa un arreglo de enteros como literal JSON de arreglo.
     *
     * @param valores enteros a escribir.
     * @return texto tipo [1, 2, 3]; si es null devuelve null.
     */
    public static String arregloEnteros(int[] valores) {
        if (valores == null) {
            return "null";
        }
        StringBuilder salida = new StringBuilder("[");
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                salida.append(',');
            }
            salida.append(valores[i]);
        }
        return salida.append(']').toString();
    }

    /**
     * Serializa una lista de enteros como literal JSON de arreglo.
     *
     * @param valores lista de enteros a escribir.
     * @return texto tipo [1, 2, 3]; si es null devuelve null.
     */
    public static String arregloEnteros(List<Integer> valores) {
        if (valores == null) {
            return "null";
        }
        StringBuilder salida = new StringBuilder("[");
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                salida.append(',');
            }
            salida.append(valores.get(i));
        }
        return salida.append(']').toString();
    }
}