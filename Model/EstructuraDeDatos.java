package Model;

import java.util.Arrays;

import Model.excepciones.ClaveDuplicadaException;
import Model.excepciones.ClaveInvalidaException;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.ConfiguracionInvalidaException;
import Model.excepciones.EstructuraLlenaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS (CLASE BASE DEL MODELO)
 * ============================================================================
 *
 * Representa el "espacio" donde se almacenan las claves numéricas.
 * Reglas generales del proyecto que esta clase garantiza:
 *
 *  - Tamaño EXACTO elegido por el usuario, entre CAPACIDAD_MINIMA (1) y
 *    CAPACIDAD_MAXIMA (1000) espacios; todas las estructuras comparten la
 *    misma configuración.
 *  - La cantidad de dígitos de las claves se define al crearla
 *    (entre DIGITOS_MINIMOS y DIGITOS_MAXIMOS) y todas las claves deben
 *    respetarla exactamente.
 *  - Las claves son únicas: no se permiten duplicados.
 *  - Los espacios libres se marcan con el centinela CLAVE_VACIA (-1),
 *    de modo que tanto estructuras contiguas como dispersas compartan
 *    el mismo manejo de estado.
 *
 * JERARQUÍA (principio Open/Closed):
 *
 *   EstructuraDeDatos (esta clase: estado y reglas comunes)
 *    ├── EstructuraContigua  -> guarda los datos uno tras otro
 *    │     ├── EstructuraSecuencial (orden de llegada -> búsqueda lineal)
 *    │     └── EstructuraOrdenada   (ascendente permanente -> búsqueda binaria)
 *    └── EstructuraHash      -> guarda por dirección calculada con una
 *                               FuncionHash (transformación de claves)
 *
 * Agregar una estructura nueva solo exige crear otra subclase; nada del
 * código existente se modifica (Open/Closed) y toda subclase es
 * intercambiable donde se espere a esta base (sustitución de Liskov).
 */
public abstract class EstructuraDeDatos {

    /** Mínimo tamaño que puede tener una estructura. */
    public static final int CAPACIDAD_MINIMA = 1;

    /** Máximo tamaño que puede tener cualquier estructura. */
    public static final int CAPACIDAD_MAXIMA = 1000;

    /** Mínima cantidad de dígitos que puede configurar el usuario. */
    public static final int DIGITOS_MINIMOS = 1;

    /** Máxima cantidad de dígitos que puede configurar el usuario. */
    public static final int DIGITOS_MAXIMOS = 7;

    /** Identificador del tipo "secuencial" (orden de llegada). */
    public static final String TIPO_SECUENCIAL = "SECUENCIAL";

    /** Identificador del tipo "ordenada" (ascendente permanente). */
    public static final String TIPO_ORDENADA = "ORDENADA";

    /** Identificador de la estructura de HASH MOD. */
    public static final String TIPO_HASH_MOD = "HASH MOD";

    /** Identificador de la estructura de HASH CUADRADO. */
    public static final String TIPO_HASH_CUADRADO = "HASH CUADRADO";

    /** Identificador de la estructura de HASH TRUNCAMIENTO. */
    public static final String TIPO_HASH_TRUNCAMIENTO = "HASH TRUNCAMIENTO";

    /** Centinela que marca un espacio libre dentro del arreglo. */
    public static final int CLAVE_VACIA = -1;

    // ========================================================================
    // ESTADO INTERNO
    // ========================================================================

    /** Arreglo interno; las posiciones libres contienen CLAVE_VACIA. */
    protected final int[] claves;

    /** Cantidad real de claves almacenadas (espacios ocupados). */
    protected int cantidad;

    /** Dígitos exactos que deben tener todas las claves (1..7). */
    private final int digitosClave;

    /** Tamaño exacto de esta estructura elegido por el usuario (1..1000). */
    private final int capacidad;

    // ========================================================================
    // CONSTRUCCIÓN
    // ========================================================================

