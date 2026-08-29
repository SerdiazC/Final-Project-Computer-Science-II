package View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import Controller.GestorBusquedas;
import Model.EstructuraDeDatos;
import Model.busquedas.ResultadoBusqueda;
import Model.estructuras.EstructuraArbolDigital;
import Model.estructuras.EstructuraHash;
import Model.estructuras.NodoArbolDigital;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;

/**
 * ============================================================================
 * DEMO DE CONSOLA (VISTA TEMPORAL)
 * ============================================================================
 *
 * ATENCIÓN: esta vista es PROVISIONAL. El entregable final será una página
 * web local; cuando exista, este archivo se reemplazará por el cliente web.
 * Se construyó sobre BufferedReader (solo librerías estándar de Java) para
 * poder probar la lógica completa desde la terminal.
 *
 * FLUJO DEL DIÁLOGO:
 *   1. Configurar dígitos de las claves y tamaño de la estructura.
 *   2. Elegir UN método de búsqueda activo (su estructura queda vacía).
 *   3. Operar: insertar, listar, buscar y eliminar sobre esa estructura.
 *   4. "Cambiar de búsqueda": elige otro método y decide si conserva las
 *      claves (migración) o reinicia la estructura vacía.
 *
 * RESPONSABILIDAD ÚNICA: dialogar con el usuario (leer entradas, mostrar
 * resultados y pasos). Toda decisión de negocio vive en el modelo y toda
 * coordinación en el controlador GestorBusquedas.
 *
 * DETALLE Open/Closed: el menú de métodos se GENERA a partir del catálogo
 * registrado en el controlador; agregar una búsqueda nueva no exige tocar
 * esta vista.
 */
public class DemoConsola {

    /** Lector de líneas del teclado (librería estándar java.io). */
    private final BufferedReader entrada = new BufferedReader(
            new InputStreamReader(System.in));

    /** Controlador que orquesta estructuras y búsquedas. */
    private GestorBusquedas gestor;

    // ========================================================================
    // CICLO PRINCIPAL DE LA APLICACIÓN
    // ========================================================================

