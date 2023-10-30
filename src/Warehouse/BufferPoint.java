package Warehouse;

import Utils.Location;

import java.util.ArrayList;

public class BufferPoint extends Storage  {
    private final ArrayList<Box> boxes;
    public BufferPoint(int id, Location location, int capacity, String name, ArrayList<Box> boxes) {
        super(id, location, capacity, name);
        this.boxes = boxes;
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
        for (Box box : this.boxes) {
            if (box.getId().equals(Id)) return box;
        }
        return null;
    }

    @Override
    public void addBox(Box box) {
        this.boxes.add(box);
        box.setStack(this);
    }

    @Override
    public void removeBox(Box box) {
        this.boxes.remove(box);
        box.setStack(null);
    }

    @Override
    public boolean contains(Box box) {
        return this.boxes.contains(box);
    }

}
