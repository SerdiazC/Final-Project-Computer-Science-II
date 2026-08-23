package Search;

public class Main {
    public static void main(String[] args) {
        int[] arreglo = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        Compare compare;

        compare = new Secuential();
        System.out.println("Búsqueda secuencial: Posición dentro del arreglo " + compare.comparar(arreglo, 10));

        
        compare = new Binary();
         System.out.println("Búsqueda binaria: Posición dentro del arreglo " + compare.comparar(arreglo, 10));
    }
}
