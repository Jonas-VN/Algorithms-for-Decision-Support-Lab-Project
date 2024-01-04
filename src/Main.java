import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;
import Warehouse.Warehouse;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static String problem;
//        public static String problem = "3_3_1";
//        public static String problem = "3_3_1_5";
//        public static String problem = "10_10_1";
//        public static String problem = "15_16_1_3";
//        public static String problem = "20_20_2_2_8b2";
//        public static String problem = "30_100_1_1_10";
//        public static String problem = "30_100_3_3_10";
//        public static String problem = "30_200_3_3_10";
//        public static String problem = "100_50_2_2_8b2";
//        public static String problem = "100_120_2_2_8b2";
//        public static String problem = "100_500_3_1_20b2";
//        public static String problem = "100_500_3_5_20";
//        public static String problem = "100_800_3_1_20b2";
//        public static String problem = "100_800_1_1_20b2";

       public static boolean doEarlyMovesToStack = true;
    public static void main(String[] args) throws IOException, BoxNotAccessibleException, StackIsFullException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Give input file path:");
        problem = scanner.nextLine();
        Warehouse warehouse = new Warehouse(problem, doEarlyMovesToStack);
        warehouse.solve();
    }
}