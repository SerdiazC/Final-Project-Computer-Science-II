import View.DemoConsola;
import View.ServidorWeb;

/**
 * ============================================================================
 * MAIN - PUNTO DE ENTRADA EXCLUSIVO DEL APLICATIVO
 * ============================================================================
 *
 * Clase dedicada ÚNICAMENTE a arrancar el programa. No pertenece a ninguna
 * capa del patrón Modelo-Vista-Controlador: su único trabajo es crear las
 * vistas y ponerlas en marcha.
 *
 * ARRANQUE ACTUAL: levanta la INTERFAZ WEB local (servidor HTTP del JDK)
 * y abre el navegador automáticamente; la consola interactiva sigue
 * disponible como vista clásica en la misma terminal.
 *
 * EJECUCIÓN (desde la raíz del proyecto):
 *   javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java).FullName
 *   java "-Dstdout.encoding=UTF-8" -cp out Main
 *
 * PUERTO: por defecto 8080; se cambia con -Dpuerto.web=8090, por ejemplo.
 *   java "-Dstdout.encoding=UTF-8" -Dpuerto.web=8090 -cp out Main
 */
public final class Main {

    /** Puerto web por defecto cuando no se indica otro. */
    private static final int PUERTO_WEB_DEFECTO = 8080;

    /**
     * Constructor privado: la clase de arranque no debe instanciarse.
     */
    private Main() {
        // Sin estado; evita instancias accidentales.
    }

    /**
     * Método invocado por la máquina virtual para iniciar el programa.
     *
     * @param args argumentos de línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        int puerto = puertoConfigurado();

        ServidorWeb web = null;
        try {
            web = new ServidorWeb(puerto);
            web.iniciar();
            System.out.println("=====================================================");
            System.out.println("  INTERFAZ WEB LISTA");
            System.out.println("  Abra su navegador en: http://localhost:" + puerto + "/");
            System.out.println("  (si no se abrio solo, copie y pegue esa direccion)");
            System.out.println("=====================================================");
            abrirNavegador(puerto);
        } catch (Exception e) {
            System.out.println("[!] No se pudo levantar la interfaz web: "
                    + e.getMessage());
            System.out.println("[!] Usando solo la consola interactiva.");
        }

        // La consola clasica sigue disponible en todo momento.
        new DemoConsola().ejecutar();

        if (web != null) {
            web.detener(0);
        }
    }

    /**
     * @return puerto web tomado de la propiedad -Dpuerto.web=NNNN, o el
     *         valor por defecto si no se especificó o no es válido.
     */
    private static int puertoConfigurado() {
        try {
            return Integer.parseInt(System.getProperty("puerto.web",
                    Integer.toString(PUERTO_WEB_DEFECTO)));
        } catch (NumberFormatException e) {
            return PUERTO_WEB_DEFECTO;
        }
    }

    /**
     * Intenta abrir el navegador del sistema en la dirección local.
     * Si el entorno no tiene escritorio (servidor sin GUI) se omite sin error.
     *
     * @param puerto puerto donde escucha el servidor web.
     */
    private static void abrirNavegador(int puerto) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(
                        java.net.URI.create("http://localhost:" + puerto + "/"));
            }
        } catch (Exception ignorable) {
            // Sin navegador disponible: el usuario copia la URL impresa.
        }
    }
}