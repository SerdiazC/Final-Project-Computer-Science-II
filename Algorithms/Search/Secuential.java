package Algorithms.Search;
//import java.util.Scanner;

public class Secuential extends Compare{
    @Override

    public int comparar(int[]datos, int numero) {

        for (int i = 0; i < datos.length; i++){
            if (datos[i]== numero){
                return i;
            }
        }

        return -1;
    }

    
}//Fin Clase Secuential
