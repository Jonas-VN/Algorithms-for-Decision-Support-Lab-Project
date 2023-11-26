package Validator;

import Utils.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Validator {
    public static void main(String[] args) {
        String outputFile = "100_120_2_2_8b2";
        final int loadDuration = 5;

        String csvFile = "src/Output/src/output" + outputFile + ".txt";
        System.out.println(csvFile);
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            int lineCount = 0;
            String line;
            Location startLocation;
            Location destinationLocation;
            int startTime;
            int endTime;
            int distance;

            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] values = line.split(";");
                startLocation = new Location(Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                destinationLocation = new Location(Integer.parseInt(values[4]), Integer.parseInt(values[5]));
                startTime = Integer.parseInt(values[3]);
                endTime = Integer.parseInt(values[6]);

                distance = startLocation.manhattanDistance(destinationLocation);
                if (startTime + distance + loadDuration < endTime)
                    System.out.println(lineCount + ": LOST TIME: " + line);
                else if (startTime + distance + loadDuration > endTime)
                    System.out.println(lineCount + ": UNFEASIBLE: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Finished validating");
        }
    }
}
