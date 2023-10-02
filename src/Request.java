public class Request {
    private int id;
    private Stack pickup;
    private Stack destination;
    private Box box;

    public Request(int id, Stack pickup, Stack destination, Box box){
        this.id = id;
        this.pickup = pickup;
        this.destination = destination;
        this.box = box;
    }

    public void handleRequest(Vehicle vehicle){
        vehicle.moveTo(this.pickup.getLocation());
        vehicle.loadBox(this.box);
        vehicle.moveTo(this.destination.getLocation());
        vehicle.unloadBox(this.box, destination);
    }
}
