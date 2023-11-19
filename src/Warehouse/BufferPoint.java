package Warehouse;

import Utils.Location;

import java.util.ArrayList;
import java.util.HashMap;

public class BufferPoint extends Storage  {
    private final HashMap<String, Box> boxes;
    public BufferPoint(int id, Location location, int capacity, String name, HashMap<String, Box> boxes) {
        super(id, location, capacity, name);
        this.boxes = boxes;
    }

    @Override
    public int getFreeSpaces() {
        // BufferPoint has no limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int numberOfBoxesOnTop(Box box) {
        // Boxes are always accessible in the BufferPoint
        return 0;
    }

    @Override
    public boolean isFull() {
        return this.boxes.size() == this.capacity;
    }

    @Override
    public boolean isEmpty() {
        return this.boxes.isEmpty();
    }

    @Override
    public Box findBoxById(String Id) {
        return this.boxes.get(Id);
    }

    @Override
    public void addBox(Box box) {
        this.boxes.put(box.getId(), box);
        box.setStack(this);
    }

    @Override
    public void removeBox(Box box) {
        this.boxes.remove(box.getId());
        box.setStack(null);
    }

    @Override
    public boolean contains(Box box) {
        return this.boxes.containsKey(box.getId());
    }

    @Override
    public boolean canRemoveBox(Box box) {
        return this.contains(box);
    }

    @Override
    public Box peek() {
        return null;
    }

    @Override
    public boolean canBeUsedByVehicle(int vehicleId) {
        // BufferPoint can always be used by any vehicle
        return true;
    }

}
