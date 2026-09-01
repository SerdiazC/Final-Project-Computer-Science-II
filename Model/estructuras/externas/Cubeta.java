package Model.estructuras.externas;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * CUBETA DE BÚSQUEDA EXTERNA
 * ============================================================================
 *
 * Representa UNA de las cubetas (páginas) de la estructura dinámica de
 * búsqueda externa.
 *
 * Cada cubeta tiene UN SOLO espacio (slot) para su clave principal. Cuando
 * otra clave colisiona en la misma dirección NO se guarda dentro de la
 * cubeta: se ENLAZA en un espacio de memoria encadenado (lista de
 * desbordamiento) que cuelga de la cubeta.
 *
 * Por eso una cubeta nunca almacena 2 claves en su propio espacio: muestra
 * su elemento principal (1/1) y las colisiones aparecen como claves
 * encadenadas que ocupan memoria adicional.
 */
public class Cubeta {

    /** Clave que ocupa el único espacio de la cubeta (null si vacía). */
    private Integer principal;

    /** Claves enlazadas (colisiones) que desbordan la cubeta. */
    private final List<Integer> encadenadas;

    /**
     * Crea una cubeta vacía (sin clave principal).
     */
    public Cubeta() {
        this.principal = null;
        this.encadenadas = new ArrayList<>();
    }

    /**
     * Inserta la clave: si el espacio principal está libre lo ocupa; si ya
     * está ocupado, la clave se ENLAZA (colisión) en el espacio encadenado.
     * Siempre tiene éxito porque el desbordamiento es memoria adicional.
     *
     * @param dato valor a almacenar.
     * @return true (siempre hay lugar vía encadenamiento).
     */
    public boolean insertar(int dato) {
        if (principal == null) {
            principal = dato;
        } else {
            encadenadas.add(dato);
        }
        return true;
    }

    /**
     * Elimina la clave. Si se elimina la principal y hay encadenadas, la
     * primera encadenada pasa a ocupar el espacio principal.
     *
     * @param dato valor a eliminar.
     * @return true si fue eliminada; false si no estaba.
     */
    public boolean eliminar(int dato) {
        if (principal == null) {
            return false;
        }
        if (principal == dato) {
            if (!encadenadas.isEmpty()) {
                principal = encadenadas.remove(0);
            } else {
                principal = null;
            }
            return true;
        }
        return encadenadas.remove(Integer.valueOf(dato));
    }

    /** @return true si la cubeta contiene la clave (principal o enlazada). */
    public boolean contiene(int dato) {
        return (principal != null && principal == dato)
                || encadenadas.contains(dato);
    }

    /** @return copia densa de las claves: principal y luego las enlazadas. */
    public int[] getDatos() {
        int total = getCantidad();
        int[] arreglo = new int[total];
        int idx = 0;
        if (principal != null) {
            arreglo[idx++] = principal;
        }
        for (int d : encadenadas) {
            arreglo[idx++] = d;
        }
        return arreglo;
    }

    /** @return cantidad total de claves (principal + enlazadas). */
    public int getCantidad() {
        return (principal == null ? 0 : 1) + encadenadas.size();
    }

    /** @return capacidad de la cubeta: SIEMPRE un solo espacio (1). */
    public int getCapacidad() {
        return 1;
    }

    /** @return true si el espacio principal ya está ocupado. */
    public boolean estaLlena() {
        return principal != null;
    }
}
