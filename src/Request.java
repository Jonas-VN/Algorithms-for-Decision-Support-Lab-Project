import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;

public class Request {
    private int id;
    private Stack pickup;
    private Stack destination;
    private Box box;
    private boolean pickupDone;
    private boolean placeDone;

    public Request(int id, Stack pickup, Stack destination, Box box){
        this.id = id;
        this.pickup = pickup;
        this.destination = destination;
        this.box = box;
    }

    public Stack getPickup(){
        return pickup;
    }
    public Stack getDestination(){
        return destination;
    }
    public boolean ispickupDone(){
        return pickupDone;
    }
    public boolean isPlaceDone(){
        return pickupDone;
    }
    public void setPickupDone(){
        pickupDone = true;
    }
    public void setPlaceDone(){
        placeDone = true;
    }

    public void handleRequest(Vehicle vehicle, Warehouse warehouse, File output, String operation){
        int startX = vehicle.getLocation().getX();
        int startY = vehicle.getLocation().getY();
        int startTime = warehouse.getTime();
//        vehicle.moveTo(this.pickup.getLocation());
//        vehicle.loadBox(this.box);
//        vehicle.moveTo(this.destination.getLocation());
//        vehicle.unloadBox(this.box, destination);
        int endX = vehicle.getLocation().getX();
        int endY = vehicle.getLocation().getY();
        int endTime = warehouse.getTime();
        String line = (vehicle.getName() + ";" + startX + ";" + startY + ";" + startTime + ";" + endX + ";" + endY + ";" + endTime + ";" + this.box.getId() + ";" + operation);

        try(FileWriter fw = new FileWriter(output, true);
            BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
