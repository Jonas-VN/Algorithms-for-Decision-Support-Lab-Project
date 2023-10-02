import java.util.ArrayList;
import java.util.List;

public class Warehouse {
    private int maxStacks;
    private int maxVehicles;
    private List<Stack> stacks;
    private List<Vehicle> vehicles;
    private List<Request> requests;

    public Warehouse(int maxStacks, int maxVehicles){
        this.maxStacks = maxStacks;
        this.maxVehicles = maxVehicles;
        this.requests = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        this.stacks = new ArrayList<>();
    }
}
