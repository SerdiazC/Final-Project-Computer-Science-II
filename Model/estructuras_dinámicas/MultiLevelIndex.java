package Model.estructuras_dinámicas;

public class MultiLevelIndex {

    private int key;
    private int indexPosition;

    public MultiLevelIndex(int key, int indexPosition) {
        this.key = key;
        this.indexPosition = indexPosition;
    }

    public int getKey() {
        return key;
    }

    public int getIndexPosition() {
        return indexPosition;
    }
}