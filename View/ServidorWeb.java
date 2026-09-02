package View;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import Controller.GestorBusquedas;
import Controller.GestorBusquedasExternas;
import Model.EstructuraDeDatos;
import Model.busquedas.PasoBusqueda;
import Model.busquedas.ResultadoBusqueda;
import Model.estructuras.EstructuraArbolDigital;
import Model.estructuras.EstructuraHash;
import Model.estructuras.NodoArbolDigital;
import Model.estructuras.PasoInsercion;
import Model.excepciones.ExcepcionEstructura;
import Model.transformaciones.FuncionHash;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * SERVIDOR WEB - VISTA PERSONALIZADA POR NAVEGADOR
 * ============================================================================
 *
 * Implementa la interfaz visual del aplicativo usando SOLO las librerías
 * estándar del JDK: el mini-servidor HTTP com.sun.net.httpserver (incluido
 * en el JDK) entrega una página HTML/CSS/JS y una API JSON sobre la que la
 * página opera. No hay dependencias externas ni servidores de aplicación.
 *
 * PROTOCOLO: la página (View/web/index.html) NO conoce los algoritmos:
 * habla únicamente con este controlador HTTP, que traduce cada petición a
 * llamadas al GestorBusquedas (controlador real del sistema) y responde en
 * JSON.
 *
 * ENDPOINTS (todos reciben parámetros por query string):
 *
 *   GET  /                        -> página principal (index.html)
 *   GET  /app.js                  -> lógica de la página
 *   GET  /estilos.css             -> estilos visuales
 *   GET  /api/estado              -> configuración, catálogos y estructura
 *   POST /api/reiniciar           -> limpia TODO y deja el gestor sin método
 *   POST /api/configurar          -> digitos, tamano (nuevo gestor, vacío)
 *   POST /api/seleccionar         -> metodo, funcion, solucion, mantener (1/0)
 *   POST /api/insertar            -> clave
 *   POST /api/eliminar            -> clave
 *   POST /api/buscar              -> clave (respuesta con pasos para animar)
 *   POST /api/cambiar-solucion    -> solucion (reinicia la estructura)
 *   POST /api/exportar            -> claves + configuración vigente (JSON)
 *   POST /api/cargar              -> claves (reconstruye la estructura activa)
 *
 * RESPONSABILIDAD ÚNICA: exponer el sistema por web; NO decide reglas de
 * negocio (viven en el modelo y el gestor).
 */
public class ServidorWeb {

    /** Ruta relativa donde residen los recursos estáticos de la web. */
    private static final String RUTA_RECURSOS = "View/web/";

    /** Controlador que orquesta estructuras y búsquedas (una por sesión). */
    private GestorBusquedas gestor;

    /** Controlador de las BÚSQUEDAS EXTERNAS (cubetas dinámicas). */
    private GestorBusquedasExternas externo;

    /** Puerta de entrada HTTP del JDK. */
    private HttpServer servidor;

    /** Puerto donde escucha el servidor. */
    private final int puerto;

    /**
     * Crea el controlador web con un gestor configurado lista para operar.
     *
     * @param puerto puerto HTTP donde escuchará el servidor.
     * @throws ExcepcionEstructura si falla la configuración inicial.
     */
    public ServidorWeb(int puerto) throws ExcepcionEstructura {
        this.puerto = puerto;
        this.gestor = new GestorBusquedas(
                EstructuraDeDatos.DIGITOS_MINIMOS,
                EstructuraDeDatos.CAPACIDAD_MINIMA);
        this.externo = new GestorBusquedasExternas(
                EstructuraDeDatos.DIGITOS_MINIMOS);
    }

