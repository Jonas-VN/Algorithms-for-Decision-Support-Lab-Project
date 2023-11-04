package Warehouse;

import Utils.Location;
import Warehouse.Exceptions.BoxNotAccessibleException;

public abstract class Storage {
    private final int id;
    private final String name;
    private final Location location;
    protected final int capacity;

    public Storage(int id, Location location, int capacity, String name) {
        super();
        this.id = id;
        this.location = location;
        this.capacity = capacity;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public abstract boolean isFull();

    public abstract boolean isEmpty();

    public abstract Box findBoxById(String Id);

    public abstract void addBox(Box box);

    public abstract void removeBox(Box box) throws BoxNotAccessibleException;

    public abstract boolean contains(Box box);

    public abstract boolean canRemoveBox(Box box);

    public abstract Box getTopBox();
}

