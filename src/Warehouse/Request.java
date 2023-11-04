package Warehouse;


import Utils.Location;

public class Request {
    private final int id;
    private final Storage pickup;
    private final Storage destination;
    private final Box box;
    private int startTime = 0;
    private Location vehicleStartLocation = null;

    public Request(int id, Storage pickup, Storage destination, Box box){
        this.id = id;
        this.pickup = pickup;
        this.destination = destination;
        this.box = box;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setVehicleStartLocation(Location vehicleStartLocation) {
        this.vehicleStartLocation = vehicleStartLocation;
    }

    public Location getVehicleStartLocation() {
        return vehicleStartLocation;
    }

    public int getId() {
        return id;
    }

    public Storage getPickup() {
        return pickup;
    }

    public Storage getDestination() {
        return destination;
    }

    public Box getBox() {
        return box;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", pickup=" + pickup +
                ", destination=" + destination +
                ", box=" + box +
                '}';
    }

    public static int compareTo(Request lhs, Request rhs, Vehicle vehicle) {
        double lhsDistance = vehicle.getLocation().manhattenDistance(lhs.getPickup().getLocation());
        double rhsDistance = vehicle.getLocation().manhattenDistance(rhs.getPickup().getLocation());
        return Double.compare(lhsDistance, rhsDistance);
    }
}

