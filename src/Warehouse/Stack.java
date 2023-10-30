package Warehouse;

import Utils.Location;

public class Stack extends Storage {
    private java.util.Stack<Box> boxes = new java.util.Stack<>();

    public Stack(int id, Location location, int capacity, String name) {
        super(id, location, capacity, name);
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
        this.boxes.push(box);
    }

    @Override
    public void removeBox(Box box) {
        Box removedBox = this.boxes.pop();
        if (removedBox != box) {
            System.out.println("ERROR: Box " + box.getId() + " != " + removedBox.getId() + "! RemovedBox was probably not on top of the stack...");
        }
        assert removedBox == box : "Box " + box.getId() + " != " + removedBox.getId() + "! RemovedBox was probably not on top of the stack...";
        box.setStack(null);
    }

    @Override
    public boolean contains(Box box) {
        return this.boxes.contains(box);
    }
}
