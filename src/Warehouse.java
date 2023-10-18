import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    public void readInput(String file) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(file)));
        JSONObject jsonData = new JSONObject(jsonContent);
        int stackCapacity = jsonData.getInt("stackcapacity");
        int vehicleSpeed = jsonData.getInt("vehiclespeed");
        int loadingDuration = jsonData.getInt("loadingduration");



        JSONArray stacks = jsonData.getJSONArray("stacks");
        for(int i = 0; i < stacks.length(); i++){
            JSONObject stackData = stacks.getJSONObject(i);
            int ID = stackData.getInt("ID");
            String name = stackData.getString("name");
            int x = stackData.getInt("x");
            int y = stackData.getInt("y");
            List<Box> boxes = new ArrayList<>();
            JSONArray boxArray = stackData.getJSONArray("boxes");
            for(int j = 0; j < boxArray.length(); j++) {
                String boxId = boxArray.getString(j);
                boxes.add(new Box(boxId));
            }
            Location location = new Location(x,y);
            this.stacks.add(new Stack(ID, location, stackCapacity, name, boxes));
        }

    }
}