    /**
     * Levanta el servidor HTTP y registra los manejadores de rutas.
     *
     * @throws IOException si no se puede abrir el puerto o leer recursos.
     */
    public void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/", this::manejarRuta);
        servidor.setExecutor(Executors.newCachedThreadPool());
        servidor.start();
    }

    /** @return puerto real en el que quedó escuchando el servidor. */
    public int getPuerto() {
        return puerto;
    }

    /**
     * Detiene el servidor HTTP liberando el puerto.
     *
     * @param demoraSegundos segundos de espera por peticiones en curso (0 = ya).
     */
    public void detener(int demoraSegundos) {
        if (servidor != null) {
            servidor.stop(demoraSegundos);
        }
    }

    // ========================================================================
    // ENRUTADOR PRINCIPAL
    // ========================================================================

    /**
     * Decide qué recurso o acción responder según la ruta solicitada:
     * los archivos estáticos van a la carpeta View/web y las rutas /api/*
     * al traductor JSON.
     *
     * @param intercambio contexto HTTP de la petición entrante.
     * @throws IOException si falla la escritura de la respuesta.
     */
    private void manejarRuta(HttpExchange intercambio) throws IOException {
        String ruta = intercambio.getRequestURI().getPath();
        try {
            if (ruta.equals("/api/estado")) {
                responderJson(intercambio, estadoJson());
            } else if (ruta.equals("/api/reiniciar")) {
                responderJson(intercambio, reiniciarJson());
            } else if (ruta.equals("/api/configurar")) {
                responderJson(intercambio, configurarJson(intercambio));
            } else if (ruta.equals("/api/seleccionar")) {
                responderJson(intercambio, seleccionarJson(intercambio));
            } else if (ruta.equals("/api/insertar")) {
                responderJson(intercambio, insertarJson(intercambio));
            } else if (ruta.equals("/api/eliminar")) {
                responderJson(intercambio, eliminarJson(intercambio));
            } else if (ruta.equals("/api/buscar")) {
                responderJson(intercambio, buscarJson(intercambio));
            } else if (ruta.equals("/api/cambiar-solucion")) {
                responderJson(intercambio, cambiarSolucionJson(intercambio));
            } else if (ruta.equals("/api/exportar")) {
                responderJson(intercambio, exportarJson());
            } else if (ruta.equals("/api/cargar")) {
                responderJson(intercambio, cargarJson(intercambio));
            } else if (ruta.equals("/api/externo/estado")) {
                responderJson(intercambio, externoEstadoJson());
            } else if (ruta.equals("/api/externo/configurar")) {
                responderJson(intercambio, externoConfigurarJson(intercambio));
            } else if (ruta.equals("/api/externo/seleccionar")) {
                responderJson(intercambio, externoSeleccionarJson(intercambio));
            } else if (ruta.equals("/api/externo/insertar")) {
                responderJson(intercambio, externoInsertarJson(intercambio));
            } else if (ruta.equals("/api/externo/eliminar")) {
                responderJson(intercambio, externoEliminarJson(intercambio));
            } else if (ruta.equals("/api/externo/buscar")) {
                responderJson(intercambio, externoBuscarJson(intercambio));
            } else if (ruta.equals("/api/externo/reiniciar")) {
                responderJson(intercambio, externoReiniciarJson());
            } else if (ruta.equals("/api/externo/cargar")) {
                responderJson(intercambio, externoCargarJson(intercambio));
            } else {
                servirEstatico(intercambio, ruta);
            }
        } catch (RuntimeException e) {
            responderJson(intercambio, errorJson(e.getMessage()));
        }
    }

    // ========================================================================
    // RECURSOS ESTÁTICOS (index.html, app.js, estilos.css)
    // ========================================================================

    /**
     * Entrega el recurso estático solicitado desde la carpeta View/web.
     *
     * @param intercambio contexto HTTP.
     * @param ruta        ruta pedida por el navegador ("/", "/app.js").
     * @throws IOException si falla la escritura de la respuesta.
     */
    private void servirEstatico(HttpExchange intercambio, String ruta)
            throws IOException {
        String nombre = ruta.equals("/") ? "index.html" : ruta.substring(1);
        Path archivo = caminoWeb(nombre);
        if (!Files.exists(archivo) || !Files.isRegularFile(archivo)) {
            responderTexto(intercambio, 404, "text/plain; charset=UTF-8",
                    "No existe el recurso: /" + nombre);
            return;
        }
        String tipo = tipoMime(nombre);
        byte[] contenido = Files.readAllBytes(archivo);
        intercambio.getResponseHeaders().set("Content-Type", tipo);
        intercambio.sendResponseHeaders(200, contenido.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(contenido);
        }
    }

    /**
     * @return ruta absoluta de un recurso web colgando del directorio de
     *         trabajo actual (la raíz del proyecto), donde vive View/web.
     */
    private Path caminoWeb(String nombre) {
        return Path.of(RUTA_RECURSOS, nombre).toAbsolutePath();
    }

    /**
     * Devuelve el tipo MIME de un archivo según su extensión.
     *
     * @param nombre nombre del archivo (index.html, app.js, estilos.css).
     * @return tipo MIME adecuado con juego de caracteres UTF-8.
     */
    private String tipoMime(String nombre) {
        if (nombre.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (nombre.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        return "text/html; charset=UTF-8";
    }

    // ========================================================================
    // ESTADO ACTUAL (CATÁLOGOS + ESTRUCTURA EN JSON)
    // ========================================================================

    /**
     * Arma el JSON de estado: configuración global, catálogos de búsquedas y
     * soluciones, método activo y la estructura serializada para la página.
     *
     * @return literal JSON de estado.
     */
    private String estadoJson() {
        StringBuilder json = new StringBuilder("{");

        json.append("\"seleccionHecha\":").append(gestor.isSeleccionHecha());
        json.append(",\"digitos\":").append(gestor.getDigitosClave());
        json.append(",\"tamano\":").append(gestor.getTamanoEstructura());
        json.append(",\"metodo\":")
                .append(SerializadorJson.cadena(gestor.getNombreBusquedaActiva()));
        json.append(",\"tipo\":")
                .append(SerializadorJson.cadena(gestor.getTipoEstructuraActiva()));
        json.append(",\"solucion\":")
                .append(SerializadorJson.cadena(gestor.getNombreSolucionActiva()));
        json.append(",\"funcion\":")
                .append(SerializadorJson.cadena(gestor.getNombreFuncionActiva()));
        json.append(",\"rangoMinimo\":").append(rangoMinimoJson());
        json.append(",\"rangoMaximo\":").append(rangoMaximoJson());

        json.append(",\"busquedas\":[");
        List<String> nombres = gestor.getNombresBusquedas();
        for (int i = 0; i < nombres.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            String nombre = nombres.get(i);
            json.append("{\"nombre\":").append(SerializadorJson.cadena(nombre))
                    .append(",\"estructura\":").append(SerializadorJson
                            .cadena(gestor.getEstructuraDe(nombre)))
                    .append(",\"usaColisiones\":").append(GestorBusquedas
                            .familiaUsaColisiones(gestor.getEstructuraDe(nombre)))
                    .append('}');
        }
        json.append("]");

        json.append(",\"soluciones\":[");
        List<String> soluciones = gestor.getNombresSoluciones();
        for (int i = 0; i < soluciones.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(SerializadorJson.cadena(soluciones.get(i)));
        }
        json.append("]");

        json.append(",\"funciones\":[");
        List<String> funciones = gestor.getNombresFunciones();
        for (int i = 0; i < funciones.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(SerializadorJson.cadena(funciones.get(i)));
        }
        json.append("]");

        json.append(",\"arr\":").append(estructuraJson());
        json.append('}');
        return json.toString();
    }

    /** @return límite inferior del rango válido de claves como texto. */
    private String rangoMinimoJson() {
        EstructuraDeDatos estructura = gestor.getEstructuraActiva();
        return (estructura == null)
                ? Long.toString(gestor.getDigitosClave() == 1 ? 0
                        : (long) Math.pow(10, gestor.getDigitosClave() - 1))
                : Long.toString(estructura.obtenerValorMinimoValido());
    }

    /** @return límite superior del rango válido de claves como texto. */
    private String rangoMaximoJson() {
        EstructuraDeDatos estructura = gestor.getEstructuraActiva();
        return (estructura == null)
                ? Long.toString((long) Math.pow(10, gestor.getDigitosClave()) - 1)
                : Long.toString(estructura.obtenerValorMaximoValido());
    }

    // ========================================================================
    // MUTACIONES DE CONFIGURACIÓN
    // ========================================================================

    /**
     * Limpia TODO y deja el servidor como al arrancar: crea un gestor nuevo
     * y vacío, SIN método de búsqueda seleccionado. El usuario vuelve al
     * inicio y puede reconfigurar dígitos, tamaño, función y solución desde
     * cero (regla del proyecto: así también se puede cambiar la función).
     *
     * @return JSON de resultado.
     */
    private String reiniciarJson() {
        try {
            gestor = new GestorBusquedas(
                    gestor.getDigitosClave(), gestor.getTamanoEstructura());
            return okJson("Todo limpiado. Seleccione un método de búsqueda "
                    + "para comenzar desde cero.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Aplica una nueva configuración (dígitos y tamaño) creando un gestor
     * nuevo y vacío; el usuario deberá elegir método de búsqueda después.
     *
     * @param intercambio contexto HTTP con los parámetros digitos y tamano.
     * @return JSON con el resultado de la operación.
     */
    private String configurarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int digitos = enteroObligatorio(parametros, "digitos");
            int tamano = enteroObligatorio(parametros, "tamano");
            gestor = new GestorBusquedas(digitos, tamano);
            return okJson("Configuración aplicada: claves de " + digitos
                    + " dígito(s), estructura de " + tamano + " espacios.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Selecciona el método de búsqueda activo (crea su estructura) con la
     * función hash que inserta las claves y la solución de colisiones.
     *
     * @param intercambio contexto HTTP con metodo, funcion, solucion y
     *                    mantener (1/0).
     * @return JSON de resultado con las claves que no migraron, si aplica.
     */
    private String seleccionarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            String metodo = textoObligatorio(parametros, "metodo");
            String solucion = textoObligatorio(parametros, "solucion");
            boolean mantener = leerBoolean(parametros, "mantener");

            // Función hash para insertar: obligatoria en toda familia de
            // arreglo; los árboles de residuos digital (ARBOL DIGITAL y
            // ARBOL POR RESIDUOS) la ignoran.
            boolean esArbolResiduos = EstructuraDeDatos.TIPO_RESIDUOS_DIGITAL
                    .equals(gestor.getEstructuraDe(metodo));
            FuncionHash funcion = null;
            String nombreFuncion = parametros.get("funcion");
            if (!esArbolResiduos
                    && (nombreFuncion == null || nombreFuncion.isBlank())) {
                throw new IllegalArgumentException(
                        "Falta el parámetro obligatorio: funcion");
            }
            if (nombreFuncion != null && !nombreFuncion.isBlank()) {
                funcion = gestor.getFuncionDe(nombreFuncion.trim());
                if (funcion == null) {
                    throw new IllegalArgumentException(
                            "Función hash no registrada: " + nombreFuncion);
                }
            }

            List<Integer> rechazadas = gestor.seleccionarBusqueda(
                    metodo, funcion, solucion, mantener);
            String mensaje = "Método activo: " + metodo
                    + " sobre estructura " + gestor.getTipoEstructuraActiva()
                    + (funcion == null ? "" : (" | Función hash para insertar: "
                            + funcion.getNombre()))
                    + " | Solución de colisión: " + solucion + ".";
            if (!rechazadas.isEmpty()) {
                mensaje += " Claves que NO migraron: " + rechazadas;
            }
            return okJsonRechazadas(mensaje, rechazadas);
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Cambia la solución de colisiones de la estructura activa. REGLA DEL
     * PROYECTO: la única forma de cambiarla es REINICIANDO la estructura,
     * así que esta operación la deja nueva y vacía.
     *
     * @param intercambio contexto HTTP con el parámetro solucion.
     * @return JSON de resultado.
     */
    private String cambiarSolucionJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            String solucion = textoObligatorio(parametros, "solucion");
            // El gestor valida la selección previa y crea la estructura nueva.
            gestor.cambiarSolucion(solucion);
            return okJson("Solución activa: " + solucion
                    + ". Estructura reiniciada y vacía.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    // ========================================================================
    // EXPORTACIÓN Y CARGA DE ESTRUCTURAS
    // ========================================================================

    /**
     * Serializa la estructura activa para exportarla: configuración vigente
     * y TODAS sus claves en orden interno. La página guarda esto como
     * archivo JSON para poder recargarlo en OTRO método de búsqueda.
     *
     * @return JSON {ok, digitos, tamano, metodo, solucion, claves}.
     */
    private String exportarJson() {
        if (!gestor.isSeleccionHecha()) {
            return errorJson("Primero debe seleccionar un método de búsqueda "
                    + "para poder exportar su estructura.");
        }
        return "{\"ok\":true"
                + ",\"digitos\":" + gestor.getDigitosClave()
                + ",\"tamano\":" + gestor.getTamanoEstructura()
                + ",\"metodo\":" + SerializadorJson
                        .cadena(gestor.getNombreBusquedaActiva())
                + ",\"funcion\":" + SerializadorJson
                        .cadena(gestor.getNombreFuncionActiva())
                + ",\"solucion\":" + SerializadorJson
                        .cadena(gestor.getNombreSolucionActiva())
                + ",\"claves\":" + SerializadorJson
                        .arregloEnteros(gestor.obtenerClavesActivas())
                + '}';
    }

    /**
     * Reconstruye el contenido de la estructura activa con las claves
     * importadas (mismo método y solución vigentes). Las claves que el
     * método activo rechace se informan sin abortar el resto.
     *
     * @param intercambio contexto HTTP con el parámetro claves
     *                    (lista separada por comas: "1234,5678").
     * @return JSON con el número de claves cargadas y las rechazadas.
     */
    private String cargarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            String texto = textoObligatorio(parametros, "claves");
            List<Integer> claves = new ArrayList<>();
            for (String parte : texto.split(",")) {
                if (parte.isBlank()) {
                    continue;
                }
                claves.add(Integer.parseInt(parte.trim()));
            }
            List<Integer> rechazadas = gestor.cargarClavesEnActiva(claves);
            int cargadas = claves.size() - rechazadas.size();
            String mensaje = "Estructura cargada en "
                    + gestor.getNombreBusquedaActiva() + ": " + cargadas
                    + " de " + claves.size() + " claves.";
            if (!rechazadas.isEmpty()) {
                mensaje += " NO se cargaron: " + rechazadas;
            }
            return okJsonRechazadas(mensaje, rechazadas);
        } catch (NumberFormatException e) {
            return errorJson("El parámetro claves debe ser una lista de "
                    + "enteros separados por comas.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    // ========================================================================
    // OPERACIONES SOBRE CLAVES
    // ========================================================================

    /** Inserta una clave en la estructura activa. */
    private String insertarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int clave = enteroObligatorio(parametros, "clave");
            gestor.insertarClave(clave);

            // Pasos de la inserción (colisión + resolución) para animar.
            StringBuilder pasos = new StringBuilder("[");
            EstructuraDeDatos estructura = gestor.getEstructuraActiva();
            if (estructura instanceof EstructuraHash) {
                List<PasoInsercion> lista = ((EstructuraHash) estructura)
                        .obtenerUltimosPasosInsercion();
                for (int i = 0; i < lista.size(); i++) {
                    PasoInsercion paso = lista.get(i);
                    if (i > 0) {
                        pasos.append(',');
                    }
                    pasos.append("{\"numero\":").append(paso.getNumeroPaso())
                            .append(",\"tipo\":")
                            .append(SerializadorJson.cadena(paso.getTipo()))
                            .append(",\"indice\":").append(paso.getIndice())
                            .append(",\"direccion\":").append(paso.getDireccion())
                            .append(",\"clave\":").append(paso.getClave())
                            .append(",\"descripcion\":")
                            .append(SerializadorJson.cadena(paso.getDescripcion()))
                            .append('}');
                }
            }
            pasos.append(']');

            return "{\"ok\":true,"
                    + "\"mensaje\":" + SerializadorJson.cadena(
                            "Clave " + clave + " insertada correctamente.")
                    + ",\"pasos\":" + pasos + '}';
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    /** Elimina una clave de la estructura activa. */
    private String eliminarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int clave = enteroObligatorio(parametros, "clave");
            gestor.eliminarClave(clave);
            return okJson("Clave " + clave + " eliminada correctamente.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Ejecuta la búsqueda ACTIVA y devuelve el resultado con TODOS sus pasos
     * para que la página los anime y dibuje la estructura.
     *
     * @param intercambio contexto HTTP con el parámetro clave.
     * @return JSON con encontró/pasos/mensaje.
     */
    private String buscarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int clave = enteroObligatorio(parametros, "clave");
            ResultadoBusqueda resultado = gestor.buscar(clave);

            StringBuilder pasos = new StringBuilder("[");
            List<PasoBusqueda> lista = resultado.getPasos();
            for (int i = 0; i < lista.size(); i++) {
                PasoBusqueda paso = lista.get(i);
                if (i > 0) {
                    pasos.append(',');
                }
                pasos.append("{\"numero\":").append(paso.getNumeroPaso())
                        .append(",\"indice\":").append(paso.getIndiceExplorado())
                        .append(",\"limiteInferior\":").append(paso.getLimiteInferior())
                        .append(",\"limiteSuperior\":").append(paso.getLimiteSuperior())
                        .append(",\"claveComparada\":").append(paso.getClaveComparada())
                        .append(",\"descripcion\":")
                        .append(SerializadorJson.cadena(paso.getDescripcion()))
                        .append('}');
            }
            pasos.append(']');

            return "{\"encontrada\":" + resultado.isEncontrada()
                    + ",\"indice\":" + resultado.getIndiceEncontrado()
                    + ",\"mensaje\":" + SerializadorJson.cadena(resultado.getMensaje())
                    + ",\"pasos\":" + pasos + '}';
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    // ========================================================================
    // BÚSQUEDAS EXTERNAS (CUBETAS DINÁMICAS) - ENDPOINTS /api/externo/*
    // ========================================================================

    /**
     * Estado de las búsquedas externas: catálogo, método activo, dígitos,
     * rangos y la estructura de cubetas serializada para la vista.
     *
     * @return literal JSON de estado externo.
     */
    private String externoEstadoJson() {
        StringBuilder json = new StringBuilder("{");

        json.append("\"seleccionHecha\":").append(externo.isSeleccionHecha());
        json.append(",\"metodo\":")
                .append(SerializadorJson.cadena(externo.getMetodoActivo()));
        json.append(",\"digitos\":").append(externo.getDigitosClave());
        json.append(",\"rangoMinimo\":").append(
                externo.getEstructura().getValorMinimo());
        json.append(",\"rangoMaximo\":").append(
                externo.getEstructura().getValorMaximo());

        json.append(",\"busquedas\":[");
        List<String> nombres = externo.getNombresBusquedas();
        for (int i = 0; i < nombres.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(SerializadorJson.cadena(nombres.get(i)));
        }
        json.append("]");

        json.append(",\"estructura\":").append(externoEstructuraJson());
        json.append('}');
        return json.toString();
    }

    /**
     * Serializa la estructura externa de cubetas: cada cubeta con su
     * contenido, capacidad y cantidad, más las métricas globales y el
     * detalle de la última inserción (colisión / directa) y expansiones.
     *
     * @return literal JSON de la estructura externa.
     */
    private String externoEstructuraJson() {
        Model.estructuras.externas.EstructuraCubetas est
                = externo.getEstructura();

        StringBuilder json = new StringBuilder("{");
        json.append("\"tipo\":\"").append("EXTERN\u00c1").append("\"");
        json.append(",\"tipoEstructura\":")
                .append(SerializadorJson.cadena(est.getTipoEstructura()));
        json.append(",\"numCubetas\":").append(est.getNumeroCubetas());
        json.append(",\"cantidad\":").append(est.getCantidad());
        json.append(",\"capacidadBase\":").append(est.getCapacidadBase());
        json.append(",\"filas\":").append(est.getFilas());
        json.append(",\"cubetasPorFila\":").append(est.getCubetasPorFila());
        json.append(",\"densidadExpansion\":").append(
                est.densidadExpansion());
        json.append(",\"densidadReduccion\":").append(
                est.densidadReduccion());

        json.append(",\"ultimaInsercion\":");
        json.append("{\"tipo\":")
                .append(SerializadorJson.cadena(externo.getUltimoTipoInsercion()))
                .append(",\"cubeta\":").append(externo.getUltimaCubetaInsercion())
                .append('}');

        json.append(",\"expansionParcial\":").append(
                est.getUltimaExpansionParcial());
        json.append(",\"expansionTotal\":").append(
                est.getUltimaExpansionTotal());

        json.append(",\"cubetas\":[");
        for (int i = 0; i < est.getNumeroCubetas(); i++) {
            if (i > 0) {
                json.append(',');
            }
            Model.estructuras.externas.Cubeta cubeta = est.consultarCubeta(i);
            json.append("{\"indice\":").append(i)
                    .append(",\"capacidad\":").append(cubeta.getCapacidad())
                    .append(",\"cantidad\":").append(cubeta.getCantidad())
                    .append(",\"datos\":")
                    .append(SerializadorJson.arregloEnteros(cubeta.getDatos()))
                    .append('}');
        }
        json.append("]");
        json.append('}');
        return json.toString();
    }

    /**
     * (Re)configura la cantidad de dígitos de las claves externas, creando
     * una estructura nueva y vacía.
     *
     * @param intercambio contexto HTTP con el parámetro digitos.
     * @return JSON de resultado.
     */
    private String externoConfigurarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int digitos = enteroObligatorio(parametros, "digitos");
            String tipo = parametros.get("tipo");
            if (tipo == null || tipo.isBlank()) {
                tipo = "TOTAL";
            }
            String filasTexto = parametros.get("filas");
            String columnasTexto = parametros.get("cubetasPorFila");
            if (filasTexto != null && !filasTexto.isBlank()
                    && columnasTexto != null && !columnasTexto.isBlank()) {
                int cubetasPorFila = Integer.parseInt(columnasTexto.trim());
                int filas = Integer.parseInt(filasTexto.trim());
                externo.configurar(digitos, cubetasPorFila, filas,
                        tipo.trim());
            } else {
                String cubetasTexto = parametros.get("numCubetas");
                if (cubetasTexto == null || cubetasTexto.isBlank()) {
                    externo.configurar(digitos);
                } else {
                    int cubetas = Integer.parseInt(cubetasTexto.trim());
                    externo.configurar(digitos, cubetas);
                }
            }
            return okJson("Búsqueda externa configurada: claves de " + digitos
                    + " dígito(s), estructura de cubetas nueva y vacía ("
                    + tipo.trim() + ").");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Selecciona la técnica de búsqueda externa activa (LINEAL/BINARIA/HASH MOD).
     *
     * @param intercambio contexto HTTP con el parámetro metodo.
     * @return JSON de resultado.
     */
    private String externoSeleccionarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            String metodo = textoObligatorio(parametros, "metodo");
            externo.seleccionar(metodo);
            return okJson("Búsqueda externa activa: " + metodo + ".");
        } catch (IllegalArgumentException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Reconstruye la estructura externa exportada: reconfigura las cubetas a
     * partir de la organización en filas y reinserta todas las claves. Como la
     * inserción es determinista (hash mod), el resultado reproduce exactamente
     * el estado guardado en el archivo.
     *
     * @param intercambio contexto HTTP con digitos, filas, cubetasPorFila,
     *                    metodo y claves (enteros separados por coma).
     * @return JSON de resultado.
     */
    private String externoCargarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int digitos = enteroObligatorio(parametros, "digitos");
            int cubetasPorFila = enteroObligatorio(parametros, "cubetasPorFila");
            int filas = enteroObligatorio(parametros, "filas");
            String tipo = parametros.get("tipo");
            if (tipo == null || tipo.isBlank()) {
                tipo = "TOTAL";
            }
            externo.configurar(digitos, cubetasPorFila, filas, tipo.trim());

            String metodo = parametros.get("metodo");
            if (metodo != null && !metodo.isBlank()) {
                externo.seleccionar(metodo.trim());
            }

            String texto = textoObligatorio(parametros, "claves");
            int cargadas = 0;
            if (!texto.isBlank()) {
                for (String parte : texto.split(",")) {
                    if (parte.isBlank()) {
                        continue;
                    }
                    externo.insertarClave(Integer.parseInt(parte.trim()));
                    cargadas++;
                }
            }
            return okJson("Estructura externa cargada: " + cargadas
                    + " clave(s) en las " + (cubetasPorFila * filas)
                    + " cubetas (" + cubetasPorFila + " por fila x "
                    + filas + " fila(s)).");
        } catch (NumberFormatException e) {
            return errorJson("El parámetro claves debe ser una lista de "
                    + "enteros separados por comas.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Inserta una clave en la estructura externa y devuelve el tipo de
     * inserción (colisión/directa) con su cubeta para animar la vista.
     *
     * @param intercambio contexto HTTP con el parámetro clave.
     * @return JSON con el resultado y el detalle de la inserción.
     */
    private String externoInsertarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int clave = enteroObligatorio(parametros, "clave");
            externo.insertarClave(clave);
            return "{\"ok\":true"
                    + ",\"mensaje\":" + SerializadorJson.cadena(
                            "Clave " + clave + " insertada en la cubeta "
                                    + externo.getUltimaCubetaInsercion() + ".")
                    + ",\"tipo\":" + SerializadorJson.cadena(
                            externo.getUltimoTipoInsercion())
                    + ",\"cubeta\":" + externo.getUltimaCubetaInsercion()
                    + ",\"expansionParcial\":" + externo.getEstructura()
                            .getUltimaExpansionParcial()
                    + ",\"expansionTotal\":" + externo.getEstructura()
                            .getUltimaExpansionTotal()
                    + '}';
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Elimina una clave de la estructura externa.
     *
     * @param intercambio contexto HTTP con el parámetro clave.
     * @return JSON de resultado.
     */
    private String externoEliminarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int clave = enteroObligatorio(parametros, "clave");
            externo.eliminarClave(clave);
            return okJson("Clave " + clave + " eliminada de la estructura externa.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Ejecuta la búsqueda externa ACTIVA y devuelve sus pasos para animar.
     *
     * @param intercambio contexto HTTP con el parámetro clave.
     * @return JSON con encontrada/indice/pasos/mensaje.
     */
    private String externoBuscarJson(HttpExchange intercambio) {
        Map<String, String> parametros = leerParametros(intercambio);
        try {
            int clave = enteroObligatorio(parametros, "clave");
            ResultadoBusqueda resultado = externo.buscar(clave);

            StringBuilder pasos = new StringBuilder("[");
            List<PasoBusqueda> lista = resultado.getPasos();
            for (int i = 0; i < lista.size(); i++) {
                PasoBusqueda paso = lista.get(i);
                if (i > 0) {
                    pasos.append(',');
                }
                pasos.append("{\"numero\":").append(paso.getNumeroPaso())
                        .append(",\"indice\":").append(paso.getIndiceExplorado())
                        .append(",\"limiteInferior\":").append(paso.getLimiteInferior())
                        .append(",\"limiteSuperior\":").append(paso.getLimiteSuperior())
                        .append(",\"claveComparada\":").append(paso.getClaveComparada())
                        .append(",\"descripcion\":")
                        .append(SerializadorJson.cadena(paso.getDescripcion()))
                        .append('}');
            }
            pasos.append(']');

            return "{\"encontrada\":" + resultado.isEncontrada()
                    + ",\"indice\":" + resultado.getIndiceEncontrado()
                    + ",\"mensaje\":" + SerializadorJson.cadena(resultado.getMensaje())
                    + ",\"pasos\":" + pasos + '}';
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorJson(e.getMessage());
        }
    }

    /**
     * Limpia la búsqueda externa: recrea la estructura con los mismos
     * dígitos y cancela el método seleccionado.
     *
     * @return JSON de resultado.
     */
    private String externoReiniciarJson() {
        try {
            externo.configurar(externo.getDigitosClave());
            return okJson("Búsqueda externa reiniciada: estructura nueva y "
                    + "vacía. Seleccione un método para operar.");
        } catch (ExcepcionEstructura e) {
            return errorJson(e.getMessage());
        }
    }

    // ========================================================================
    // SERIALIZACIÓN DE LA ESTRUCTURA ACTIVA PARA LA PÁGINA
    // ========================================================================

    /**
     * Convierte la estructura activa en un árbol JSON que la página dibuja:
     * las familias de arreglo envían su arreglo interno completo y las
     * cadenas de desbordes hash; el árbol digital envía su jerarquía.
     *
     * @return literal JSON de la estructura activa (o {} si no hay).
     */
    private String estructuraJson() {
        EstructuraDeDatos estructura = gestor.getEstructuraActiva();
        if (estructura == null) {
            return "{}";
        }
        if (estructura instanceof EstructuraArbolDigital) {
            return arbolJson((EstructuraArbolDigital) estructura);
        }
        return arregloJson(estructura);
    }

    /**
     * Serializa una familia basada en arreglo: cada posición interna del
     * arreglo (o CLAVE_VACIA) y, en hash, los desbordes por dirección.
     *
     * @param estructura estructura contigua (secuencial, ordenada o hash).
     * @return literal JSON con el arreglo interno y desbordes (si aplica).
     */
    private String arregloJson(EstructuraDeDatos estructura) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"tipo\":").append(SerializadorJson.cadena(estructura.getTipo()))
                .append(",\"tamano\":").append(estructura.getCapacidad())
                .append(",\"cantidad\":").append(estructura.getCantidad())
                .append(",\"esHash\":").append(estructura instanceof EstructuraHash)
                .append(",\"posiciones\":[");

        for (int i = 0; i < estructura.getCapacidad(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(estructura.consultarPosicion(i));
        }
        json.append("]");

        if (estructura instanceof EstructuraHash) {
            EstructuraHash hash = (EstructuraHash) estructura;
            StringBuilder desbordes = new StringBuilder("{");
            boolean primero = true;
            for (int i = 0; i < estructura.getCapacidad(); i++) {
                int[] fila = hash.consultarDesbordes(i);
                if (fila.length > 0) {
                    if (!primero) {
                        desbordes.append(',');
                    }
                    primero = false;
                    desbordes.append('"').append(i + 1).append("\":")
                            .append(SerializadorJson.arregloEnteros(fila));
                }
            }
            desbordes.append('}');
            json.append(",\"desbordes\":").append(desbordes);
        }
        return json.append('}').toString();
    }

    /**
     * Serializa el árbol digital recursivamente: cada nodo lleva su clave,
     * su binario y sus ramas izquierda (bit 0) y derecha (bit 1).
     *
     * @param arbol estructura de residuos digital.
     * @return literal JSON con la jerarquía del árbol.
     */
    private String arbolJson(EstructuraArbolDigital arbol) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"tipo\":").append(SerializadorJson.cadena(arbol.getTipo()))
                .append(",\"tamano\":").append(arbol.getCapacidad())
                .append(",\"cantidad\":").append(arbol.getCantidad())
                .append(",\"esHash\":false")
                .append(",\"raiz\":").append(nodoJson(arbol.getRaiz()))
                .append('}');
        return json.toString();
    }

    /**
     * Serializa un nodo del árbol digital (o null si no existe).
     *
     * @param nodo nodo actual del árbol.
     * @return literal JSON del nodo y sus hijos.
     */
    private String nodoJson(NodoArbolDigital nodo) {
        if (nodo == null) {
            return "null";
        }
        return "{\"vacio\":" + nodo.estaVacio()
                + ",\"clave\":"
                + (nodo.estaVacio() ? -1 : nodo.getClave())
                + ",\"binario\":"
                + (nodo.estaVacio() ? "null"
                        : SerializadorJson.cadena(Integer.toBinaryString(nodo.getClave())))
                + ",\"izq\":" + nodoJson(nodo.getIzquierda())
                + ",\"der\":" + nodoJson(nodo.getDerecha())
                + '}';
    }

    // ========================================================================
    // UTILIDADES DE RESPUESTA HTTP
    // ========================================================================

    /** Envía una respuesta de texto (HTML, JS, CSS) con su código HTTP. */
    private void responderTexto(HttpExchange intercambio, int codigo,
            String tipo, String contenido) throws IOException {
        byte[] bytes = contenido.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", tipo);
        intercambio.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }

    /** Envía una respuesta JSON con código 200. */
    private void responderJson(HttpExchange intercambio, String json)
            throws IOException {
        responderTexto(intercambio, 200,
                "application/json; charset=UTF-8", json);
    }

    // ========================================================================
    // UTILIDADES DE PARÁMETROS
    // ========================================================================

    /**
     * Lee los parámetros de la query string en un mapa ordenado.
     *
     * @param intercambio contexto HTTP.
     * @return mapa nombre -> valor (sin decodificación pendiente).
     */
    private Map<String, String> leerParametros(HttpExchange intercambio) {
        Map<String, String> mapa = new LinkedHashMap<>();
        String consulta = intercambio.getRequestURI().getRawQuery();
        if (consulta == null || consulta.isBlank()) {
            return mapa;
        }
        for (String par : consulta.split("&")) {
            String[] partes = par.split("=", 2);
            String clave = decodificar(partes[0]);
            String valor = partes.length > 1 ? decodificar(partes[1]) : "";
            mapa.put(clave, valor);
        }
        return mapa;
    }

    /** Decodifica una componente de la query (URL-encoded, UTF-8). */
    private String decodificar(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }

    /** Obtiene un entero obligatorio del parámetro, o lanza excepción. */
    private int enteroObligatorio(Map<String, String> parametros, String nombre) {
        String valor = parametros.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "Falta el parámetro obligatorio: " + nombre);
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un número entero.");
        }
    }

    /** Obtiene un texto obligatorio del parámetro. */
    private String textoObligatorio(Map<String, String> parametros, String nombre) {
        String valor = parametros.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "Falta el parámetro obligatorio: " + nombre);
        }
        return valor.trim();
    }

    /**
     * Interpreta un parámetro booleano en sus formas aceptadas (1/0/true/false).
     *
     * @param parametros mapa de parámetros.
     * @param nombre     nombre del parámetro.
     * @return valor booleano interpretado (false si falta o no coincide).
     */
    private boolean leerBoolean(Map<String, String> parametros, String nombre) {
        String valor = parametros.get(nombre);
        if (valor == null) {
            return false;
        }
        return valor.equals("1") || valor.equalsIgnoreCase("true")
                || valor.equalsIgnoreCase("s");
    }

    // ========================================================================
    // CONSTRUCCIÓN DE RESPUESTAS JSON
    // ========================================================================

    /** @return JSON {"ok":true,"mensaje":"..."}. */
    private String okJson(String mensaje) {
        return "{\"ok\":true,\"mensaje\":" + SerializadorJson.cadena(mensaje) + '}';
    }

    /** @return JSON {"ok":true,"mensaje":"...","rechazadas":[..]}. */
    private String okJsonRechazadas(String mensaje, List<Integer> rechazadas) {
        return "{\"ok\":true,\"mensaje\":" + SerializadorJson.cadena(mensaje)
                + ",\"rechazadas\":" + SerializadorJson.arregloEnteros(rechazadas) + '}';
    }

    /** @return JSON {"ok":false,"mensaje":"..."}. */
    private String errorJson(String mensaje) {
        return "{\"ok\":false,\"mensaje\":" + SerializadorJson.cadena(mensaje) + '}';
    }
}