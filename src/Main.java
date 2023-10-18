public class Main {

    public static void main(String[] args){
        Warehouse warehouse = new Warehouse(5, 3);
        Stack buffer = new Stack(0, new Location(0,0), 9999999, "stack_0");
        Stack stack1 = new Stack(1, new Location(3,4), 10, "stack_1");
        warehouse.addStack(buffer);
        warehouse.addStack(stack1);
        Vehicle vehicle1 = new Vehicle(1, new Location(0,0), 4, 10, "vehicle_1");
        warehouse.addVehicle(vehicle1);
        Box box1 = new Box("B01");
        buffer.addBox(box1);
        Request request1 = new Request(1, buffer, stack1, box1);
        System.out.println(vehicle1.getLocation().getX() + "," + vehicle1.getLocation().getY());
        request1.handleRequest(vehicle1);
        System.out.println(vehicle1.getLocation().getX() + "," + vehicle1.getLocation().getY());
    }
}
