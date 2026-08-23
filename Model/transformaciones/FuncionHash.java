package Model.transformaciones;

/**
 * ============================================================================
 * FUNCIÓN HASH (INTERFAZ DEL PATRÓN STRATEGY PARA TRANSFORMACIÓN DE CLAVES)
 * ============================================================================
 *
 * Contrato común de las funciones de transformación de claves del proyecto.
 *
 * REGLA GENERAL ACORDADA: el valor devuelto por calcularDireccion SIEMPRE
 * queda dentro del rango [1 .. tamanoEstructura], porque el último paso de
 * toda función es "módulo del tamaño" y después "+1".
 *
 * PRINCIPIOS SOLID:
 *  - ISP: solo lo esencial (calcular la dirección y describirla).
 *  - OCP: agregar una función hash nueva = crear otra implementación y
 *    registrarla; nada del código existente cambia.
 *  - DIP: EstructuraHash depende de esta interfaz, no de clases concretas.
 */
public interface FuncionHash {

    /**
     * Calcula la dirección (posición 1-based) donde vive la clave.
     *
     * @param clave             valor numérico a transformar.
     * @param tamanoEstructura  tamaño total de la estructura destino.
     * @return dirección resultante, siempre entre 1 y tamanoEstructura.
     */
    int calcularDireccion(int clave, int tamanoEstructura);

    /**
     * @return texto explicativo del cálculo paso a paso (para mostrar al
     *         usuario y para los pasos visualizables de la búsqueda).
     */
    String describirCalculo(int clave, int tamanoEstructura);

    /**
     * @return nombre único de la función; coincide con el tipo de la
     *         estructura que la utiliza (constantes TIPO_HASH_* de
     *         {@link Model.EstructuraDeDatos}).
     */
    String getNombre();

    /**
     * Utilidad compartida: cantidad de dígitos decimales de un número.
     *
     * @param valor número positivo a medir.
     * @return cantidad de cifras que lo componen.
     */
    default int contarDigitos(int valor) {
        return Integer.toString(valor).length();
    }

    /**
     * Utilidad compartida: cierre estándar de TODAS las funciones del
     * proyecto: se ajusta al tamaño con módulo y AL FINAL se suma 1.
     *
     * @param valorIntermedio  número obtenido antes del cierre.
     * @param tamanoEstructura tamaño total de la estructura.
     * @return dirección final válida en [1 .. tamanoEstructura].
     */
    default int cerrarCalculo(int valorIntermedio, int tamanoEstructura) {
        return (valorIntermedio % tamanoEstructura) + 1;
    }
}
