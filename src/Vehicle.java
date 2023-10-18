import java.util.ArrayList;
import java.util.List;

public class Vehicle {
    private int id;
    private Location location;
    private int speed;
    private int capacity;
    private List<Box> boxes;
    private String name;
    private final int loadingDuration;

    public Vehicle(int id, Location location, int speed, int capacity, String name, int loadingDuration){
        this.id = id;
        this.location = location;
        this.speed = speed;
        this.capacity = capacity;
        this.boxes = new ArrayList<>();
        this.name = name;
        this.loadingDuration = loadingDuration;
    }

    public String getName() {
        return name;
    }

    public int getId(){
        return id;
    }

    public boolean isFull(){
        return boxes.size() >= capacity;
    }

    public Location getLocation(){
        return location;
    }

    public int getCapacity(){
        return capacity;
    }

    public int getSpeed(){
        return speed;
    }

    public List<Box> getBoxes(){
        return boxes;
    }

    public void loadBox(Box box){
        if(!isFull()){
            boxes.add(box);
            box.getStack().removeBox(box);
            System.out.println("Vehicle " + id + " loaded box " + box.getId() + ".");
        }
        else{
            System.out.println("Vehicle " + id + " is full.");
        }
    }

    public void unloadBox(Box box, Stack stack){
        if(boxes.contains(box)){
            boxes.remove(box);
            stack.addBox(box);
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
