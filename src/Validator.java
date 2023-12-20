import Utils.Location;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;


public class Validator {
    public static void main(String[] args) throws IOException {
        String outputFile = Main.problem;
        HashMap<Location, Integer> lastAccessed = new HashMap<>();
        int warnings = 0;
        int errors = 0;

        String csvFile;
        boolean doEarlyMovesToStack = Main.doEarlyMovesToStack;
        if (!doEarlyMovesToStack) csvFile = "src/Output/src/output" + outputFile + ".txt";
        else csvFile = "src/Output/src2/output" + outputFile + ".txt";
        System.out.println(csvFile);

        String jsonContent = new String(Files.readAllBytes(new File("src/Input/src/I" + outputFile + ".json").toPath()));
        JSONObject jsonData = new JSONObject(jsonContent);
        final int loadDuration = jsonData.getInt("loadingduration");
        final int speed = jsonData.getInt("vehiclespeed");

        ArrayList<Location> bufferPointLocations = new ArrayList<>();
        JSONArray bufferPoints = jsonData.getJSONArray("bufferpoints");
        for (int idx = 0; idx < bufferPoints.length(); idx++) {
            JSONObject bufferPointData = bufferPoints.getJSONObject(idx);
            int bufferPointX = bufferPointData.getInt("x");
            int bufferPointY = bufferPointData.getInt("y");
            bufferPointLocations.add(new Location(bufferPointX, bufferPointY));
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            int lineCount = 0;
            String line;
            Location startLocation;
            Location destinationLocation;
            int startTime;
            int endTime;
            int timeNeeded;

            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] values = line.split(";");
                startLocation = new Location(Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                destinationLocation = new Location(Integer.parseInt(values[4]), Integer.parseInt(values[5]));
                startTime = Integer.parseInt(values[3]);
                endTime = Integer.parseInt(values[6]);
                
                if (!lastAccessed.containsKey(destinationLocation)) lastAccessed.put(destinationLocation, 0);

                timeNeeded = startLocation.manhattanDistance(destinationLocation) / speed;
                if (startTime + timeNeeded + loadDuration < endTime) {
                    System.out.println(lineCount + " WARNING: LOST TIME " + line);
                    warnings++;
                }
                if (startTime + timeNeeded + loadDuration > endTime) {
                    System.out.println(lineCount + "ERROR: UNFEASIBLE TIME " + line);
                    errors++;
                }

                // If a vehicle freed a stack at time t then next vehicle can free it as soon as t + loadDuration
                if (lastAccessed.get(destinationLocation) + loadDuration > endTime && !bufferPointLocations.contains(destinationLocation)) {
                    System.out.println(lineCount + " ERROR: UNFEASIBLE LOADING " + line);
                    errors++;
                }
                lastAccessed.put(destinationLocation, endTime);

                // No need to validate if the storages would be full or empty since our simulation would have thrown an error
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Finished validating with " + warnings + " warnings and " + errors + " errors.");
        }
    }
}
