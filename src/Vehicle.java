import java.util.List;

public class Vehicle {
    private int id;
    private int x;
    private int y;
    private int speed;
    private int capacity;
    private List<Box> boxes;

    public Vehicle(int id, int x, int y, int speed, int capacity, List<Box> boxes){
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.capacity = capacity;
        this.boxes = boxes;
    }
}
