import java.util.List;

public class Stack {
    private int id;
    private int x;
    private int y;
    private int capacity;
    private List<Box> boxes;

    public Stack(int id, int x, int y, int capacity, List<Box> boxes){
        this.id = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
        this.boxes = boxes;
    }
}
