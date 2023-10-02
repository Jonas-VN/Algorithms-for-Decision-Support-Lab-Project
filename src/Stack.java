import java.util.List;

public class Stack {
    private int id;
    private Location location;
    private int capacity;
    private List<Box> boxes;

    public Stack(int id, Location location, int capacity, List<Box> boxes){
        this.id = id;
        this.location = location;
        this.capacity = capacity;
        this.boxes = boxes;
    }
    public boolean isFull(){
        return boxes.size() >= capacity;
    }
    public void addBox(Box box){
        if(!isFull()){
            boxes.add(box);
            System.out.println("Box with id " + box.getId() + " has been added to stack with id " + id + ".");
        }
        else{
            System.out.println("Stack with id "+ id + " is full");
        }
    }

    public void removeBox(Box box){
        if(boxes.contains(box)) {
            boxes.remove(box);
            System.out.println("Box with id " + box.getId() + " has been removed from stack with id " + id + ".");
        }
        else{
            System.out.println("The box with id " + box.getId() + " is not present in stack with id " + id + "." );
        }
    }
}
