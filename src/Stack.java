import java.util.ArrayList;
import java.util.List;

public class Stack {
    private int id;
    private Location location;
    private int capacity;
    private List<Box> boxes;

    public Stack(int id, Location location, int capacity){
        this.id = id;
        this.location = location;
        this.capacity = capacity;
        this.boxes = new ArrayList<>();
    }

    public int getId(){
        return id;
    }
    public Location getLocation(){
        return location;
    }
    public boolean isFull(){
        return boxes.size() >= capacity;
    }
    public void addBox(Box box){
        if(!isFull()){
            boxes.add(box);
            box.setStack(this);
            System.out.println("Box with id " + box.getId() + " has been added to stack with id " + id + ".");
        }
        else{
            System.out.println("Stack with id "+ id + " is full");
        }
    }

    public void removeBox(Box box){
        if(boxes.contains(box)) {
            boxes.remove(box);
            box.setStack(null);
            System.out.println("Box with id " + box.getId() + " has been removed from stack with id " + id + ".");
        }
        else{
            System.out.println("The box with id " + box.getId() + " is not present in stack with id " + id + "." );
        }
    }
}
