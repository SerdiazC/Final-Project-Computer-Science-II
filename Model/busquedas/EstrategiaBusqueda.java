package Model.busquedas;

import Model.EstructuraDeDatos;

/**
 * ============================================================================
 * ESTRATEGIA DE BÚSQUEDA (INTERFAZ DEL PATRÓN STRATEGY)
 * ============================================================================
 *
 * Contrato común de TODAS las búsquedas internas del proyecto.
 *
 * PRINCIPIOS SOLID QUE SUSTENTA:
 *
 *  - Segregación de Interfaces (ISP): una sola operación esencial (buscar),
 *    sin métodos que las implementaciones no necesiten.
 *  - Inversión de Dependencias (DIP): el controlador dependerá de esta
 *    abstracción, no de clases concretas como BusquedaLineal.
 *  - Open/Closed (OCP): para agregar una búsqueda nueva (ej. por
 *    interpolación) basta con crear otra clase que implemente esta interfaz
 *    y registrarla; ni la lineal ni la binaria se modifican.
 *
 * Cada estrategia declara además sobre qué tipo de estructura trabaja, de
 * modo que el controlador pueda entregarle los datos correctos.
 */
public interface EstrategiaBusqueda {

    /**
     * Ejecuta la búsqueda sobre la estructura indicada.
     *
     * @param estructura    espacio de datos donde se busca.
     * @param claveBuscada  valor numérico solicitado por el usuario.
     * @return resultado con el desenlace y TODOS los pasos realizados,
     *         listos para ser visualizados.
     */
    ResultadoBusqueda buscar(EstructuraDeDatos estructura, int claveBuscada);

    /**
     * @return nombre único con el que se registra la estrategia
     *         (por ejemplo "LINEAL" o "BINARIA").
     */
    String getNombre();

    /**
     * @return tipo de estructura que requiere esta búsqueda; debe coincidir
     *         con las constantes TIPO_* de {@link EstructuraDeDatos}.
     */
    String getEstructuraRequerida();
}
