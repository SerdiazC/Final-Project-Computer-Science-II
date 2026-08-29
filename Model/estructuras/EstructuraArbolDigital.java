package Model.estructuras;

import java.util.ArrayList;
import java.util.List;

import Model.EstructuraDeDatos;
import Model.excepciones.ClaveDuplicadaException;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;

/**
 * ============================================================================
 * ESTRUCTURA DE ÁRBOL DIGITAL (BÚSQUEDA POR RESIDUOS DIGITAL)
 * ============================================================================
 *
 * Árbol binario donde la UBICACIÓN de cada clave depende de los BITS de su
 * conversión a binario, según el ORDEN DE INSERCIÓN:
 *
 *   1. La clave se transforma inmediatamente a código binario.
 *   2. La PRIMERA clave se coloca en la RAÍZ.
 *   3. Cada clave siguiente desciende desde la raíz leyendo SUS bits uno a
 *      uno (del más significativo al menos significativo):
 *        - bit '0' -> se desplaza a la IZQUIERDA.
 *        - bit '1' -> se desplaza a la DERECHA.
 *   4. Se detiene en el primer espacio libre y ahí se coloca; si el punto
 *      ya está ocupado por otra clave, usa el siguiente bit y continúa.
 *
 * REGLA COMPLEMENTARIA (documentada): si los bits de la clave se agotan
 * mientras sigue descendiendo por nodos ocupados, se continúa con CEROS
 * implícitos (como si la cadena binaria se rellenara con '0') hasta hallar
 * el espacio libre. Con claves únicas esto garantiza inserción siempre
 * posible y sin ambigüedades.
 *
 * CRECIMIENTO LIBRE: esta familia NO usa el tamaño configurado; crece solo
 * con las claves insertadas (tieneTamanoFijo() = false).
 *
 * AL ELIMINAR: el nodo queda VACÍO pero conserva sus hijos, pues la ruta
 * de otras claves puede pasar por él.
 */
public class EstructuraArbolDigital extends EstructuraDeDatos {

    /** Nodo raíz del árbol (null mientras esté vacía la estructura). */
    private NodoArbolDigital raiz;

    /** Orden exacto de llegada de las claves (para listar y migraciones). */
    private final List<Integer> ordenInsercion = new ArrayList<>();

    /**
     * Crea el árbol digital vacío.
     *
     * @param digitosClave dígitos exactos que tendrán las claves (1..7).
     * @throws ExcepcionEstructura si la cifra está fuera del rango 1..7.
     */
    public EstructuraArbolDigital(int digitosClave) throws ExcepcionEstructura {
        super(digitosClave, CAPACIDAD_MAXIMA);
    }

    // ========================================================================
    // REGLAS PROPIAS DEL ÁRBOL
    // ========================================================================

    /**
     * {@inheritDoc} El árbol NO limita su espacio: solo exige rango válido
     * y unicidad.
     */
    @Override
    public void verificarPuedeInsertar(int clave) throws ExcepcionEstructura {
        validarRangoClave(clave);
        if (contieneClave(clave)) {
            throw new ClaveDuplicadaException(
                    "La clave " + clave + " ya existe en la estructura; "
                            + "no se permiten duplicados.");
        }
    }

    /** {@inheritDoc} El árbol crece libremente: nunca se llena. */
    @Override
    public boolean estaLlena() {
        return false;
    }

    /** {@inheritDoc} Familia de crecimiento libre. */
    @Override
    public boolean tieneTamanoFijo() {
        return false;
    }

    // ========================================================================
    // INSERCIÓN GUIADA POR BITS
    // ========================================================================

