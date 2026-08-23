package CollisionsFix;


public class NestedArray {

    private int[][] table;
    private int size;

    public NestedArray(int size) {

        this.size = size;
        table = new int[size][size];

        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {
                table[i][j] = -1;
            }
        }
    }

    public void insertar(int numero) {

        int posicion = numero % size;

        for (int i = 0; i < size; i++) {

            if (table[posicion][i] == -1) {

                table[posicion][i] = numero;
                return;
            }
        }

        System.out.println("La posición está llena.");
    }

    public boolean buscar(int numero) {

        int posicion = numero % size;

        for (int i = 0; i < size; i++) {

            if (table[posicion][i] == numero) {
                return true;
            }

            if (table[posicion][i] == -1) {
                return false;
            }
        }

        return false;
    }

    public void mostrar() {

        for (int i = 0; i < size; i++) {

            System.out.print(i + " -> ");

            for (int j = 0; j < size; j++) {

                if (table[i][j] != -1) {
                    System.out.print(table[i][j] + " ");
                }
            }

            System.out.println();
        }
    }
}