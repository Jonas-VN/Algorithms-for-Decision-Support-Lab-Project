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
    public void addStack(Stack stack){
        if(stacks.size() >= maxStacks){
            System.out.println("The warehouse has reached its limit of stacks.");
        }
        else{
            stacks.add(stack);
            System.out.println("Stack " + stack.getId() + " has been added to the warehouse.");
        }
    }
    public void addVehicle(Vehicle vehicle){
        if(vehicles.size() >= maxVehicles){
            System.out.println("The warehouse has reached its limit of vehicles.");
        }
        else{
            vehicles.add(vehicle);
            System.out.println("Vehicle " + vehicle.getId() + " has been added to the warehouse.");
        }
    }
}