    /**
     * Inserta la clave siguiendo el guion de la especificación:
     *
     *   FASE 1 - Validación (rango + duplicado).
     *   FASE 2 - Convertir la clave a binario.
     *   FASE 3 - Si no hay raíz, colocar la clave allí.
     *   FASE 4 - Descender leyendo los propios bits ('0' izquierda,
     *            '1' derecha) hasta el primer espacio libre.
     *   FASE 5 - Registrar la clave y contarla.
     */
    @Override
    public void insertar(int clave) throws ExcepcionEstructura {

        // --- FASE 1: reglas de negocio ---------------------------------------
        verificarPuedeInsertar(clave);

        // --- FASE 2: transformación inmediata a binario ------------------------
        String bits = Integer.toBinaryString(clave);

        // --- FASE 3: primera clave -> raíz --------------------------------------
        if (raiz == null) {
            raiz = new NodoArbolDigital(clave);
        } else {
            // --- FASE 4: descenso guiado por los bits ---------------------------
            NodoArbolDigital actual = raiz;
            int indiceBit = 0;
            while (true) {

                // Bit vigente; si se agotaron, se continúa con ceros implícitos.
                char bit = (indiceBit < bits.length())
                        ? bits.charAt(indiceBit)
                        : '0';

                NodoArbolDigital siguiente = (bit == '0')
                        ? actual.getIzquierda()
                        : actual.getDerecha();

                if (siguiente == null) {
                    // Espacio libre encontrado: aquí vive la nueva clave.
                    NodoArbolDigital nuevo = new NodoArbolDigital(clave);
                    if (bit == '0') {
                        actual.setIzquierda(nuevo);
                    } else {
                        actual.setDerecha(nuevo);
                    }
                    break;
                }
                // Punto ocupado por otra clave: usar el siguiente bit.
                actual = siguiente;
                indiceBit++;
            }
        }

        // --- FASE 5: registro ----------------------------------------------------
        ordenInsercion.add(clave);
        cantidad++;
    }

    // ========================================================================
    // LOCALIZACIÓN Y ELIMINACIÓN
    // ========================================================================

    /**
     * Recorre el árbol replicando el camino que la inserción habría seguido
     * para la clave, comparando el valor guardado en cada nodo visitado.
     *
     * @param clave valor buscado.
     * @return nodo que contiene la clave, o null si no está.
     */
    private NodoArbolDigital localizarNodo(int clave) {
        String bits = Integer.toBinaryString(clave);
        NodoArbolDigital actual = raiz;
        int indiceBit = 0;

        while (actual != null) {
            if (!actual.estaVacio() && actual.getClave() == clave) {
                return actual;
            }
            char bit = (indiceBit < bits.length()) ? bits.charAt(indiceBit) : '0';
            actual = (bit == '0') ? actual.getIzquierda() : actual.getDerecha();
            indiceBit++;
        }
        return null;
    }

    /**
     * Elimina la clave vaciando SU nodo (los hijos se conservan porque la
     * ruta de otras claves puede pasar por él).
     *
     * @param clave valor a eliminar.
     * @throws EstructuraVaciaException   si el árbol no tiene datos.
     * @throws ClaveNoEncontradaException si la clave no existe.
     */
    @Override
    public void eliminar(int clave)
            throws EstructuraVaciaException, ClaveNoEncontradaException {

        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "No hay claves para eliminar: la estructura está vacía.");
        }
        NodoArbolDigital nodo = localizarNodo(clave);
        if (nodo == null) {
            throw new ClaveNoEncontradaException(
                    "No se puede eliminar: la clave " + clave
                            + " no existe en la estructura.");
        }
        nodo.setClave(CLAVE_VACIA);
        ordenInsercion.remove(Integer.valueOf(clave));
        cantidad--;
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    /** {@inheritDoc} */
    @Override
    public boolean contieneClave(int clave) {
        return localizarNodo(clave) != null;
    }

    /**
     * {@inheritDoc} Devuelve las claves en su ORDEN DE INSERCIÓN, que es el
     * dato relevante para reconstruir el árbol idéntico en una migración.
     */
    @Override
    public int[] obtenerClaves() {
        int[] resultado = new int[ordenInsercion.size()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = ordenInsercion.get(i);
        }
        return resultado;
    }

    /** @return nodo raíz del árbol (null si está vacío). */
    public NodoArbolDigital getRaiz() {
        return raiz;
    }

    /** @return identificador técnico de este tipo de estructura. */
    @Override
    public String getTipo() {
        return TIPO_RESIDUOS_DIGITAL;
    }
}
