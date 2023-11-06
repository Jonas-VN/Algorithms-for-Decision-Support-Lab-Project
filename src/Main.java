import Warehouse.Warehouse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String problem = "10_10_1";
        Warehouse warehouse = new Warehouse(problem);
        warehouse.solve();
    }
}