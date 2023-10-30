package Warehouse;

import Utils.Location;

public abstract class Storage {
    private int id;
    private String name;
    private Location location;
    protected int capacity;

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

    public abstract void removeBox(Box box);

    public abstract boolean contains(Box box);
}

