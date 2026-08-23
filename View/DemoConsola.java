package View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import Controller.GestorBusquedas;
import Model.EstructuraDeDatos;
import Model.busquedas.ResultadoBusqueda;
import Model.excepciones.ExcepcionEstructura;

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
 * RESPONSABILIDAD ÚNICA: dialogar con el usuario (leer entradas, mostrar
 * resultados y pasos). Toda decisión de negocio vive en el modelo y toda
 * coordinación en el controlador GestorBusquedas.
 *
 * DETALLE Open/Closed: el menú de búsquedas se GENERA a partir del catálogo
 * registrado en el controlador; agregar una búsqueda nueva no exige tocar
 * esta vista.
 */
public class DemoConsola {

    /** Lector de líneas del teclado (librería estándar java.io). */
    private final BufferedReader entrada = new BufferedReader(
            new InputStreamReader(System.in));

    /** Controlador que orquesta estructuras y búsquedas. */
    private GestorBusquedas gestor;

    /** Nombres de búsquedas registrados (orden estable) para el menú. */
    private List<String> nombresBusquedas;

    // ========================================================================
    // CICLO PRINCIPAL DE LA APLICACIÓN
    // ========================================================================

    /**
     * Punto de ejecución: configura dígitos y tamaño de las estructuras y
     * despliega el menú hasta que el usuario decida salir.
     */
    public void ejecutar() {
        System.out.println("=====================================================");
        System.out.println("   BUSQUEDAS INTERNAS - LINEAL, BINARIA Y HASH");
        System.out.println("   Tamano configurable por estructura: "
                + EstructuraDeDatos.CAPACIDAD_MINIMA + "-"
                + EstructuraDeDatos.CAPACIDAD_MAXIMA + " espacios");
        System.out.println("=====================================================");

        configurarParametros();
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
        int inicioBusquedas = 3;
        int finBusquedas = inicioBusquedas + nombresBusquedas.size() - 1;

        if (opcion == 1) {
            opcionInsertar();
        } else if (opcion == 2) {
            opcionListar();
        } else if (opcion >= inicioBusquedas && opcion <= finBusquedas) {
            opcionBuscar(nombresBusquedas.get(opcion - inicioBusquedas));
        } else if (opcion == finBusquedas + 1) {
            opcionEliminar();
        } else if (opcion == finBusquedas + 2) {
            configurarParametros(); // reinicia todas las estructuras
        } else if (opcion == 0) {
            System.out.println("Hasta pronto.");
            return false;
        } else {
            System.out.println("[!] Opcion invalida: " + opcion);
        }
        return true;
    }

    // ========================================================================
    // CONFIGURACIÓN INICIAL
    // ========================================================================

