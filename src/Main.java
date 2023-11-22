import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;
import Warehouse.Warehouse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, BoxNotAccessibleException, StackIsFullException {
//       String problem = "3_3_1";
//        String problem = "3_3_1_2vehicles";
//        String problem = "3_3_1_5";
//        String problem = "10_10_1";
        String problem = "10_10_1_2vehicles";
//        String problem = "15_16_1_3";
//        String problem = "20_20_2_2_8b2";
        Warehouse warehouse = new Warehouse(problem);
        warehouse.solve();
    }
}