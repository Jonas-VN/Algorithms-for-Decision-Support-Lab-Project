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

    public Request(int id, Stack pickup, Stack destination, Box box){
        this.id = id;
        this.pickup = pickup;
        this.destination = destination;
        this.box = box;
    }

    public void handleRequest(Vehicle vehicle, Warehouse warehouse, File output){
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
        String line = (vehicle.getName() + ";" + startX + ";" + startY + ";" + startTime + ";" + endX + ";" + endY + ";" + endTime + ";" + this.box.getId() + ";" + "PU");

        try(FileWriter fw = new FileWriter(output, true);
            BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
