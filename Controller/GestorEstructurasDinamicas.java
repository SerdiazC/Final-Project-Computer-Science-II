package Controller;

import Model.busquedas.ResultadoBusqueda;
import Model.estructuras_dinámicas.DynamicStruct;
import Model.excepciones.ExcepcionEstructura;

public class GestorEstructurasDinamicas {

    private DynamicStruct estructura;

    // Constructor del Gestor: Propaga la excepción con 'throws ExcepcionEstructura'
    public GestorEstructurasDinamicas(int cantidadCubetas, int capacidadCubeta)
            throws ExcepcionEstructura {
        this.estructura = new DynamicStruct(cantidadCubetas, capacidadCubeta);
    }

    /**
     * (Re)construye la estructura de índices con la configuración pedida:
     * cifras de la clave, cantidad de cubetas y capacidad de cada cubeta.
     */
    public void configurar(int digitos, int cantidadCubetas, int capacidadCubeta)
            throws ExcepcionEstructura {
        this.estructura = new DynamicStruct(
                cantidadCubetas, capacidadCubeta, digitos);
    }

    public void insertar(int dato) throws ExcepcionEstructura {
        estructura.insertar(dato);
    }

    public boolean eliminar(int dato) {
        return estructura.eliminarDato(dato);
    }

    // Métodos de búsqueda tradicionales y por índices
    public int buscar(int dato) {
        return estructura.buscar(dato);
    }

    public int buscarPrimario(int dato) {
        return estructura.buscarPrimario(dato);
    }

    public int buscarSecundario(int dato) {
        return estructura.buscarSecundario(dato);
    }

    public int buscarMultinivel(int dato) {
        return estructura.buscarMultinivel(dato);
    }

    // Búsquedas por índice con pasos visualizables para la interfaz web
    public ResultadoBusqueda buscarPrimarioConPasos(int dato) {
        return estructura.buscarPrimarioConPasos(dato);
    }

    public ResultadoBusqueda buscarSecundarioConPasos(int dato) {
        return estructura.buscarSecundarioConPasos(dato);
    }

    public ResultadoBusqueda buscarMultinivelConPasos(int dato) {
        return estructura.buscarMultinivelConPasos(dato);
    }

    public int getDigitosClave() {
        return estructura.getDigitosClave();
    }

    public DynamicStruct getEstructura() {
        return estructura;
    }
}