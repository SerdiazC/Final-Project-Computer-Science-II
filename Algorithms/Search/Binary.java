package Algorithms.Search;
//import java.util.Scanner;

public class Binary extends Compare{
    @Override
    public int comparar(int[]datos, int numero) {

        int inicio = 0;
        int fin = datos.length -1;


            while(inicio <= fin){
                int mitad = (inicio+fin)/2;

                if (datos[mitad] == numero){
                    return mitad;
                }
                if (numero < datos[mitad]){
                    fin = mitad -1;
                }
                else {
                    inicio = mitad +1;
                }
            }
            return -1;
    }//Fin compare

    
}//Fin Clase Binary