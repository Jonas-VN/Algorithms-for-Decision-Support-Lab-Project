package Output;

import Warehouse.Operation;
import Warehouse.Request;
import Warehouse.Vehicle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputWriter {
    File outputFile;
    int counter = 0;

    public OutputWriter(File outputFile) throws IOException {
        System.out.println("Output file: " + outputFile.getAbsolutePath());
        this.outputFile = outputFile;
        if (outputFile.exists()) outputFile.delete();
        outputFile.createNewFile();
    }

    public void writeLine(Vehicle vehicle, int endTime, Operation operation) {
        Request request = vehicle.getCurrentRequest();
        String line = (vehicle.getName() + ";" +
                request.getVehicleStartLocation().getX() + ";" +
                request.getVehicleStartLocation().getY() + ";" +
                request.getStartTime() + ";" +
                vehicle.getLocation().getX() + ";" +
                vehicle.getLocation().getY() + ";" +
                endTime + ";" +
                request.getBox().getId() + ";" +
                operation);
        System.out.println(++counter + ": " + line);
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
