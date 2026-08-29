package Model;

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
 * Reglas generales del proyecto que toda estructura garantiza:
 *
 *  - Claves de dígitos EXACTOS definidos al crearla (entre DIGITOS_MINIMOS
 *    y DIGITOS_MAXIMOS); los ceros a la izquierda no cuentan.
 *  - Claves únicas: no se permiten duplicados.
 *  - Las familias con espacio acotado usan un tamaño elegido por el
 *    usuario entre CAPACIDAD_MINIMA (1) y CAPACIDAD_MAXIMA (1000); las
 *    familias de crecimiento libre (árboles digitales) lo ignoran.
 *
 * JERARQUÍA (principio Open/Closed):
 *
 *   EstructuraDeDatos (esta clase: configuración y reglas comunes)
 *    ├── EstructuraContigua     -> datos uno tras otro en un arreglo
 *    │     ├── EstructuraSecuencial     (orden de llegada -> LINEAL)
 *    │     ├── EstructuraOrdenada       (ascendente -> BINARIA)
 *    │     └── EstructuraHash           (dirección calculada -> MOD/CUADRADO/
 *    │                                    TRUNCAMIENTO/PLEGAMIENTO)
 *    └── EstructuraArbolDigital -> árbol binario guiado por los bits de la
 *                                  clave (RESIDUOS DIGITAL)
 *
 * Agregar una familia nueva solo exige otra subclase; nada del código
 * existente se modifica (Open/Closed) y cualquier subclase sustituye a la
 * base donde se espere (Liskov).
 */
public abstract class EstructuraDeDatos {

    /** Mínimo tamaño que puede tener una estructura acotada. */
    public static final int CAPACIDAD_MINIMA = 1;

    /** Máximo tamaño que puede tener cualquier estructura acotada. */
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

    /** Identificador de la estructura de HASH PLEGAMIENTO. */
    public static final String TIPO_HASH_PLEGAMIENTO = "HASH PLEGAMIENTO";

    /** Identificador de la estructura de RESIDUOS DIGITAL (árbol). */
    public static final String TIPO_RESIDUOS_DIGITAL = "RESIDUOS DIGITAL";

    /** Centinela que marca un espacio/node sin clave asignada. */
    public static final int CLAVE_VACIA = -1;

    // ========================================================================
    // ESTADO COMÚN
    // ========================================================================

    /** Dígitos exactos que deben tener todas las claves (1..7). */
    private final int digitosClave;

    /**
     * Tamaño configurado para estructuras acotadas; las de crecimiento
     * libre lo reciben como techo nominal pero nunca lo alcanzan.
     */
    private final int capacidad;

    /** Cantidad real de claves almacenadas actualmente. */
    protected int cantidad;

    // ========================================================================
    // CONSTRUCCIÓN
    // ========================================================================

    /**
     * Crea la estructura validando toda su configuración inicial.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @param capacidad    tamaño nominal de la estructura (1..1000).
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
        this.cantidad = 0;
    }

    // ========================================================================
    // OPERACIONES PRINCIPALES (CADA FAMILIA DEFINE CÓMO GUARDAR Y HALLAR)
    // ========================================================================

    /**
     * Inserta una clave aplicando la técnica propia de la familia.
     *
     * @param clave valor numérico a almacenar.
     * @throws ExcepcionEstructura si viola alguna regla de negocio.
     */
    public abstract void insertar(int clave) throws ExcepcionEstructura;

    /**
     * Elimina la clave indicada.
     *
     * OJO: este recorrido interno es un detalle mecánico de la eliminación,
     * NO sustituye las búsquedas pedagógicas del proyecto (esas viven en
     * Model.busquedas con sus pasos visualizables).
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si no hay datos.
     * @throws ClaveNoEncontradaException si la clave no existe.
     */
    public abstract void eliminar(int clave)
            throws EstructuraVaciaException,
            ClaveNoEncontradaException;

    /**
     * Indica si la clave ya fue almacenada (para rechazar duplicados).
     *
     * @param clave valor a verificar.
     * @return true si ya existe dentro de la estructura.
     */
    public abstract boolean contieneClave(int clave);

    /**
     * @return copia densa de las claves ocupadas, en el orden interno
     *         natural de la estructura (modificar el arreglo devuelto NO
     *         afecta la estructura).
     */
    public abstract int[] obtenerClaves();

    /**
     * Verifica SI LA CLAVE PODRÍA insertarse sin modificar nada. La vista o
     * el gestor pueden usarla como fase de reserva antes de aplicar cambios.
     *
     * @param clave valor que se pretende almacenar.
     * @throws ExcepcionEstructura rango inválido, espacio agotado o
     *                             duplicado, según la subclase.
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
     * Consulta el contenido crudo de una posición interna indexada. Solo
     * las familias direccionables (arreglos) devuelven datos reales; el
     * resto reporta espacio vacío.
     *
     * @param indice índice interno consultado (base 0).
     * @return clave almacenada o {@link #CLAVE_VACIA}.
     */
    public int consultarPosicion(int indice) {
        return CLAVE_VACIA;
    }

    /**
     * Comprueba si los datos están en orden ascendente. Solo tiene sentido
     * en la estructura ordenada; por defecto las familias no lo garantizan.
     *
     * @return true si cada clave ocupada es menor o igual que la siguiente.
     */
    public boolean estaOrdenadaAscendente() {
        return false;
    }

    /**
     * @return true si la familia opera dentro del tamaño configurado;
     *         false para estructuras de crecimiento libre (árboles).
     */
    public boolean tieneTamanoFijo() {
        return true;
    }

    // ========================================================================
    // GETTERS / ESTADO
    // ========================================================================

    /** @return cantidad de claves almacenadas actualmente. */
    public int getCantidad() {
        return cantidad;
    }

    /** @return tamaño configurado de la estructura. */
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

    /** @return true si agotó sus espacios disponibles (familias acotadas). */
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
        StringBuilder texto = new StringBuilder(getTipo()).append("[");
        int[] claves = obtenerClaves();
        for (int i = 0; i < claves.length; i++) {
            if (i > 0) {
                texto.append(", ");
            }
            texto.append(claves[i]);
        }
        return texto.append("]").toString();
    }
}