    /**
     * Punto de ejecución: configura los parámetros, pide el método de
     * búsqueda inicial y despliega el menú operativo hasta salir.
     */
    public void ejecutar() {
        System.out.println("=====================================================");
        System.out.println("  BUSQUEDAS INTERNAS - LINEAL, BINARIA, HASH Y");
        System.out.println("  RESIDUOS DIGITAL (arbol binario por bits)");
        System.out.println("  Tamano configurable: "
                + EstructuraDeDatos.CAPACIDAD_MINIMA + "-"
                + EstructuraDeDatos.CAPACIDAD_MAXIMA + " espacios");
        System.out.println("=====================================================");

        configurarParametros();
        seleccionarMetodo();

        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            int opcion = leerEntero("Seleccione una opcion: ");
            continuar = procesarOpcion(opcion);
        }
    }

    /**
     * Ejecuta la opción elegida y decide si el programa continúa.
     *
     * @param opcion número digitado por el usuario.
     * @return true para seguir en el menú, false para salir.
     */
    private boolean procesarOpcion(int opcion) {
        switch (opcion) {
            case 1:
                opcionInsertar();
                return true;
            case 2:
                opcionListar();
                return true;
            case 3:
                opcionBuscar();
                return true;
            case 4:
                opcionEliminar();
                return true;
            case 5:
                seleccionarMetodo();
                return true;
            case 6:
                opcionCambiarSolucion();
                return true;
            case 7:
                configurarParametros(); // nueva configuración: todo vacío
                seleccionarMetodo();
                return true;
            case 0:
                System.out.println("Hasta pronto.");
                return false;
            default:
                System.out.println("[!] Opcion invalida: " + opcion);
                return true;
        }
    }

    // ========================================================================
    // CONFIGURACIÓN DE PARÁMETROS
    // ========================================================================

    /**
     * Pide los dos parámetros globales del proyecto y crea un nuevo gestor
     * sin estructura activa (la elegirá el usuario en el paso siguiente).
     */
    private void configurarParametros() {
        while (true) {
            try {
                int digitos = leerEntero("Cantidad de digitos de las claves ("
                        + EstructuraDeDatos.DIGITOS_MINIMOS + "-"
                        + EstructuraDeDatos.DIGITOS_MAXIMOS + "): ");
                int tamano = leerEntero("Tamano exacto de la estructura ("
                        + EstructuraDeDatos.CAPACIDAD_MINIMA + "-"
                        + EstructuraDeDatos.CAPACIDAD_MAXIMA + "): ");

                gestor = new GestorBusquedas(digitos, tamano);

                System.out.println("[OK] Configuracion guardada: claves de "
                        + digitos + " digito(s), entre "
                        + rangoClavesValido() + "; tamano "
                        + tamano + " espacios.");
                return;
            } catch (ExcepcionEstructura e) {
                System.out.println("[!] " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // SELECCIÓN / CAMBIO DEL MÉTODO DE BÚSQUEDA
    // ========================================================================

    /**
     * Muestra el catálogo dinámico de búsquedas, pide la solución de
     * colisiones y, si ya había datos, pregunta si se conservan en la
     * nueva estructura.
     */
    private void seleccionarMetodo() {
        List<String> nombres = gestor.getNombresBusquedas();

        while (true) {
            System.out.println();
            System.out.println("--------- METODO DE BUSQUEDA ---------");
            for (int i = 0; i < nombres.size(); i++) {
                String nombre = nombres.get(i);
                System.out.println(" " + (i + 1) + ") " + nombre
                        + "  (estructura: " + gestor.getEstructuraDe(nombre) + ")");
            }
            System.out.println("--------------------------------------");

            int opcion = leerEntero("Seleccione el metodo: ");
            if (opcion < 1 || opcion > nombres.size()) {
                System.out.println("[!] Opcion invalida: " + opcion);
                continue;
            }

            String nombre = nombres.get(opcion - 1);

            // REGLA DEL PROYECTO: en TODA búsqueda se pregunta al inicio con
            // qué función hash se insertan las claves (el árbol digital le
            // niega la pregunta porque crece con su regla de bits).
            FuncionHash funcion = null;
            if (!Model.EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL
                    .equals(gestor.getEstructuraDe(nombre))) {
                funcion = seleccionarFuncion();
            }

            // La solución de colisión se elige SIEMPRE, sin importar el método.
            String solucion = seleccionarSolucion(
                    gestor.getEstructuraDe(nombre));

            // Solo se pregunta por los datos si ya hay estructura activa.
            boolean mantenerDatos = false;
            if (gestor.isSeleccionHecha()) {
                mantenerDatos = leerSiNo(
                        "Mantener las claves actuales en la nueva estructura? (S/N): ");
            }

            try {
                List<Integer> rechazadas = gestor.seleccionarBusqueda(nombre,
                        funcion, solucion, mantenerDatos);

                System.out.println("[OK] Metodo activo: " + nombre
                        + " sobre estructura "
                        + gestor.getTipoEstructuraActiva()
                        + (funcion == null ? "" : (" | Funcion hash para "
                                + "insertar: " + funcion.getNombre()))
                        + " | Solucion de colision: " + solucion + ".");

                if (!rechazadas.isEmpty()) {
                    System.out.println("[!] Claves que NO pudieron migrarse: "
                            + rechazadas + " (sin espacio o cubeta llena).");
                }
                return;
            } catch (ExcepcionEstructura e) {
                System.out.println("[!] " + e.getMessage());
            }
        }
    }

    /**
     * Muestra el catálogo de soluciones de colisión y devuelve la elegida.
     * Siempre se pregunta (regla del proyecto), aunque la familia elegida
     * no genere colisiones; en ese caso se informa su alcance real.
     *
     * @param tipoEstructura estructura del método que se está configurando.
     * @return nombre de la solución seleccionada.
     */
    private String seleccionarSolucion(String tipoEstructura) {
        List<String> nombres = gestor.getNombresSoluciones();

        System.out.println();
        System.out.println("--------- SOLUCION DE COLISION ---------");
        for (int i = 0; i < nombres.size(); i++) {
            System.out.println(" " + (i + 1) + ") " + nombres.get(i));
        }
        System.out.println("----------------------------------------");

        if (!GestorBusquedas.familiaUsaColisiones(tipoEstructura)) {
            if (Model.EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL.equals(tipoEstructura)) {
                System.out.println("(El arbol resuelve sus puntos ocupados con su");
                System.out.println(" regla nativa: avanzar al siguiente bit.)");
            } else {
                System.out.println("(Esta familia no genera colisiones de direccion;");
                System.out.println(" la solucion quedara registrada sin uso.)");
            }
        }

        int opcion;
        do {
            opcion = leerEntero("Seleccione la solucion: ");
            if (opcion < 1 || opcion > nombres.size()) {
                System.out.println("[!] Opcion invalida: " + opcion);
            }
        } while (opcion < 1 || opcion > nombres.size());

        return nombres.get(opcion - 1);
    }

    /**
     * Muestra el catálogo de funciones hash para INSERTAR las claves y
     * devuelve la elegida. Se pregunta al inicio de TODA búsqueda (regla
     * del proyecto) y queda INMODIFICABLE una vez aplicada: la única forma
     * de cambiarla es reiniciar la estructura (cambiar de búsqueda o
     * reconfigurar).
     *
     * @return función hash elegida por el usuario.
     */
    private FuncionHash seleccionarFuncion() {
        List<String> nombres = gestor.getNombresFunciones();

        System.out.println();
        System.out.println("---------- FUNCION HASH PARA INSERTAR ---------");
        for (int i = 0; i < nombres.size(); i++) {
            System.out.println(" " + (i + 1) + ") " + nombres.get(i));
        }
        System.out.println("(Se usa para insertar cada clave y NO puede");
        System.out.println(" cambiarse sin reiniciar la estructura.)");
        System.out.println("-----------------------------------------------");

        int opcion;
        do {
            opcion = leerEntero("Seleccione la funcion hash: ");
            if (opcion < 1 || opcion > nombres.size()) {
                System.out.println("[!] Opcion invalida: " + opcion);
            }
        } while (opcion < 1 || opcion > nombres.size());

        return gestor.getFuncionDe(nombres.get(opcion - 1));
    }

    // ========================================================================
    // MENÚ OPERATIVO
    // ========================================================================

    /** Muestra las opciones fijas con el método activo como contexto. */
    private void mostrarMenu() {
        System.out.println();
        System.out.println("----------------- MENU -----------------");
        System.out.println(" Metodo activo: " + gestor.getNombreBusquedaActiva()
                + "  |  Estructura: " + gestor.getTipoEstructuraActiva());
        if (gestor.getNombreFuncionActiva() != null) {
            System.out.println(" Funcion hash: " + gestor.getNombreFuncionActiva());
        }
        System.out.println(" Solucion de colision: "
                + gestor.getNombreSolucionActiva());
        System.out.println("----------------------------------------");
        System.out.println(" 1) Insertar clave");
        System.out.println(" 2) Listar estructura");
        System.out.println(" 3) Buscar clave (" + gestor.getNombreBusquedaActiva() + ")");
        System.out.println(" 4) Eliminar clave");
        System.out.println(" 5) Cambiar de busqueda");
        System.out.println(" 6) Cambiar solucion de colision (reinicia)");
        System.out.println(" 7) Reconfigurar (digitos y tamano)");
        System.out.println(" 0) Salir");
        System.out.println("----------------------------------------");
    }

    // ========================================================================
    // OPERACIONES DEL USUARIO
    // ========================================================================

    /** Lee una clave, la inserta en la estructura activa y reporta. */
    private void opcionInsertar() {
        int clave = leerEntero("Clave a insertar ("
                + rangoClavesValido() + "): ");
        try {
            gestor.insertarClave(clave);
            System.out.println("[OK] Clave " + clave + " insertada.");
            opcionListar();
        } catch (ExcepcionEstructura e) {
            System.out.println("[!] No se pudo insertar: " + e.getMessage());
        }
    }

    /**
     * Imprime la estructura activa: listado lineal en las familias de
     * arreglo, dibujo jerárquico en el árbol digital y, en las hash,
     * además el detalle de claves desbordadas por dirección.
     */
    private void opcionListar() {
        EstructuraDeDatos estructura = gestor.getEstructuraActiva();
        System.out.println();
        System.out.println("=== ESTRUCTURA ACTUAL: " + estructura.getTipo() + " ===");

        if (estructura instanceof EstructuraArbolDigital) {
            imprimirArbolDigital((EstructuraArbolDigital) estructura);
        } else {
            System.out.println(Arrays.toString(estructura.obtenerClaves())
                    + "  (" + estructura.getCantidad() + "/"
                    + estructura.getCapacidad() + ")");
        }

        if (estructura instanceof EstructuraHash) {
            String desbordes = ((EstructuraHash) estructura).describirDesbordes();
            if (!desbordes.isEmpty()) {
                System.out.println("Desbordes ("
                        + gestor.getNombreSolucionActiva() + "): " + desbordes);
            }
        }
    }

    /** Ejecuta la búsqueda ACTIVA mostrando cada paso registrado. */
    private void opcionBuscar() {
        int clave = leerEntero("Clave a buscar ("
                + gestor.getNombreBusquedaActiva() + "): ");
        ResultadoBusqueda resultado = gestor.buscar(clave);

        System.out.println();
        System.out.println("=== PROCESO DE LA BUSQUEDA "
                + gestor.getNombreBusquedaActiva() + " ===");
        for (int i = 0; i < resultado.getPasos().size(); i++) {
            System.out.println(resultado.getPasos().get(i));
        }

        if (resultado.isEncontrada()) {
            System.out.println(">>> EXITO: posicion interna "
                    + resultado.getIndiceEncontrado());
        } else {
            System.out.println(">>> ERROR: clave no encontrada");
        }
        System.out.println(resultado.getMensaje());
    }

    /** Lee una clave y la elimina de la estructura activa. */
    private void opcionEliminar() {
        int clave = leerEntero("Clave a eliminar: ");
        try {
            gestor.eliminarClave(clave);
            System.out.println("[OK] Clave " + clave + " eliminada.");
            opcionListar();
        } catch (ExcepcionEstructura e) {
            System.out.println("[!] No se pudo eliminar: " + e.getMessage());
        }
    }

    /**
     * Cambia la solución de colisión de la estructura activa. REGLA: la
     * única manera de cambiarla es reiniciar, así que la estructura queda
     * NUEVA y VACÍA (los datos no se conservan).
     */
    private void opcionCambiarSolucion() {
        List<String> nombres = gestor.getNombresSoluciones();

        System.out.println();
        System.out.println("--------- SOLUCION DE COLISION ---------");
        for (int i = 0; i < nombres.size(); i++) {
            System.out.println(" " + (i + 1) + ") " + nombres.get(i));
        }
        System.out.println("----------------------------------------");
        System.out.println("(Cambiar la solucion REINICIA la estructura:");
        System.out.println(" quedara nueva y vacia.)");

        int opcion;
        do {
            opcion = leerEntero("Seleccione la solucion: ");
            if (opcion < 1 || opcion > nombres.size()) {
                System.out.println("[!] Opcion invalida: " + opcion);
            }
        } while (opcion < 1 || opcion > nombres.size());

        String elegida = nombres.get(opcion - 1);
        if (!elegida.equals(gestor.getNombreSolucionActiva())) {
            try {
                gestor.cambiarSolucion(elegida);
            } catch (ExcepcionEstructura e) {
                System.out.println("[!] " + e.getMessage());
                return;
            }
        }
        System.out.println("[OK] Solucion activa: " + gestor.getNombreSolucionActiva()
                + ". Estructura reiniciada y vacia.");
    }

    // ========================================================================
    // DIBUJO DEL ÁRBOL DIGITAL
    // ========================================================================

    /**
     * Dibuja el árbol digital con sangrías: cada hijo izquierdo cuelga de
     * la rama "0" y cada derecho de la rama "1", mostrando junto a cada
     * nodo su clave y su código binario.
     *
     * @param arbol estructura de árbol digital activa.
     */
    private void imprimirArbolDigital(EstructuraArbolDigital arbol) {
        NodoArbolDigital raiz = arbol.getRaiz();
        if (raiz == null) {
            System.out.println("(arbol sin nodos)");
            return;
        }
        System.out.println("RAIZ -> " + textoNodo(raiz));
        dibujarHijos(raiz, "");
        System.out.println("Total: " + arbol.getCantidad() + " clave(s).");
    }

    /**
     * Recorre recursivamente los hijos de un nodo imprimiendo sus ramas.
     *
     * @param nodo    nodo padre cuyos hijos se dibujan.
     * @param sangria prefijo de espacios acumulado hasta este nivel.
     */
    private void dibujarHijos(NodoArbolDigital nodo, String sangria) {
        NodoArbolDigital izquierda = nodo.getIzquierda();
        NodoArbolDigital derecha = nodo.getDerecha();

        if (izquierda != null) {
            System.out.println(sangria + "+- 0 -> " + textoNodo(izquierda));
            dibujarHijos(izquierda, sangria + "|  ");
        }
        if (derecha != null) {
            System.out.println(sangria + "`- 1 -> " + textoNodo(derecha));
            dibujarHijos(derecha, sangria + "   ");
        }
    }

    /**
     * @return descripción corta de un nodo: su clave con su binario entre
     *         corchetes, o la marca de espacio vacío.
     */
    private String textoNodo(NodoArbolDigital nodo) {
        if (nodo.estaVacio()) {
            return "(vacio)";
        }
        return nodo.getClave() + " [" + Integer.toBinaryString(nodo.getClave()) + "]";
    }

    // ========================================================================
    // UTILIDADES DE ENTRADA / SALIDA
    // ========================================================================

    /**
     * @return texto del rango válido de claves según los dígitos
     *         configurados (ejemplo: "1000-9999").
     */
    private String rangoClavesValido() {
        return gestor.getDigitosClave() > 0 && gestor.isSeleccionHecha()
                ? gestor.getEstructuraActiva().obtenerValorMinimoValido() + "-"
                        + gestor.getEstructuraActiva().obtenerValorMaximoValido()
                : calcularRango(gestor.getDigitosClave());
    }

    /**
     * Calcula el rango de claves para una cifra dada sin depender de una
     * estructura creada (usado antes de elegir método).
     *
     * @param digitos cantidad de dígitos configurada.
     * @return rango textual "min-max".
     */
    private String calcularRango(int digitos) {
        long minimo = (digitos == 1) ? 0 : (long) Math.pow(10, digitos - 1);
        long maximo = (long) Math.pow(10, digitos) - 1;
        return minimo + "-" + maximo;
    }

    /**
     * Lee una respuesta S/N repitiendo hasta recibir una válida.
     *
     * @param mensaje pregunta mostrada al usuario.
     * @return true si respondió S; false si respondió N.
     */
    private boolean leerSiNo(String mensaje) {
        while (true) {
            String respuesta = leerLinea(mensaje).toUpperCase();
            if (respuesta.equals("S")) {
                return true;
            }
            if (respuesta.equals("N")) {
                return false;
            }
            System.out.println("[!] Responda S o N.");
        }
    }

    /**
     * @return línea escrita por el usuario sin espacios sobrantes.
     *         Si la entrada se agota (fin de archivo), cierra el programa
     *         para no quedar esperando datos que nunca llegarán.
     */
    private String leerLinea(String mensaje) {
        System.out.print(mensaje);
        try {
            String linea = entrada.readLine();
            if (linea == null) {
                System.out.println();
                System.out.println("[!] Entrada finalizada: cerrando el programa.");
                System.exit(0);
            }
            return linea.trim();
        } catch (IOException e) {
            System.out.println();
            System.out.println("[!] Error de lectura: cerrando el programa.");
            System.exit(0);
            return ""; // nunca se alcanza; System.exit corta la ejecución
        }
    }

    /**
     * Lee un número entero repitiendo la pregunta hasta que sea válido.
     *
     * @param mensaje texto mostrado al usuario.
     * @return entero digitado.
     */
    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerLinea(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("[!] Debe escribir un numero entero.");
            }
        }
    }
}
