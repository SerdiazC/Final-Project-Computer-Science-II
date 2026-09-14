package Model.estructuras_dinámicas;

import Model.EstructuraDeDatos;
import Model.busquedas.BusquedaBinaria;
import Model.busquedas.BusquedaLineal;
import Model.busquedas.EstrategiaBusqueda;
import Model.busquedas.PasoBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.excepciones.ClaveNoEncontradaException;
import Model.excepciones.ConfiguracionInvalidaException;
import Model.excepciones.EstructuraVaciaException;
import Model.excepciones.ExcepcionEstructura;

import java.util.ArrayList;
import java.util.List;

public class DynamicStruct extends EstructuraDeDatos {

    private Bucket[] cubetas;

    private int cantidadRegistros;

    private int capacidadCubeta;

    private EstrategiaBusqueda busqueda;

    private Index[] indicePrimario;

    private SecondaryIndex[] indiceSecundario;

    private MultiLevelIndex[] indiceMultiNivel;

    // Límites establecidos en clase, pero pueden ser modificados 
    private final double LIMITE_EXPANSION = 0.75;
    private final double LIMITE_REDUCCION = 1.05;


   
    public DynamicStruct(int cantidadCubetas, int capacidadCubeta) throws ExcepcionEstructura {
        this(cantidadCubetas, capacidadCubeta, 4);
    }

    public DynamicStruct(int cantidadCubetas, int capacidadCubeta, int digitos)
            throws ExcepcionEstructura {
        super(digitos, 1000); // Pasa los parámetros que exige la clase padre

        if (cantidadCubetas < 1) {
            throw new ConfiguracionInvalidaException(
                    "El número de cubetas debe ser al menos 1, se recibió: "
                            + cantidadCubetas);
        }
        if (capacidadCubeta < 1) {
            throw new ConfiguracionInvalidaException(
                    "La capacidad de cada cubeta debe ser al menos 1, se recibió: "
                            + capacidadCubeta);
        }

        this.capacidadCubeta = capacidadCubeta;
        this.cantidadRegistros = 0;

        cubetas = new Bucket[cantidadCubetas];

        for (int i = 0; i < cubetas.length; i++) {
            cubetas[i] = new Bucket(capacidadCubeta);
        }

        // Por defecto usamos búsqueda secuencial
        busqueda = new BusquedaLineal();
    }


    // ----------
    // INSERT
    //------------

    public void insertar(int dato) throws ExcepcionEstructura {
        verificarPuedeInsertar(dato);

        int posicion = obtenerPosicion(dato);

        boolean insertado = cubetas[posicion].insertar(dato);

        if (!insertado) {
            expansionParcial(posicion);

            posicion = obtenerPosicion(dato);

            cubetas[posicion].insertar(dato);
        }

        cantidadRegistros++;

        verificarExpansion();

        actualizarIndices();
    }


    // ------------------------------------------------------
    // INSERTING WITHOUT EXPANSION (USED IN TOTAL EXPANSION)
    // ------------------------------------------------------

    private void insertarSinExpansion(int dato) {
        int posicion = obtenerPosicion(dato);

        cubetas[posicion].insertar(dato);
    }


    // ------
    // HASH
    // ------

    private int obtenerPosicion(int dato) {
        int posicion = dato % cubetas.length;

        // Si el dato es negativo
        if (posicion < 0) {
            posicion = posicion * -1;
        }

        return posicion;
    }

    public boolean eliminarDato(int dato) {
        int posicion = obtenerPosicion(dato);

        boolean eliminado = cubetas[posicion].eliminar(dato);

        if (eliminado) {
            cantidadRegistros--;

            verificarReduccion();

            actualizarIndices();

            return true;
        }

        return false;
    }

    public double densidadExpansion() {
        int tamaño = cubetas.length * capacidadCubeta;

        return (double) cantidadRegistros / tamaño;
    }

    public double densidadReduccion() {
        return (double) cantidadRegistros / cubetas.length;
    }