    /**
     * Pide los dos parámetros globales del proyecto y crea un nuevo gestor:
     * las CINCO estructuras quedan vacías con esa misma configuración.
     */
    private void configurarParametros() {
        while (true) {
            try {
                int digitos = leerEntero("Cantidad de digitos de las claves ("
                        + EstructuraDeDatos.DIGITOS_MINIMOS + "-"
                        + EstructuraDeDatos.DIGITOS_MAXIMOS + "): ");
                int tamano = leerEntero("Tamano exacto de cada estructura ("
                        + EstructuraDeDatos.CAPACIDAD_MINIMA + "-"
                        + EstructuraDeDatos.CAPACIDAD_MAXIMA + "): ");

                gestor = new GestorBusquedas(digitos, tamano);
                nombresBusquedas = gestor.getNombresBusquedas();

                System.out.println("[OK] Estructuras creadas (" + tamano
                        + " espacios cada una). Claves de exactamente " + digitos
                        + " digito(s), entre "
                        + gestor.getEstructuraReferencia().obtenerValorMinimoValido()
                        + " y "
                        + gestor.getEstructuraReferencia().obtenerValorMaximoValido()
                        + ".");
                return;
            } catch (ExcepcionEstructura e) {
                System.out.println("[!] " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // MENÚ
    // ========================================================================

    /** Muestra las opciones disponibles, generando las de búsqueda solas. */
    private void mostrarMenu() {
        System.out.println();
        System.out.println("----------------- MENU -----------------");
        System.out.println(" 1) Insertar clave");
        System.out.println(" 2) Listar claves de las estructuras");

        for (int i = 0; i < nombresBusquedas.size(); i++) {
            System.out.println(" " + (i + 3) + ") Buscar (" + nombresBusquedas.get(i) + ")");
        }

        int ultimaBusqueda = 2 + nombresBusquedas.size();
        System.out.println(" " + (ultimaBusqueda + 1) + ") Eliminar clave");
        System.out.println(" " + (ultimaBusqueda + 2) + ") Reiniciar (digitos y tamano)");
        System.out.println(" 0) Salir");
        System.out.println("----------------------------------------");
    }

    // ========================================================================
    // OPERACIONES DEL USUARIO
    // ========================================================================

    /** Lee una clave, la inserta en todas las estructuras y reporta. */
    private void opcionInsertar() {
        int clave = leerEntero("Clave a insertar: ");
        try {
            gestor.insertarClave(clave);
            System.out.println("[OK] Clave " + clave + " insertada.");
            opcionListar();
        } catch (ExcepcionEstructura e) {
            System.out.println("[!] No se pudo insertar: " + e.getMessage());
        }
    }

    /** Imprime el contenido actual de TODAS las estructuras. */
    private void opcionListar() {
        for (Map.Entry<String, EstructuraDeDatos> par : gestor.getEstructuras().entrySet()) {
            imprimirEstructura(etiquetaDe(par.getKey()), par.getValue());
        }
    }

    /**
     * Traduce el nombre técnico a una etiqueta descriptiva.
     *
     * @param tipo nombre técnico de la estructura.
     * @return etiqueta legible para la lista.
     */
    private String etiquetaDe(String tipo) {
        switch (tipo) {
            case EstructuraDeDatos.TIPO_SECUENCIAL:
                return "SECUENCIAL        (orden de llegada)";
            case EstructuraDeDatos.TIPO_ORDENADA:
                return "ORDENADA          (menor a mayor)";
            default:
                return String.format("%-18s(direccion calculada)", tipo);
        }
    }

    /** Ejecuta la búsqueda pedida mostrando cada paso registrado. */
    private void opcionBuscar(String nombreBusqueda) {
        int clave = leerEntero("Clave a buscar (" + nombreBusqueda + "): ");
        ResultadoBusqueda resultado = gestor.buscar(nombreBusqueda, clave);

        System.out.println();
        System.out.println("=== PROCESO DE LA BUSQUEDA " + nombreBusqueda + " ===");
        for (int i = 0; i < resultado.getPasos().size(); i++) {
            System.out.println(resultado.getPasos().get(i));
        }

        if (resultado.isEncontrada()) {
            System.out.println(">>> EXITO: posicion interna " + resultado.getIndiceEncontrado());
        } else {
            System.out.println(">>> ERROR: clave no encontrada");
        }
        System.out.println(resultado.getMensaje());
    }

    /** Lee una clave y la elimina de todas las estructuras. */
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

    // ========================================================================
    // UTILIDADES DE ENTRADA / SALIDA
    // ========================================================================

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

    /**
     * Imprime el estado de una estructura con formato uniforme.
     *
     * @param titulo     encabezado descriptivo.
     * @param estructura estructura cuyo contenido se muestra.
     */
    private void imprimirEstructura(String titulo, EstructuraDeDatos estructura) {
        System.out.println(titulo + ": " + Arrays.toString(estructura.obtenerClaves())
                + "  (" + estructura.getCantidad() + "/"
                + estructura.getCapacidad() + ")");
    }
}
