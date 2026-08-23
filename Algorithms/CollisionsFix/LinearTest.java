package CollisionsFix;

public class LinearTest {
    
    private int [] table; 
    private int size;

    public LinearTest(int size) {
        this.size = size;
        table = new int[size];

        for (int i = 0; i < size; i++){
            table[i]= -1;
        }
    }//End LinearTest method

    public void insert(int num){
        int index = num % size;

        for (int i = 0; i < size; i++){
            int newIndex = (index + i) % size;

            if (table[newIndex]== -1){
                table[newIndex]= num;
                return;
            }
             if (table[newIndex]== 1){
            table[newIndex]= num;
            return;
            }
        }
        System.out.println("La tabla está llena, no se puede insertar la clave: "+ num);
    }//End insert method

    public boolean search(int num){
        int index = num % size;

        for (int i = 0; i < size; i++){
            int newIndex = (index + i) % size;

            if (table[newIndex]== num){
                return true;
            }
            if (table[newIndex] == -1) {
                return false;
            }
        }
        return false;
    }//End search method

    public void displayTable(){
        for (int i = 0; i < size; i++){
            System.out.println(i + " -> "+ table[i]);
        }
    }//End displayTable method
}//End LinearTest class