    /**
     * Crea la estructura validando toda su configuración inicial.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @param capacidad    tamaño exacto de la estructura (1..1000).
     * @throws ExcepcionEstructura si algún parámetro queda fuera de rango.
     */
    protected EstructuraDeDatos(int digitosClave, int capacidad)
            throws ExcepcionEstructura {

        if (digitosClave < DIGITOS_MINIMOS || digitosClave > DIGITOS_MAXIMOS) {
            throw new ClaveInvalidaException(
                    "La cantidad de dígitos debe estar entre " + DIGITOS_MINIMOS
                            + " y " + DIGITOS_MAXIMOS + ", se recibió: " + digitosClave);
        }
        if (capacidad < CAPACIDAD_MINIMA || capacidad > CAPACIDAD_MAXIMA) {
            throw new ConfiguracionInvalidaException(
                    "El tamaño de la estructura debe estar entre "
                            + CAPACIDAD_MINIMA + " y " + CAPACIDAD_MAXIMA
                            + ", se recibió: " + capacidad);
        }
        this.digitosClave = digitosClave;
        this.capacidad = capacidad;
        this.claves = new int[capacidad];
        Arrays.fill(this.claves, CLAVE_VACIA);
        this.cantidad = 0;
    }

    // ========================================================================
    // OPERACIONES PRINCIPALES (CADA SUBCLASE DEFINE SU FORMA DE GUARDAR)
    // ========================================================================

    /**
     * Inserta una clave en la estructura según la técnica propia de cada
     * familia: contigua (junto a las demás) o dispersa (dirección hash).
     *
     * @param clave valor numérico a almacenar.
     * @throws ExcepcionEstructura si viola alguna regla de negocio.
     */
    public abstract void insertar(int clave) throws ExcepcionEstructura;

    /**
     * Verifica SI LA CLAVE PODRÍA insertarse sin modificar nada. El gestor
     * usa esta fase de "reserva" sobre TODAS las estructuras antes de
     * aplicar cambios, garantizando que o se inserta en todas o en ninguna.
     *
     * @param clave valor que se pretende almacenar.
     * @throws ExcepcionEstructura rango inválido, espacio agotado,
     *                             duplicado o colisión hash, según la
     *                             subclase.
     */
    public void verificarPuedeInsertar(int clave) throws ExcepcionEstructura {
        validarRangoClave(clave);
        if (estaLlena()) {
            throw new EstructuraLlenaException(
                    "La estructura alcanzó su capacidad máxima de "
                            + capacidad + " espacios.");
        }
        if (contieneClave(clave)) {
            throw new ClaveDuplicadaException(
                    "La clave " + clave + " ya existe en la estructura; "
                            + "no se permiten duplicados.");
        }
    }