    // -----------------------
    // EXPANSION VERIFICATION
    // -----------------------

    private void verificarExpansion() {
        double densidad = densidadExpansion();

        if (densidad >= LIMITE_EXPANSION) {
            expansionTotal();
        }
    }


    // ------------------
    // PARTIAL EXPANSION
    // ------------------

    private void expansionParcial(int posicion) {
        int cantidad = cubetas[posicion].getCantidad();

        int[] datosAntiguos = cubetas[posicion].getDatos();

        Bucket nuevaCubeta = new Bucket(capacidadCubeta * 2);

        for (int i = 0; i < cantidad; i++) {
            nuevaCubeta.insertar(datosAntiguos[i]);
        }

        cubetas[posicion] = nuevaCubeta;
    }


    // ----------------
    // TOTAL EXPANSION
    // ----------------

    private void expansionTotal() {
        int[] registros = obtenerRegistros();

        int nuevaCantidad = cubetas.length * 2;

        cubetas = new Bucket[nuevaCantidad];

        for (int i = 0; i < cubetas.length; i++) {
            cubetas[i] = new Bucket(capacidadCubeta);
        }

        cantidadRegistros = 0;

        for (int i = 0; i < registros.length; i++) {
            insertarSinExpansion(registros[i]);

            cantidadRegistros++;
        }
    }


    // -----------------------
    // REDUCTION VERIFICATION
    // -----------------------

    private void verificarReduccion() {
        double densidad = densidadReduccion();

        if (densidad <= LIMITE_REDUCCION) {
            for (int i = 0; i < cubetas.length; i++) {
                if (cubetas[i].getCapacidad() > capacidadCubeta) {
                    reduccionParcial(i);

                    return;
                }
            }

            if (cubetas.length > 1) {
                reduccionTotal();
            }
        }
    }


    // ------------------
    // PARTIAL REDUCTION
    // ------------------

    private void reduccionParcial(int posicion) {
        int cantidad = cubetas[posicion].getCantidad();

        if (cubetas[posicion].getCapacidad() > capacidadCubeta) {
            Bucket nuevaCubeta = new Bucket(cubetas[posicion].getCapacidad() / 2);

            int[] datos = cubetas[posicion].getDatos();

            for (int i = 0; i < cantidad; i++) {
                nuevaCubeta.insertar(datos[i]);
            }

            cubetas[posicion] = nuevaCubeta;
        }
    }


    // ----------------
    // TOTAL REDUCTION
    // ----------------

    private void reduccionTotal() {
        if (cubetas.length <= 1) {
            return;
        }

        int[] registros = obtenerRegistros();

        int nuevaCantidad = cubetas.length / 2;

        if (nuevaCantidad < 1) {
            nuevaCantidad = 1;
        }

        cubetas = new Bucket[nuevaCantidad];

        for (int i = 0; i < cubetas.length; i++) {
            cubetas[i] = new Bucket(capacidadCubeta);
        }

        cantidadRegistros = 0;

        for (int i = 0; i < registros.length; i++) {
            insertarSinExpansion(registros[i]);

            cantidadRegistros++;
        }
    }


    // ------------------------
    // GET RECORDS (REGISTROS)
    // ------------------------

    private int[] obtenerRegistros() {
        int[] registros = new int[cantidadRegistros];

        int posicion = 0;

        for (int i = 0; i < cubetas.length; i++) {
            int[] datos = cubetas[i].getDatos();

            for (int j = 0; j < cubetas[i].getCantidad(); j++) {
                registros[posicion] = datos[j];

                posicion++;
            }
        }

        return registros;
    }


    // ---------------------
    // CHANGE SEARCH METHOD
    // ---------------------

    public void setBusqueda(EstrategiaBusqueda busqueda) {
        this.busqueda = busqueda;
    }


