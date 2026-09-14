package Model.estructuras_dinámicas;

public class SecondaryIndex {

    private int key;
    private int bucket;
    private int position;

    public SecondaryIndex(int key, int bucket, int position) {
        this.key = key;
        this.bucket = bucket;
        this.position = position;
    }

    public int getKey() {
        return key;
    }

    public int getBucket() {
        return bucket;
    }

    public int getPosition() {
        return position;
    }
}