    /**
     * Elimina la clave indicada dejando su espacio libre.
     *
     * OJO: este recorrido interno es un detalle mecánico de la eliminación,
     * NO sustituye las búsquedas pedagógicas del proyecto (esas viven en
     * Model.busquedas con sus pasos visuales).
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si no hay datos.
     * @throws ClaveNoEncontradaException si la clave no existe.
     */
    public void eliminar(int clave)
            throws EstructuraVaciaException, ClaveNoEncontradaException {

        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "No hay claves para eliminar: la estructura está vacía.");
        }

        int posicion = buscarPosicionInterna(clave);
        if (posicion == -1) {
            throw new ClaveNoEncontradaException(
                    "No se puede eliminar: la clave " + clave
                            + " no existe en la estructura.");
        }

        liberarPosicion(posicion);
        cantidad--;
    }

    /**
     * PASO VARIABLE de la eliminación: las contiguas compactan para no
     * dejar huecos; las dispersas simplemente vacían el espacio.
     *
     * @param posicion índice interno a liberar.
     */
    protected void liberarPosicion(int posicion) {
        claves[posicion] = CLAVE_VACIA;
    }

    // ========================================================================
    // VALIDACIONES Y CONSULTAS DE NEGOCIO
    // ========================================================================

    /**
     * Verifica que la clave tenga EXACTAMENTE la cantidad de dígitos
     * configurada. Como los ceros a la izquierda no cuentan, basta comparar
     * contra el rango numérico correspondiente:
     *
     *   1 dígito  -> [0 .. 9]
     *   4 dígitos -> [1000 .. 9999]
     *   7 dígitos -> [1000000 .. 9999999]
     *
     * @param clave valor a comprobar.
     * @throws ClaveInvalidaException si queda fuera del rango permitido.
     */
    protected void validarRangoClave(int clave) throws ClaveInvalidaException {
        long minimo = obtenerValorMinimoValido();
        long maximo = obtenerValorMaximoValido();
        if (clave < minimo || clave > maximo) {
            throw new ClaveInvalidaException(
                    "La clave " + clave + " no tiene " + digitosClave
                            + " dígito(s). Debe estar entre " + minimo
                            + " y " + maximo + ".");
        }
    }

    /** @return la menor clave aceptable según los dígitos configurados. */
    public long obtenerValorMinimoValido() {
        return (digitosClave == 1) ? 0 : (long) Math.pow(10, digitosClave - 1);
    }

    /** @return la mayor clave aceptable según los dígitos configurados. */
    public long obtenerValorMaximoValido() {
        return (long) Math.pow(10, digitosClave) - 1;
    }

    /**
     * Recorrido interno mínimo para ubicar una clave guardada.
     *
     * @param clave valor buscado.
     * @return índice donde está la clave, o -1 si no aparece.
     */
    private int buscarPosicionInterna(int clave) {
        for (int i = 0; i < capacidad; i++) {
            if (claves[i] != CLAVE_VACIA && claves[i] == clave) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Indica si la clave ya fue almacenada (para rechazar duplicados).
     *
     * @param clave valor a verificar.
     * @return true si ya existe dentro de la estructura.
     */
    public boolean contieneClave(int clave) {
        return buscarPosicionInterna(clave) != -1;
    }

    /**
     * Comprueba si los datos están en orden ascendente. La búsqueda binaria
     * lo usa como salvaguarda antes de operar; ignora espacios vacíos.
     *
     * @return true si cada clave ocupada es menor o igual que la siguiente.
     */
    public boolean estaOrdenadaAscendente() {
        int anterior = CLAVE_VACIA;
        for (int i = 0; i < capacidad; i++) {
            int actual = claves[i];
            if (actual == CLAVE_VACIA) {
                continue;
            }
            if (anterior != CLAVE_VACIA && anterior > actual) {
                return false;
            }
            anterior = actual;
        }
        return true;
    }

    /**
     * Consulta el contenido crudo de una posición interna. Las búsquedas
     * por transformación de claves la usan para comparar la dirección.
     *
     * @param indice índice interno consultado (base 0).
     * @return clave almacenada o {@link #CLAVE_VACIA} si está libre
     *         (o si el índice es inválido).
     */
    public int consultarPosicion(int indice) {
        if (indice < 0 || indice >= capacidad) {
            return CLAVE_VACIA;
        }
        return claves[indice];
    }

    // ========================================================================
    // GETTERS / ESTADO
    // ========================================================================

    /**
     * @return copia densa de las claves ocupadas, en su orden interno
     *         actual (modificar el arreglo devuelto NO afecta la estructura).
     */
    public int[] obtenerClaves() {
        int[] resultado = new int[cantidad];
        int k = 0;
        for (int i = 0; i < capacidad; i++) {
            if (claves[i] != CLAVE_VACIA) {
                resultado[k++] = claves[i];
            }
        }
        return resultado;
    }

    /** @return cantidad de espacios actualmente ocupados (0..capacidad). */
    public int getCantidad() {
        return cantidad;
    }

    /** @return tamaño exacto de esta estructura (elegido por el usuario). */
    public int getCapacidad() {
        return capacidad;
    }

    /** @return dígitos exactos exigidos a las claves (1..7). */
    public int getDigitosClave() {
        return digitosClave;
    }

    /** @return true si no contiene ninguna clave. */
    public boolean estaVacia() {
        return cantidad == 0;
    }

    /** @return true si ya ocupa todos sus espacios disponibles. */
    public boolean estaLlena() {
        return cantidad == capacidad;
    }

    /**
     * @return nombre técnico del tipo concreto de estructura
     *         (constantes TIPO_* de esta clase).
     */
    public abstract String getTipo();

    /** {@inheritDoc} Representación legible: [c1, c2, ...] con las claves. */
    @Override
    public String toString() {
        return getTipo() + Arrays.toString(obtenerClaves());
    }
}
