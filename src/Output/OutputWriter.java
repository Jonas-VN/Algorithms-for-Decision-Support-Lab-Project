package Output;

import Utils.Location;
import Warehouse.Box;
import Warehouse.Operation;
import Warehouse.Request;
import Warehouse.Vehicle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputWriter {
    File outputFile;

    public OutputWriter(File outputFile) throws IOException {
        this.outputFile = outputFile;
        if (outputFile.exists()) outputFile.delete();
        outputFile.createNewFile();
    }

    public void writeLine(Vehicle vehicle, Request request, int endTime, Operation operation) {
        String line = (vehicle.getName() + ";" +
                request.getPickup().getLocation().getX() + ";" +
                request.getPickup().getLocation().getY() + ";" +
                request.getStartTime() + ";" +
                request.getDestination().getLocation().getX() + ";" +
                request.getDestination().getLocation().getY() + ";" +
                endTime + ";" +
                request.getBox().getId() + ";" +
                operation);
        try {
            FileWriter fw = new FileWriter(outputFile, true);
            fw.write(line);
            fw.write("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
