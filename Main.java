import View.DemoConsola;

/**
 * ============================================================================
 * MAIN - PUNTO DE ENTRADA EXCLUSIVO DEL APLICATIVO
 * ============================================================================
 *
 * Clase dedicada ÚNICAMENTE a arrancar el programa. No pertenece a ninguna
 * capa del patrón Modelo-Vista-Controlador: su único trabajo es crear la
 * vista y ponerla en marcha.
 *
 * ¿Por qué separada? Para que el día de mañana la forma de lanzar cambie
 * (por ejemplo, levantar la página web local en lugar de la consola), el
 * cambio ocurra AQUÍ y ninguna capa del proyecto se entere.
 *
 * EJECUCIÓN (desde la raíz del proyecto):
 *   javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java).FullName
 *   java "-Dstdout.encoding=UTF-8" -cp out Main
 */
public final class Main {

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
        new DemoConsola().ejecutar();
    }
}
