public class Main {

    public static void main(String[] args){
        Warehouse warehouse = new Warehouse(5, 3);
        Stack buffer = new Stack(0, new Location(0,0), 9999999);
        Stack stack1 = new Stack(1, new Location(3,4), 10);
        warehouse.addStack(buffer);
        warehouse.addStack(stack1);
        Vehicle vehicle1 = new Vehicle(1, new Location(0,0), 4, 10);
        warehouse.addVehicle(vehicle1);
        Box box1 = new Box(1);
        buffer.addBox(box1);
        Request request1 = new Request(1, buffer, stack1, box1);
        request1.handleRequest(vehicle1);
    }
}
