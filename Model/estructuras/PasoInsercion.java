package Model.estructuras;

/**
 * ============================================================================
 * PASO DE INSERCIÓN (COLISIÓN Y SU RESOLUCIÓN)
 * ============================================================================
 *
 * Objeto inmutable que describe UN momento de la inserción de una clave en
 * una estructura hash ({@link EstructuraHash}), para que la interfaz web
 * pueda mostrar (y ANIMAR) dónde se presentó la colisión y cómo la resuelve
 * la solución elegida, sin volver a ejecutar el algoritmo.
 *
 * Tipos de paso (campo "tipo"):
 *
 *   "directa"   -> la dirección calculada estaba libre; la clave se coloca
 *                  justo ahí (sin colisión).
 *   "colision"  -> la dirección estaba OCUPADA: aquí se presenta la colisión.
 *                  "indice"/"direccion" señalan la dirección y "clave" es el
 *                  valor del ocupante que la bloquea.
 *   "sondeo"    -> intento de sondeo que aterrizó en otra posición OCUPADA
 *                  (prueba lineal, cuadrática o doble dispersión); la
 *                  clave que ocupa esa posición está en "clave".
 *   "desborde"  -> la clave llegó tarde a una dirección y se agregó a su
 *                  CUBETA (encadenamiento o arreglo anidado); "clave" es el
 *                  valor ya encadenado.
 *   "exito"     -> fin de la resolución: la clave ("clave") quedó colocada en
 *                  "indice"/"direccion" (bien en el arreglo principal.
 *
 * RESPONSABILIDAD ÚNICA: transportar un paso de inserción a la vista.
 */
public class PasoInsercion {

    /** Número ordinal del paso dentro de la inserción (1, 2, 3...). */
    private final int numeroPaso;

    /** Tipo de paso: "directa", "colision", "sondeo", "desborde" o "exito". */
    private final String tipo;

    /** Índice físico interno (base 0) involucrado en el paso. */
    private final int indice;

    /** Dirección visible (base 1 = indice + 1) involucrada en el paso. */
    private final int direccion;

    /** Valor numérico protagonista del paso (ocupante o clave insertada). */
    private final int clave;

    /** Explicación en español de lo ocurrido en este paso. */
    private final String descripcion;

    /**
     * @param numeroPaso  número ordinal del paso (1 en adelante).
     * @param tipo        tipo del paso ("directa", "colision", "sondeo",
     *                    "desborde" o "exito").
     * @param indice      índice físico interno (base 0) del paso.
     * @param direccion   dirección visible (base 1).
     * @param clave       valor protagonista del paso.
     * @param descripcion texto explicativo del paso.
     */
    public PasoInsercion(int numeroPaso, String tipo, int indice, int direccion,
            int clave, String descripcion) {
        this.numeroPaso = numeroPaso;
        this.tipo = tipo;
        this.indice = indice;
        this.direccion = direccion;
        this.clave = clave;
        this.descripcion = descripcion;
    }

    /** @return número ordinal del paso. */
    public int getNumeroPaso() {
        return numeroPaso;
    }

    /** @return tipo del paso ("directa", "colision", "sondeo", "desborde", "exito"). */
    public String getTipo() {
        return tipo;
    }

    /** @return índice físico interno (base 0) del paso. */
    public int getIndice() {
        return indice;
    }

    /** @return dirección visible (base 1) del paso. */
    public int getDireccion() {
        return direccion;
    }

    /** @return valor protagonista del paso (ocupante o clave insertada). */
    public int getClave() {
        return clave;
    }

    /** @return explicación textual del paso. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Representación lista para mostrar en consola. */
    @Override
    public String toString() {
        return "Paso " + numeroPaso + " [" + tipo + "]: " + descripcion;
    }
}