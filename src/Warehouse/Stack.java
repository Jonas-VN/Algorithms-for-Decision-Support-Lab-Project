package Warehouse;

import Utils.Location;
import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;

public class Stack extends Storage {
    private final java.util.Stack<Box> boxes = new java.util.Stack<>();

    public Stack(int id, Location location, int capacity, String name) {
        super(id, location, capacity, name);
    }

    @Override
    public int getFreeSpaces() {
        return this.capacity - this.boxes.size();
    }

    @Override
    public int numberOfBoxesOnTop(Box box) {
        int count = 0;
        for (int i = this.boxes.size() - 1; i >= 0; i--) {
            if (this.boxes.get(i) == box) break;
            count++;
        }
        return count;
    }

    @Override
    public boolean canBeUsedByVehicle(int vehicleId) {
        // Not in use OR used by the same vehicle (give priority to the vehicle that is already using this stack)
        return this.vehicleId == -1 || this.vehicleId == vehicleId;
    }

    @Override
    public boolean willBeFull(int numberOfBoxes) {
        return this.boxes.size() + this.numberOfBoxesComing + numberOfBoxes > this.capacity;
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
    public void addBox(Box box) throws StackIsFullException {
        if (this.isFull()) {
            throw new StackIsFullException("Stack is full!");
        }
        this.boxes.push(box);
        box.setStack(this);
    }

    @Override
    public void removeBox(Box box) throws BoxNotAccessibleException {
        Box removedBox = this.boxes.peek();
        if (removedBox != box) {
            throw new BoxNotAccessibleException("Tried removing Box " + box.getId() + "! Box was probably not on top of the stack...");
        }
        this.boxes.pop();
        box.setStack(null);
    }

    @Override
    public boolean contains(Box box) {
        return this.boxes.contains(box);
    }

    @Override
    public boolean canRemoveBox(Box box) {
        if (this.boxes.isEmpty()) return false;
        else return this.boxes.peek() == box;
    }

    @Override
    public Box peek() {
        return this.boxes.peek();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Stack{id=" + id + ", boxes=[");
        for (Box box : this.boxes) {
            ret.append(box.getId()).append(", ");
        }
        ret.append("]}");
        return ret.toString();
    }
    @Override
    public Box getBox(int index) {
        return this.boxes.get(this.boxes.size() - 1 - index);
    }
}
