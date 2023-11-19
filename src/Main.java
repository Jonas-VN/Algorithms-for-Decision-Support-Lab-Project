import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;
import Warehouse.Warehouse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, BoxNotAccessibleException, StackIsFullException {
//        String problem = "3_3_1";
//        String problem = "3_3_1_5";
//        String problem = "10_10_1";
        String problem = "15_16_1_3";
        Warehouse warehouse = new Warehouse(problem);
        warehouse.solve();
    }
}