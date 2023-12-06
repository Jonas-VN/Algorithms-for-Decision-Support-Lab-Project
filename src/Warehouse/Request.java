package Warehouse;


import Utils.Location;

public class Request {
    private final int id;
    private final Storage pickup;
    private final Storage destination;
    private final Box box;
    private int startTime = 0;
    private Location vehicleStartLocation = null;
    private boolean claimed = false;

    public Request(int id, Storage pickup, Storage destination, Box box){
        this.id = id;
        this.pickup = pickup;
        this.destination = destination;
        this.box = box;
    }

    public boolean isClaimed() {
        return this.claimed;
    }

    public void claim() {
        this.claimed = true;
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
        return "\nRequest{" +
                "id=" + id +
                ", pickup=" + pickup +
                ", destination=" + destination +
                ", box=" + box +
                ", startTime=" + startTime +
                ", vehicleStartLocation=" + vehicleStartLocation +
                '}';
    }

    public static int compareTo(Request lhs, Request rhs, Vehicle vehicle) {
        double lhsDistance = vehicle.getLocation().manhattanDistance(lhs.getPickup().getLocation());
        // Add some extra distance behind the decimal point to prefer boxes that are on top of a stack
        lhsDistance += (double) lhs.getPickup().numberOfBoxesOnTop(lhs.getBox()) / 10;

        double rhsDistance = vehicle.getLocation().manhattanDistance(rhs.getPickup().getLocation());
        // Add some extra distance behind the decimal point to prefer boxes that are on top of a stack
        rhsDistance += (double) rhs.getPickup().numberOfBoxesOnTop(rhs.getBox()) / 10;

        return Double.compare(lhsDistance, rhsDistance);
    }
}

