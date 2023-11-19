package Input;

import Output.OutputWriter;
import Utils.Location;
import Warehouse.*;
import Warehouse.Exceptions.StackIsFullException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class JSONParser {
    JSONObject jsonData;

    public JSONParser(File jsonFile) throws IOException {
        String jsonContent = new String(Files.readAllBytes(jsonFile.toPath()));
        this.jsonData = new JSONObject(jsonContent);
    }

    private Storage findStackBasedOnName(ArrayList<Storage> stacks, String name){
        for (Storage stack : stacks) {
            if (stack.getName().equals(name)) {
                return stack;
            }
        }
        return null;
    }

    public ArrayList<Storage> parseBufferPoints() {
        ArrayList<Storage> bufferPointsList = new ArrayList<>();
        JSONArray bufferPoints = jsonData.getJSONArray("bufferpoints");

        for (int idx = 0; idx < bufferPoints.length(); idx++) {
            JSONObject bufferPointData = bufferPoints.getJSONObject(idx);

            int bufferPointId = bufferPointData.getInt("ID");
            String bufferPointName = bufferPointData.getString("name");
            int bufferPointX = bufferPointData.getInt("x");
            int bufferPointY = bufferPointData.getInt("y");
            Location bufferPointLocation = new Location(bufferPointX,bufferPointY);
            HashMap<String, Box> bufferPointBoxes = new HashMap<>();
            bufferPointsList.add(new BufferPoint(bufferPointId, bufferPointLocation, Integer.MAX_VALUE, bufferPointName, bufferPointBoxes));
        }

        return bufferPointsList;
    }

    public ArrayList<Stack> parseStacks() throws StackIsFullException {
        ArrayList<Stack> stacksList = new ArrayList<>();
        JSONArray stacks = jsonData.getJSONArray("stacks");
        int stackCapacity = jsonData.getInt("stackcapacity");

        for (int idx = 0; idx < stacks.length(); idx++) {
            JSONObject stackData = stacks.getJSONObject(idx);

            int stackId = stackData.getInt("ID");
            String stackName = stackData.getString("name");
            int stackX = stackData.getInt("x");
            int stackY = stackData.getInt("y");
            Location stackLocation = new Location(stackX,stackY);
            Stack stack = new Stack(stackId, stackLocation, stackCapacity, stackName);
            stacksList.add(stack);

            // Add the boxes to the stack
            JSONArray boxArray = stackData.getJSONArray("boxes");
            for (int idxx = 0; idxx < boxArray.length(); idxx++) {
                String boxId = boxArray.getString(idxx);
                Box box = new Box(boxId);
                box.setStack(stack);
                stack.addBox(box);
            }
        }

        return stacksList;
    }

    public ArrayList<Vehicle> parseVehicles(OutputWriter outputWriter) {
        ArrayList<Vehicle> vehiclesList = new ArrayList<>();
        JSONArray vehicles = jsonData.getJSONArray("vehicles");
        int vehicleSpeed = jsonData.getInt("vehiclespeed");
        int loadingDuration = jsonData.getInt("loadingduration");

        for (int idx = 0; idx < vehicles.length(); idx++) {
            JSONObject vehicleData = vehicles.getJSONObject(idx);

            int vehicleId = vehicleData.getInt("ID");
            String vehicleName = vehicleData.getString("name");
            int vehicleCapacity = vehicleData.getInt("capacity");
            int vehicleX = vehicleData.getInt("xCoordinate");
            int vehicleY =  vehicleData.getInt("yCoordinate");
            Location vehicleLocation = new Location(vehicleX,vehicleY);

            vehiclesList.add(new Vehicle(vehicleId, vehicleLocation, vehicleSpeed, vehicleCapacity, vehicleName, loadingDuration, outputWriter));
        }

        return vehiclesList;
    }

    public ArrayList<Request> parseRequests(ArrayList<Storage> stacks) throws StackIsFullException {
        ArrayList<Request> requestsList = new ArrayList<>();
        JSONArray requests = jsonData.getJSONArray("requests");

        for (int idx = 0; idx < requests.length(); idx++) {
            JSONObject requestData = requests.getJSONObject(idx);
            int requestId = requestData.getInt("ID");

            JSONArray pickupArray = requestData.getJSONArray("pickupLocation");
            String pickupLocationName = pickupArray.getString(0);
            Storage pickupLocationStack = findStackBasedOnName(stacks, pickupLocationName);

            JSONArray placeArray = requestData.getJSONArray("placeLocation");
            String placeLocationName = placeArray.getString(0);
            Storage placeLocationStack = findStackBasedOnName(stacks, placeLocationName);

            String boxIdStack = requestData.getString("boxID");
            Box pickupBox = pickupLocationStack.findBoxById(boxIdStack);
            if (pickupBox == null) {
                // Box wasn't found in the pickup stack, so it must be in the BufferPoint
                pickupLocationStack.addBox(pickupBox = new Box(boxIdStack));
                pickupBox.setStack(pickupLocationStack);
            }

            requestsList.add(new Request(requestId, pickupLocationStack, placeLocationStack, pickupBox));
        }

        return requestsList;
    }

}