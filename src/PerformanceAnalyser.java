import java.io.BufferedReader;
import java.io.FileReader;

public class PerformanceAnalyser {
    public static int getTotalEndTime(String csvFile) {
        int totalEndTime = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while (br.readLine() != null) {
                String[] values = br.readLine().split(";");
                totalEndTime = Integer.parseInt(values[6]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return totalEndTime;
        }
    }
    public static void main(String[] args) {
        String outputFile = Main.problem;

        int normalEnd, optimisedEnd;

        String normalCsvFile = "src/Output/src/output" + outputFile + ".txt";
        String optimisedCsvFile = "src/Output/src2/output" + outputFile + ".txt";

        normalEnd = getTotalEndTime(normalCsvFile);
        optimisedEnd = getTotalEndTime(optimisedCsvFile);

        float percentage = 100 - ((float) optimisedEnd / normalEnd * 100);
        System.out.println("Optimised solution is " + percentage + "% faster than normal solution");

    }
}
