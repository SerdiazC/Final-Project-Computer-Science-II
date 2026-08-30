package DynamicStructures;

public class Bucket {

    private int[] datos;
    private int cantidad;

    public Bucket(int capacidad) {

        datos = new int[capacidad];
        cantidad = 0;
    }

    // INSERTAR
    public boolean insertar(int dato) {

        if (cantidad < datos.length) {

            datos[cantidad] = dato;
            cantidad++;

            return true;
        }

        return false;
    }

    // ELIMINAR
    public boolean eliminar(int dato) {

        for (int i = 0; i < cantidad; i++) {

            if (datos[i] == dato) {

                //Move to the left
                for (int j = i; j < cantidad - 1; j++) {
                    datos[j] = datos[j + 1];
                }

                cantidad--;

                return true;
            }
        }

        return false;
    }

    public int[] getDatos() {
        return datos;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getCapacidad() {
        return datos.length;
    }
}//End Bucket class