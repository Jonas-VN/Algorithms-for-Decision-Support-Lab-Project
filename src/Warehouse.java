import java.io.File;
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
    private int time;
    private List<Stack> stacks;
    private List<Vehicle> vehicles;
    private List<Request> requests;

    public Warehouse(int maxStacks, int maxVehicles){
        this.maxStacks = maxStacks;
        this.maxVehicles = maxVehicles;
        this.requests = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        this.stacks = new ArrayList<>();
        this.time = 0;
    }
    public int getTime(){
        return time;
    }
    public List<Request> getRequests(){
        return requests;
    }
    public List<Vehicle> getVehicles(){
        return vehicles;
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

    public void everythingToString(){
        for (Stack stack : this.stacks) {
            System.out.println(stack);
        }
        for (Vehicle vehicle : this.vehicles) {
            System.out.println(vehicle);
        }
        for (Request req : requests) {
            System.out.println(req);
        }
    }

    public void handleRequests(File output){
            double smallestDistance = 9999999;
            Request closestRequest = requests.get(0);
            String operation = null;
            Vehicle vehicle = vehicles.get(0);
            for(int i = 0; i < requests.size(); i++){
                double distanceDestination = requests.get(i).getDestination().getLocation().getDistance(vehicle.getLocation());
                double distancePickup = requests.get(i).getPickup().getLocation().getDistance(vehicle.getLocation());
                if(distanceDestination <= smallestDistance && !requests.get(i).isPlaceDone()){
                    smallestDistance = distanceDestination;
                    closestRequest = requests.get(i);
                    operation = "PL";
                }
                if(distancePickup <= smallestDistance && !requests.get(i).ispickupDone()){
                    smallestDistance = distancePickup;
                    closestRequest = requests.get(i);
                    operation = "PU";
                }
            }
            if(operation.equals("PL")){
                closestRequest.setPlaceDone();
            }
            else if(operation.equals("PU")){
                closestRequest.setPickupDone();
            }
            if(closestRequest.ispickupDone() && closestRequest.isPlaceDone()){
                requests.remove(closestRequest);
            }
            closestRequest.handleRequest(vehicle, this, output, operation);
        }

    public Stack findStackBasedOnName(String name){
        for (Stack stack : this.stacks) {
            if (stack.getName().equals(name)) {
                return stack;
            }
        }
        return null;
    }
    public void readInput(String file) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(file)));
        JSONObject jsonData = new JSONObject(jsonContent);
        int stackCapacity = jsonData.getInt("stackcapacity");
        int vehicleSpeed = jsonData.getInt("vehiclespeed");
        int loadingDuration = jsonData.getInt("loadingduration");

        JSONArray bufferpoints = jsonData.getJSONArray("bufferpoints");
        JSONObject bufferpointsData = bufferpoints.getJSONObject(0);
        int bufferpointId = bufferpointsData.getInt("ID");
        String bufferpointName = bufferpointsData.getString("name");
        int bufferpointX = bufferpointsData.getInt("x");
        int bufferpointY = bufferpointsData.getInt("y");
        Location bufferpointLocation = new Location(bufferpointX,bufferpointY);
        List<Box> bufferpointBoxes = new ArrayList<>();
        this.stacks.add(new Stack(bufferpointId, bufferpointLocation, 99999999, bufferpointName, bufferpointBoxes));

        JSONArray stacks = jsonData.getJSONArray("stacks");
        for(int i = 0; i < stacks.length(); i++){
            JSONObject stackData = stacks.getJSONObject(i);
            int stackId = stackData.getInt("ID");
            String stackName = stackData.getString("name");
            int stackX = stackData.getInt("x");
            int stackY = stackData.getInt("y");
            List<Box> boxes = new ArrayList<>();
            JSONArray boxArray = stackData.getJSONArray("boxes");
            for(int j = 0; j < boxArray.length(); j++) {
                String boxId = boxArray.getString(j);
                boxes.add(new Box(boxId));
            }
            Location stackLocation = new Location(stackX,stackY);
            Stack stack = new Stack(stackId, stackLocation, stackCapacity, stackName, boxes);
            this.stacks.add(stack);
            for (Box box : stack.getBoxes()) box.setStack(stack);
        }
        JSONArray vehicles = jsonData.getJSONArray("vehicles");
        for(int k = 0; k < vehicles.length(); k++){
            JSONObject vehicleData = vehicles.getJSONObject(k);
            int vehicleId = vehicleData.getInt("ID");
            String vehicleName = vehicleData.getString("name");
            int vehicleCapacity = vehicleData.getInt("capacity");
            int vehicleX = vehicleData.getInt("xCoordinate");
            int vehicleY =  vehicleData.getInt("yCoordinate");
            Location vehicleLocation = new Location(vehicleX,vehicleY);
            this.vehicles.add(new Vehicle(vehicleId, vehicleLocation, vehicleSpeed, vehicleCapacity, vehicleName, loadingDuration));
        }
        JSONArray requests = jsonData.getJSONArray("requests");
        for(int l = 0; l < requests.length(); l++){
            JSONObject requestData = requests.getJSONObject(l);
            int requestId = requestData.getInt("ID");
            JSONArray pickupArray = requestData.getJSONArray("pickupLocation");
            String pickupLocationName = pickupArray.getString(0);
            Stack pickupLocationStack = findStackBasedOnName(pickupLocationName);
            JSONArray placeArray = requestData.getJSONArray("placeLocation");
            String placeLocationName = placeArray.getString(0);
            Stack placeLocationStack = findStackBasedOnName(placeLocationName);
            String boxIdStack = requestData.getString("boxID");
            Box pickupBox = pickupLocationStack.findBoxById(boxIdStack);
            this.requests.add(new Request(requestId, pickupLocationStack, placeLocationStack, pickupBox));
        }
    }
}
