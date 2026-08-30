package DynamicStructures;

import Search.Compare;
import Search.Secuential;
import Search.Binary;

public class DynamicStruct {

    private Bucket[] cubetas;

    private int cantidadRegistros;

    private int capacidadCubeta;

    private Compare busqueda;


    // Límites establecidos en clase, pero pueden ser modificados 
    private final double LIMITE_EXPANSION = 0.75;
    private final double LIMITE_REDUCCION = 1.05;


    // CONSTRUCTOR
    public DynamicStruct(int cantidadCubetas, int capacidadCubeta) {

        this.capacidadCubeta = capacidadCubeta;
        this.cantidadRegistros = 0;

        cubetas = new Bucket[cantidadCubetas];

        for (int i = 0; i < cubetas.length; i++) {

            cubetas[i] = new Bucket(capacidadCubeta);
        }

        // Por defecto usamos búsqueda secuencial
        busqueda = new Secuential();
    }


    // ----------
    // INSERT
    //------------

    public void insertar(int dato) {

        int posicion = obtenerPosicion(dato);

        
        boolean insertado = cubetas[posicion].insertar(dato);

        
        if (!insertado) {

            
            expansionParcial(posicion);

            
            posicion = obtenerPosicion(dato);

            
            cubetas[posicion].insertar(dato);
        }

        cantidadRegistros++;

        
        verificarExpansion();
    }//End insertar method


    // ------------------------------------------------------
    // INSERTING WITHOUT EXPANSION (USED IN TOTAL EXPANSION)
    // ------------------------------------------------------

    private void insertarSinExpansion(int dato) {

        int posicion = obtenerPosicion(dato);

        cubetas[posicion].insertar(dato);
    }//End insertarSinExpansion method


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
    }//End obtenerPosicion method


    // -------
    // REMOVE
    // -------

    public boolean eliminar(int dato) {

        int posicion = obtenerPosicion(dato);

        boolean eliminado = cubetas[posicion].eliminar(dato);

        if (eliminado) {

            cantidadRegistros--;

            verificarReduccion();

            return true;
        }

        return false;
    }//End eliminar method

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
    }//End expansionParcial method


    // ----------------
    // TOTAL EXPANSION
    // ----------------

    private void expansionTotal() {

        
        int[] registros = obtenerRegistros();

        // Aumentamos el número de cubetas
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
    }//End expansionTotal method


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

        // No reducimos si la cubeta ya tiene la capacidad mínima
        if (cubetas[posicion].getCapacidad() > capacidadCubeta) {

            Bucket nuevaCubeta =
                    new Bucket(cubetas[posicion].getCapacidad() / 2);

            int[] datos = cubetas[posicion].getDatos();

            for (int i = 0; i < cantidad; i++) {

                nuevaCubeta.insertar(datos[i]);
            }

            cubetas[posicion] = nuevaCubeta;
        }
    }//End reduccionParcial method


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
    }//End reduccionTotal method


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

    public void setBusqueda(Compare busqueda) {

        this.busqueda = busqueda;
    }


    // ------------------
    // SEQUENTIAL SEARCH
    // ------------------

    public int buscarSecuencial(int numero) {

        int[] registros = obtenerRegistros();

        busqueda = new Secuential();

        return busqueda.comparar(registros, numero);
    }


    // --------------
    // BINARY SEARCH
    // --------------

    public int buscarBinaria(int numero) {

        int[] registros = obtenerRegistros();

        // Binary necesita los datos ordenados
        ordenar(registros);

        busqueda = new Binary();

        return busqueda.comparar(registros, numero);
    }


    // -------------------
    // MAIN SEARCH METHOD
    // -------------------

    public int buscar(int numero) {

        int[] registros = obtenerRegistros();

        // Si estamos usando Binary
        if (busqueda instanceof Binary) {

            ordenar(registros);
        }

        return busqueda.comparar(registros, numero);
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

            System.out.println(
                    "Capacidad: " +
                    cubetas[i].getCapacidad()
            );

            System.out.println(
                    "Cantidad: " +
                    cubetas[i].getCantidad()
            );
        }

        System.out.println("---------------------------------");

        System.out.println(
                "Número de cubetas: " +
                cubetas.length
        );

        System.out.println(
                "Número de registros: " +
                cantidadRegistros
        );

        System.out.println(
                "D.E.: " +
                densidadExpansion() * 100 +
                "%"
        );

        System.out.println(
                "D.R.: " +
                densidadReduccion() * 100 +
                "%"
        );

        System.out.println("=================================");
    }
}//End DynamicStruct class