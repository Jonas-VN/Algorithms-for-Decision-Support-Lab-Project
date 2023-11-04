package Warehouse;

import Utils.Location;
import Warehouse.Exceptions.BoxNotAccessibleException;

public class Stack extends Storage {
    private final java.util.Stack<Box> boxes = new java.util.Stack<>();

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
        box.setStack(this);
    }

    @Override
    public void removeBox(Box box) throws BoxNotAccessibleException {
        Box removedBox = this.boxes.pop();
        if (removedBox != box) {
            throw new BoxNotAccessibleException("Box " + box.getId() + " != " + removedBox.getId() + "! RemovedBox was probably not on top of the stack...");
            // System.out.println("ERROR: Box " + box.getId() + " != " + removedBox.getId() + "! RemovedBox was probably not on top of the stack...");
        }
        // assert removedBox == box : "Box " + box.getId() + " != " + removedBox.getId() + "! RemovedBox was probably not on top of the stack...";
        box.setStack(null);
    }

    @Override
    public boolean contains(Box box) {
        return this.boxes.contains(box);
    }

    @Override
    public boolean canRemoveBox(Box box) {
        return this.boxes.peek() == box;
    }

    @Override
    public Box getTopBox() {
        return this.boxes.peek();
    }
}
