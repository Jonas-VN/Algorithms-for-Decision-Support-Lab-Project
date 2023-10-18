import java.util.ArrayList;
import java.util.List;

public class Stack {
    private int id;
    private String name;
    private Location location;
    private int capacity;
    private List<Box> boxes;

    public Stack(int id, Location location, int capacity, String name, List<Box> boxes){
        this.id = id;
        this.location = location;
        this.capacity = capacity;
        this.boxes = boxes;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCapacity(){
        return capacity;
    }
    public List<Box> getBoxes(){
        return boxes;
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

    public Box findBoxById(String Id){
        for(int i = 0; i < this.boxes.size(); i++){
            if(this.boxes.get(i).getId().equals(Id)){
                return(this.boxes.get(i));
            }
        }
        return null;
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

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Stack{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", capacity=" + capacity +
                ", boxes:\n");
        for (Box box : boxes) ret.append("\t").append(box).append("\n");
        ret.append('}');

        return ret.toString();
    }
}
