package Warehouse;

import Output.OutputWriter;
import Utils.Location;
import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;

import java.util.ArrayList;

public class Vehicle {
    private final int id;
    private Location location;
    private final int speed;
    private final int capacity;
    private final String name;
    private final int loadingDuration;
    private final ArrayList<Box> stack;
    private final ArrayList<Request> requests = new ArrayList<>();
    private Request currentRequest = null;
    private VehicleState state = VehicleState.IDLE;
    private int timeFinishState = 0;
    private int lastRequestFinishedTime = 0;
    private final OutputWriter outputWriter;
    private Storage freedStorage = null;

    public Vehicle(int id, Location location, int speed, int capacity, String name, int loadingDuration, OutputWriter outputWriter) {
        this.id = id;
        this.location = location;
        this.speed = speed;
        this.capacity = capacity;
        this.name = name;
        this.loadingDuration = loadingDuration;
        this.stack = new ArrayList<>(capacity);
        this.outputWriter = outputWriter;
    }

    public Request getCurrentRequest() {
        return this.currentRequest;
    }

    public void initNextState(VehicleState vehicleState, int timeFinishState) {
        this.state = vehicleState;
        this.timeFinishState = timeFinishState;
    }

    public VehicleState getState() {
        return this.state;
    }

    public void addRequest(Request request, int time) throws StackIsFullException, BoxNotAccessibleException {
        if (this.requests.size() >= this.capacity) throw new StackIsFullException("Vehicle " + this.id + " is full.");
        this.currentRequest = request;
        this.requests.add(request);

        if (this.location == request.getPickup().getLocation()) {
            // Vehicle is already at the pickup location -> start loading
            this.initNextState(VehicleState.LOADING, time + this.loadingDuration);
            this.currentRequest.getPickup().setUsedByVehicle(this.id);
            this.initCurrentRequest();
            this.loadBox(this.currentRequest.getBox());
            this.load(time);
        }
        else {
            // Vehicle is not at the pickup location -> start moving there
            int timeFinishState = time + this.location.manhattanDistance(request.getPickup().getLocation()) / this.speed;
            this.initNextState(VehicleState.MOVING_TO_PICKUP, timeFinishState);
            this.initCurrentRequest();

            // Claim the pickup location before we start driving there (if possible)
            if (this.currentRequest.getPickup().canBeUsedByVehicle(this.id))
                this.currentRequest.getPickup().setUsedByVehicle(this.id);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getId(){
        return this.id;
    }

    public boolean isFull(){
        return this.stack.size() == this.capacity;
    }
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public Location getLocation(){
        return this.location;
    }

    public void loadBox(Box box) throws BoxNotAccessibleException, StackIsFullException {
        if (this.isFull()) throw new StackIsFullException("Vehicle " + this.id + " is full! Tried to add box: " + box.getId());

        box.getStack().removeBox(box);
        box.setStack(null);
        this.stack.add(box);
    }

    public void unloadBox(Box box, Storage storage) throws BoxNotAccessibleException, StackIsFullException {
        if (!this.stack.contains(box)) throw new BoxNotAccessibleException("Vehicle " + this.id + " does not contain box " + box.getId() + "!");

        storage.addBox(box);
        box.setStack(storage);
        this.stack.remove(box);
    }

    public void unload(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (time == this.timeFinishState) {
            this.lastRequestFinishedTime = time;
            this.outputWriter.writeLine(this, time, Operation.UNLOAD);

             // Fulfilled the full  -> can be removed
            this.requests.remove(this.currentRequest);

            // The storage is free for everyone again
            this.currentRequest.getDestination().resetUsedByVehicle();
            this.freedStorage = this.currentRequest.getDestination();

            // Control next state
            if (this.requests.isEmpty()) {
                this.currentRequest = null;
                // Delivered all requests/boxes, let the warehouse decide what to do next
                this.state = VehicleState.IDLE;
            }
            else {
                // Vehicle is not empty -> start moving to next delivery
                this.currentRequest = this.requests.get(0);
                this.setupMoveToDelivery(time);
                this.moveToDelivery(time);
            }
        }
    }

    public void load(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (time == this.timeFinishState) {
            this.lastRequestFinishedTime = time;
            this.outputWriter.writeLine(this, time, Operation.LOAD);

            this.currentRequest.getPickup().resetUsedByVehicle();
            this.freedStorage = this.currentRequest.getPickup();

            // Control next state
            if (this.isFull()) {
                // Vehicle is full -> start moving to delivery
                // TODO: maybe sort on distance here and change the currentRequest to the closest one
                this.setupMoveToDelivery(time);
                this.moveToDelivery(time);

            }
            // Let the warehouse decide what to do next (find the next pickup)
            else this.state = VehicleState.IDLE;
        }
    }

    public void setupMoveToDelivery(int time) {
        int timeFinishState = time + this.location.manhattanDistance(this.currentRequest.getDestination().getLocation()) / this.speed;
        this.initNextState(VehicleState.MOVING_TO_DELIVERY, timeFinishState);
        this.initCurrentRequest();

        // If the destination storage is already claimed, we still drive there and hope it will be free when we get there
        if (this.currentRequest.getDestination().canBeUsedByVehicle(this.id))
            this.currentRequest.getDestination().setUsedByVehicle(this.id);
    }

    public void moveToDelivery(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.canStartUnloading(time)) {
            // Vehicle arrived at delivery -> start unloading
            this.location = this.currentRequest.getDestination().getLocation();
            this.initNextState(VehicleState.UNLOADING, time + this.loadingDuration);
            this.currentRequest.getDestination().setUsedByVehicle(this.id);
            this.unloadBox(this.currentRequest.getBox(), this.currentRequest.getDestination());
            this.unload(time);
        }
    }

    private boolean canStartUnloading(int time) {
        // (We have arrived at the delivery location OR we were already there) AND the delivery location is free
        boolean timeIsUp = time >= this.timeFinishState;
        boolean locationIsSame = this.location.equals(this.currentRequest.getDestination().getLocation());
        boolean canBeUsedByVehicle = this.currentRequest.getDestination().canBeUsedByVehicle(this.id);
        return (timeIsUp || locationIsSame) && canBeUsedByVehicle && !this.currentRequest.getDestination().isFull();
    }

    public void moveToPickup(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.canStartLoading(time)) {
            this.location = this.currentRequest.getPickup().getLocation();
            this.initNextState(VehicleState.LOADING, time + this.loadingDuration);
            this.currentRequest.getPickup().setUsedByVehicle(this.id);
            this.loadBox(this.currentRequest.getBox());
            this.load(time);
        }
    }

    private boolean canStartLoading(int time) {
        // (We have arrived at the pickup location OR we were already there) AND the pickup location is free
        boolean timeIsUp = time >= this.timeFinishState;
        boolean locationIsSame = this.location.equals(this.currentRequest.getPickup().getLocation());
        boolean canBeUsedByVehicle = this.currentRequest.getPickup().canBeUsedByVehicle(this.id);
        return (timeIsUp || locationIsSame) && canBeUsedByVehicle;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + this.id +
                ", location=" + this.location +
                ", name=" + this.name +
                ", boxes=" + this.stack +
                ", requests=" + this.requests +
                ", currentRequest=" + this.currentRequest +
                ", state=" + this.state +
                ", timeFinishState=" + this.timeFinishState +
                '}';
    }

    public void initCurrentRequest() {
        this.currentRequest.setStartTime(this.lastRequestFinishedTime);
        this.currentRequest.setVehicleStartLocation(this.location);
    }

    public void skipTick() {
        this.timeFinishState--;
    }

    public Storage getFreedStorage() {
        Storage returnStorage = this.freedStorage;
        this.freedStorage = null;
        return returnStorage;
    }
}
