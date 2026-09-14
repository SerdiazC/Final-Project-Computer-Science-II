package Model.estructuras_dinámicas;

public class Index {

    private int key;
    private int bucket;

    public Index(int key, int bucket) {
        this.key = key;
        this.bucket = bucket;
    }

    public int getKey() {
        return key;
    }

    public int getBucket() {
        return bucket;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
    }
}