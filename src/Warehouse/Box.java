package Warehouse;

import java.util.Objects;

public class Box {
    private final String id;
    private Storage storage;

    public Box(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setStack(Storage storage) {
        this.storage = storage;
    }

    public Storage getStack() {
        return storage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box box = (Box) o;
        return Objects.equals(id, box.id) && Objects.equals(storage, box.storage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storage);
    }

    @Override
    public String toString() {
        return "Box{" +
                "id='" + id + '\'' +
                ", storage=" + (storage == null ? "NULL" : storage.getName()) +
                '}';
    }
}