    // ------------------
    // SEQUENTIAL SEARCH
    // ------------------
    public int buscarSecuencial(int numero) {
        int[] registros = obtenerRegistros();
        
        for (int i = 0; i < registros.length; i++) {
            if (registros[i] == numero) {
                return registros[i];
            }
        }
        return -1;
    }


    // --------------
    // BINARY SEARCH
    // --------------

    public int buscarBinaria(int numero) {
        int[] registros = obtenerRegistros();
        ordenar(registros);
        
        int inicio = 0;
        int fin = registros.length - 1;
        
        while (inicio <= fin) {
            int medio = inicio + (fin - inicio) / 2;
            
            if (registros[medio] == numero) {
                return registros[medio];
            }
            if (registros[medio] < numero) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return -1;
    }


    // -------------------
    // MAIN SEARCH METHOD
    // -------------------

    public int buscar(int numero) {
        return buscarSecuencial(numero);
    }   


    // ------------
    // ARRANGEMENT
    // ------------

    private void ordenar(int[] datos) {
        for (int i = 0; i < datos.length - 1; i++) {
            for (int j = 0; j < datos.length - 1 - i; j++) {
                if (datos[j] > datos[j + 1]) {
                    int aux = datos[j];
                    datos[j] = datos[j + 1];
                    datos[j + 1] = aux;
                }
            }
        }
    }


    // ------------
    // SHOW STRUCT
    // ------------

    public void mostrar() {
        System.out.println("\n=================================");
        System.out.println("      ESTRUCTURA DINÁMICA");
        System.out.println("=================================");

        for (int i = 0; i < cubetas.length; i++) {
            System.out.print("Cubeta " + i + ": ");

            int[] datos = cubetas[i].getDatos();

            for (int j = 0; j < cubetas[i].getCantidad(); j++) {
                System.out.print(datos[j] + " ");
            }

            System.out.println();
            System.out.println("Capacidad: " + cubetas[i].getCapacidad());
            System.out.println("Cantidad: " + cubetas[i].getCantidad());
        }

        System.out.println("---------------------------------");
        System.out.println("Número de cubetas: " + cubetas.length);
        System.out.println("Número de registros: " + cantidadRegistros);
        System.out.println("=================================");
    }


    // ---------------------------------------------------------
    // MÉTODOS OBLIGATORIOS HEREDADOS DE EstructuraDeDatos
    // ---------------------------------------------------------

    @Override
    public int[] obtenerClaves() {
        return obtenerRegistros();
    }

    @Override
    public boolean contieneClave(int clave) {
        return buscar(clave) != -1;
    }


    // ------------
    // PRIMARY INDEX CREATION
    // ------------

    private void crearIndicePrimario() {
        indicePrimario = new Index[cubetas.length];

        int cantidad = 0;

        for (int i = 0; i < cubetas.length; i++) {
            if (cubetas[i].getCantidad() > 0) {
                int[] datos = cubetas[i].getDatos();
                int primeraClave = datos[0];

                indicePrimario[cantidad] = new Index(primeraClave, i);
                cantidad++;
            }
        }
    }

    public int buscarPrimario(int numero) {
        int bloque = -1;

        if (indicePrimario == null) return -1;

        for (int i = 0; i < indicePrimario.length; i++) {
            if (indicePrimario[i] == null) {
                break;
            }

            if (numero >= indicePrimario[i].getKey()) {
                bloque = indicePrimario[i].getBucket();
            }
        }

        if (bloque == -1) {
            return -1;
        }

        int[] datos = cubetas[bloque].getDatos();

        for (int i = 0; i < cubetas[bloque].getCantidad(); i++) {
            if (datos[i] == numero) {
                return datos[i];
            }
        }

        return -1;
    }


    // ------------
    // SECONDARY INDEX CREATION & SEARCH
    // ------------

    private void crearIndiceSecundario() {
        int total = cantidadRegistros;

        indiceSecundario = new SecondaryIndex[total];

        int posicion = 0;

        for (int i = 0; i < cubetas.length; i++) {
            int[] datos = cubetas[i].getDatos();

            for (int j = 0; j < cubetas[i].getCantidad(); j++) {
                indiceSecundario[posicion] = new SecondaryIndex(datos[j], i, j);
                posicion++;
            }
        }
    }

    public int buscarSecundario(int numero) {
        if (indiceSecundario == null) return -1;

        for (int i = 0; i < indiceSecundario.length; i++) {
            if (indiceSecundario[i] != null && indiceSecundario[i].getKey() == numero) {
                int bucket = indiceSecundario[i].getBucket();
                int position = indiceSecundario[i].getPosition();

                return cubetas[bucket].getDatos()[position];
            }
        }

        return -1;
    }


    // ------------
    // MULTI-LEVEL INDEX CREATION & SEARCH
    // ------------

    private void crearIndiceMultinivel() {
        if (indicePrimario == null) return;

        int cantidad = 0;

        for (int i = 0; i < indicePrimario.length; i++) {
            if (indicePrimario[i] != null) {
                cantidad++;
            }
        }

        int cantidadSuperior = (cantidad + 1) / 2;

        indiceMultiNivel = new MultiLevelIndex[cantidadSuperior];

        int posicion = 0;

        for (int i = 0; i < cantidad; i += 2) {
            if (indicePrimario[i] != null) {
                indiceMultiNivel[posicion] = new MultiLevelIndex(indicePrimario[i].getKey(), i);
                posicion++;
            }
        }
    }

    public int buscarMultinivel(int numero) {
        if (indiceMultiNivel == null || indicePrimario == null) return -1;

        int posicionIndice = -1;

        for (int i = 0; i < indiceMultiNivel.length; i++) {
            if (indiceMultiNivel[i] == null) {
                break;
            }

            if (numero >= indiceMultiNivel[i].getKey()) {
                posicionIndice = indiceMultiNivel[i].getIndexPosition();
            }
        }

        if (posicionIndice == -1 || indicePrimario[posicionIndice] == null) {
            return -1;
        }

        int bloque = indicePrimario[posicionIndice].getBucket();

        int[] datos = cubetas[bloque].getDatos();

        for (int i = 0; i < cubetas[bloque].getCantidad(); i++) {
            if (datos[i] == numero) {
                return datos[i];
            }
        }

        return -1;
    }
    
    // ------------
    // UPDATE INDEXES
    // ------------

    private void actualizarIndices() {
        crearIndicePrimario();
        crearIndiceSecundario();
        crearIndiceMultinivel();
    }


    @Override
    public void eliminar(int clave) throws EstructuraVaciaException, ClaveNoEncontradaException {
        if (cantidadRegistros == 0) {
            throw new EstructuraVaciaException(
                    "La estructura no contiene registros; no hay nada que eliminar.");
        }
        if (!eliminarDato(clave)) {
            throw new ClaveNoEncontradaException(
                    "La clave " + clave + " no existe en la estructura.");
        }
    }


    // ------------
    // BÚSQUEDAS POR ÍNDICE CON PASOS VISUALIZABLES
    // ------------

    /**
     * Búsqueda por índice PRIMARIO registrando cada comparación: primero
     * contra las claves del índice (¿clave buscada >= clave del índice?),
     * luego dentro de la cubeta señalada.
     *
     * @param numero clave solicitada.
     * @return resultado con pasos para la interfaz.
     */
    public ResultadoBusqueda buscarPrimarioConPasos(int numero) {
        List<PasoBusqueda> pasos = new ArrayList<>();
        int numPaso = 0;
        int bloque = -1;

        if (indicePrimario != null) {
            for (int i = 0; i < indicePrimario.length; i++) {
                Index entrada = indicePrimario[i];
                if (entrada == null) {
                    break;
                }
                numPaso++;
                boolean mayorIgual = numero >= entrada.getKey();
                pasos.add(new PasoBusqueda(numPaso, i, -1, -1, entrada.getKey(),
                        "Índice primario, entrada " + (i + 1) + ": ¿" + numero
                                + " >= " + entrada.getKey() + "? "
                                + (mayorIgual ? "Sí, queda señalada la cubeta "
                                        + entrada.getBucket() + "."
                                        : "No, el índice ya rebasó la clave buscada.")));
                if (mayorIgual) {
                    bloque = entrada.getBucket();
                } else {
                    break;
                }
            }
        }

        if (bloque == -1) {
            return ResultadoBusqueda.fallida(numero,
                    "La clave " + numero + " es menor que toda entrada del índice "
                            + "primario; no puede estar en la estructura.", pasos);
        }

        int[] datos = cubetas[bloque].getDatos();
        for (int i = 0; i < cubetas[bloque].getCantidad(); i++) {
            numPaso++;
            boolean igual = datos[i] == numero;
            pasos.add(new PasoBusqueda(numPaso, bloque, -1, -1, datos[i],
                    "Dentro de la cubeta " + bloque + ", posición " + (i + 1)
                            + ": se compara " + datos[i] + " con " + numero + " "
                            + (igual ? "-> ¡Encontrada!" : "-> no coincide, sigue.")));
            if (igual) {
                return ResultadoBusqueda.exitosa(numero, bloque, pasos);
            }
        }

        return ResultadoBusqueda.fallida(numero,
                "La clave " + numero + " no aparece dentro de la cubeta " + bloque
                        + " que señaló el índice primario.", pasos);
    }

    /**
     * Búsqueda por índice SECUNDARIO (lista invertida: cada clave apunta a su
     * cubeta y posición). Registra el recorrido de la lista secundaria.
     *
     * @param numero clave solicitada.
     * @return resultado con pasos para la interfaz.
     */
    public ResultadoBusqueda buscarSecundarioConPasos(int numero) {
        List<PasoBusqueda> pasos = new ArrayList<>();
        int numPaso = 0;

        if (indiceSecundario != null) {
            for (int i = 0; i < indiceSecundario.length; i++) {
                SecondaryIndex entrada = indiceSecundario[i];
                if (entrada == null) {
                    break;
                }
                numPaso++;
                boolean igual = entrada.getKey() == numero;
                pasos.add(new PasoBusqueda(numPaso, entrada.getBucket(),
                        -1, -1, entrada.getKey(),
                        "Índice secundario, elemento " + (i + 1) + ": clave "
                                + entrada.getKey() + " -> (cubeta "
                                + entrada.getBucket() + ", posición "
                                + entrada.getPosition() + "). "
                                + (igual ? "¡Coincide con la buscada!"
                                        : "No coincide, sigue.")));
                if (igual) {
                    return ResultadoBusqueda.exitosa(numero,
                            entrada.getBucket(), pasos);
                }
            }
        }

        return ResultadoBusqueda.fallida(numero,
                "La clave " + numero + " no figura en el índice secundario.", pasos);
    }

    /**
     * Búsqueda por índice MULTINIVEL: se recorre el nivel superior (resumen
     * del primario), se desciende a la entrada del índice primario señalada y
     * finalmente a la cubeta.
     *
     * @param numero clave solicitada.
     * @return resultado con pasos para la interfaz.
     */
    public ResultadoBusqueda buscarMultinivelConPasos(int numero) {
        List<PasoBusqueda> pasos = new ArrayList<>();
        int numPaso = 0;
        int posicionIndice = -1;

        if (indiceMultiNivel != null) {
            for (int i = 0; i < indiceMultiNivel.length; i++) {
                MultiLevelIndex entrada = indiceMultiNivel[i];
                if (entrada == null) {
                    break;
                }
                numPaso++;
                boolean mayorIgual = numero >= entrada.getKey();
                pasos.add(new PasoBusqueda(numPaso, -1, -1, -1, entrada.getKey(),
                        "Índice multinivel, entrada " + (i + 1) + ": ¿" + numero
                                + " >= " + entrada.getKey() + "? "
                                + (mayorIgual ? "Sí, desciende a la posición "
                                        + (entrada.getIndexPosition() + 1)
                                        + " del índice primario."
                                        : "No, el nivel superior ya rebasó la clave.")));
                if (mayorIgual) {
                    posicionIndice = entrada.getIndexPosition();
                } else {
                    break;
                }
            }
        }

        if (posicionIndice == -1 || indicePrimario == null
                || indicePrimario[posicionIndice] == null) {
            return ResultadoBusqueda.fallida(numero,
                    "La clave " + numero + " no queda cubierta por ninguna entrada "
                            + "del índice multinivel.", pasos);
        }

        numPaso++;
        int bloque = indicePrimario[posicionIndice].getBucket();
        pasos.add(new PasoBusqueda(numPaso, bloque, -1, -1,
                indicePrimario[posicionIndice].getKey(),
                "Índice primario, posición " + (posicionIndice + 1) + ": clave "
                        + indicePrimario[posicionIndice].getKey() + " -> cubeta "
                        + bloque + "."));

        int[] datos = cubetas[bloque].getDatos();
        for (int i = 0; i < cubetas[bloque].getCantidad(); i++) {
            numPaso++;
            boolean igual = datos[i] == numero;
            pasos.add(new PasoBusqueda(numPaso, bloque, -1, -1, datos[i],
                    "Dentro de la cubeta " + bloque + ", posición " + (i + 1)
                            + ": se compara " + datos[i] + " con " + numero + " "
                            + (igual ? "-> ¡Encontrada!" : "-> no coincide, sigue.")));
            if (igual) {
                return ResultadoBusqueda.exitosa(numero, bloque, pasos);
            }
        }

        return ResultadoBusqueda.fallida(numero,
                "La clave " + numero + " no aparece dentro de la cubeta " + bloque
                        + " señalada por el índice multinivel.", pasos);
    }


    // ------------
    // CONSULTAS DE ESTADO PARA LA VISTA
    // ------------

    /** @return arreglo interno de cubetas (solo lectura). */
    public Bucket[] getCubetas() {
        return cubetas;
    }

    /** @return cantidad actual de cubetas del archivo. */
    public int getNumeroCubetas() {
        return cubetas.length;
    }

    /** @return capacidad base (inicial) configurada para cada cubeta. */
    public int getCapacidadCubeta() {
        return capacidadCubeta;
    }

    /** @return cantidad real de registros almacenados. */
    public int getCantidadRegistros() {
        return cantidadRegistros;
    }

    /** @return índice primario vigente (puede ser null sin registros). */
    public Index[] getIndicePrimario() {
        return indicePrimario;
    }

    /** @return índice secundario vigente (puede ser null sin registros). */
    public SecondaryIndex[] getIndiceSecundario() {
        return indiceSecundario;
    }

    /** @return índice multinivel vigente (puede ser null sin registros). */
    public MultiLevelIndex[] getIndiceMultiNivel() {
        return indiceMultiNivel;
    }


    // ---------------------------------------------------------
    // SOBREESCRITURAS DE ESTADO (LA FAMILIA ES DE CRECIMIENTO LIBRE)
    // ---------------------------------------------------------

    /** @return cantidad real de registros (no la del padre, que queda en 0). */
    @Override
    public int getCantidad() {
        return cantidadRegistros;
    }

    /** @return true solo si no hay ningún registro almacenado. */
    @Override
    public boolean estaVacia() {
        return cantidadRegistros == 0;
    }

    /** Los índices crecen libremente al llenarse cubetas: nunca está "llena". */
    @Override
    public boolean estaLlena() {
        return false;
    }

    /** @return false: esta familia no opera dentro de un tamaño fijo. */
    @Override
    public boolean tieneTamanoFijo() {
        return false;
    }

    /** @return nombre técnico de la familia de índices dinámicos. */
    @Override
    public String getTipo() {
        return "DINAMICA";
    }
}