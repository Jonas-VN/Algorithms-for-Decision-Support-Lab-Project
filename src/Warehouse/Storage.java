package Warehouse;

import Utils.Location;
import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;

public abstract class Storage {
    private final int id;
    private final String name;
    private final Location location;
    protected final int capacity;
    protected int vehicleId = -1;

    public Storage(int id, Location location, int capacity, String name) {
        super();
        this.id = id;
        this.location = location;
        this.capacity = capacity;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getUsedByVehicle() {
        return vehicleId;
    }

    public abstract int getFreeSpaces();

    public abstract int numberOfBoxesOnTop(Box box);

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public abstract boolean isFull();

    public abstract boolean isEmpty();

    public abstract Box findBoxById(String Id);

    public abstract void addBox(Box box) throws StackIsFullException;

    public abstract void removeBox(Box box) throws BoxNotAccessibleException;

    public abstract boolean contains(Box box);

    public abstract boolean canRemoveBox(Box box);

    public abstract Box peek();

    public abstract boolean canBeUsedByVehicle(int vehicleId);

    public void setUsedByVehicle(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void resetUsedByVehicle() {
        this.vehicleId = -1;
    }

    public static int compareByLocationBox(Storage s1, Storage s2, Storage reference) {
        final int s1Distance = s1.getLocation().manhattanDistance(reference.getLocation());
        final int s2Distance = s2.getLocation().manhattanDistance(reference.getLocation());
        return Integer.compare(s1Distance, s2Distance);
    }
}

