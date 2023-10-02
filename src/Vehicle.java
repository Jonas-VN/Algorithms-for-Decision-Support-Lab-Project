import java.util.List;

public class Vehicle {
    private int id;
    private Location location;
    private int speed;
    private int capacity;
    private List<Box> boxes;

    public Vehicle(int id, Location location, int speed, int capacity, List<Box> boxes){
        this.id = id;
        this.location = location;
        this.speed = speed;
        this.capacity = capacity;
        this.boxes = boxes;
    }

    public boolean isFull(){
        return boxes.size() >= capacity;
    }

    public void loadBox(Box box){
        if(!isFull()){
            boxes.add(box);
            System.out.println("Vehicle " + id + " loaded box " + box.getId() + ".");
        }
        else{
            System.out.println("Vehicle " + id + " is full.");
        }
    }

    public void unloadBox(Box box){
        if(boxes.contains(box)){
            boxes.remove(box);
            System.out.println("Vehicle " + id + " has unloaded box " + box.getId() + ".");
        }
        else{
            System.out.println("Vehicle " + id + " does not contain box " + box.getId() + ".");
        }
    }

    public void moveTo(Location location){
        this.location = location;
    }
}
