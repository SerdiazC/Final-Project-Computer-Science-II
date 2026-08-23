package CollisionsFix;
;
public class LinkedList {

    private Node[] tabla;
    private int size;

    private class Node {

        int data;
        Node next;

        public Node(int data) {
            this.data = data;
            next = null;
        }
    }//End Node class

    public LinkedList(int size) {
        this.size = size;
        tabla = new Node[size];
    }//End LinkedList method

    public void insert(int numero) {

        int posicion = numero % size;

        Node nuevo = new Node(numero);

        if (tabla[posicion] == null) {
            tabla[posicion] = nuevo;
        } else {

            Node actual = tabla[posicion];

            while (actual.next != null) {
                actual = actual.next;
            }

            actual.next = nuevo;
        }
    }//End insert method

    public boolean search(int numero) {

        int posicion = numero % size;

        Node actual = tabla[posicion];

        while (actual != null) {

            if (actual.data == numero) {
                return true;
            }

            actual = actual.next;
        }

        return false;
    }//End search method

    public void displayTable() {

        for (int i = 0; i < size; i++) {

            System.out.print(i + " -> ");

            Node actual = tabla[i];

            while (actual != null) {

                System.out.print(actual.data + " -> ");

                actual = actual.next;
            }

            System.out.println("null");
        }
    }//End displayTable method
}
