package Model.busquedas.externas;

import java.util.ArrayList;
import java.util.List;

import Model.busquedas.PasoBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.estructuras.externas.EstructuraCubetas;

/**
 * ============================================================================
 * BÚSQUEDA EXTERNA BINARIA (SOBRE CUBETAS AGRUPADAS)
 * ============================================================================
 *
 * La búsqueda binaria clásica exige datos ORDENADOS. En la estructura
 * externa por cubetas los datos NO están ordenados por valor: cada cubeta
 * los agrupa por su hash mod. Para conservar la lección didáctica de
 * dividir y conquistar, esta variante ORDENA una copia de TODAS las claves
 * y aplica la binaria clásica sobre ese arreglo plano; cada vez que compara
 * con una clave, localiza en qué CUBETA vive esa clave (para iluminarla en
 * la interfaz) y registra la comparación.
 *
 * Pasos: cada paso apunta a la cubeta donde está alojada la clave comparada;
 * se muestran los límites (flat) del rango vigente del arreglo ordenado.
 */
public class BusquedaExternaBinaria {

    /** Nombre con el que se identifica la estrategia. */
    public static final String NOMBRE = "BINARIA";

    /**
     * Ejecuta la búsqueda binaria sobre una copia ORDENADA de las claves.
     *
     * @param estructura estructura externa de cubetas.
     * @param claveBuscada valor solicitado.
     * @return resultado con pasos y desenlace.
     */
    public ResultadoBusqueda buscar(EstructuraCubetas estructura, int claveBuscada) {
        int[] plano = estructura.obtenerClavesPlanas();
        if (plano.length == 0) {
            return ResultadoBusqueda.fallida(claveBuscada,
                    "No se puede buscar " + claveBuscada
                            + ": la estructura externa está vacía.",
                    new ArrayList<>());
        }

        int[] ordenado = copiarYOrdenar(plano);
        List<PasoBusqueda> pasos = new ArrayList<>();
        int numeroPaso = 0;
        int izquierda = 0;
        int derecha = ordenado.length - 1;

        while (izquierda <= derecha) {
            int medio = (izquierda + derecha) / 2;
            int valorMedio = ordenado[medio];
            int cubeta = estructura.direccionDe(valorMedio);
            numeroPaso++;

            if (valorMedio == claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, cubeta, izquierda, derecha,
                        valorMedio, "¡Coincidencia! El punto medio (valor "
                                + valorMedio + ") vive en la cubeta " + cubeta
                                + " y es igual a la buscada."));
                return ResultadoBusqueda.exitosa(claveBuscada, cubeta, pasos);
            }
            if (valorMedio < claveBuscada) {
                pasos.add(new PasoBusqueda(numeroPaso, cubeta, izquierda, derecha,
                        valorMedio, "Punto medio " + valorMedio + " (cubeta "
                                + cubeta + ") es MENOR que la buscada: se continúa "
                                + "por la derecha."));
                izquierda = medio + 1;
            } else {
                pasos.add(new PasoBusqueda(numeroPaso, cubeta, izquierda, derecha,
                        valorMedio, "Punto medio " + valorMedio + " (cubeta "
                                + cubeta + ") es MAYOR que la buscada: se continúa "
                                + "por la izquierda."));
                derecha = medio - 1;
            }
        }

        return ResultadoBusqueda.fallida(claveBuscada,
                "La clave " + claveBuscada
                        + " NO existe: la búsqueda binaria sobre las " + ordenado.length
                        + " claves ordenadas no la halló.",
                pasos);
    }

    private int[] copiarYOrdenar(int[] plano) {
        int[] copia = new int[plano.length];
        System.arraycopy(plano, 0, copia, 0, plano.length);
        java.util.Arrays.sort(copia);
        return copia;
    }

    /** @return nombre único ("BINARIA"). */
    public String getNombre() {
        return NOMBRE;
    }
}
