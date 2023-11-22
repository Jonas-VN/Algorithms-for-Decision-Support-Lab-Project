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
    private int timeToFinishState = 0;
    private final OutputWriter outputWriter;
    private boolean doneAllRequests = false;

    public Vehicle(int id, Location location, int speed, int capacity, String name, int loadingDuration, OutputWriter outputWriter){
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
        return currentRequest;
    }

    public void initNextState(VehicleState vehicleState, int timeToFinishState) {
        this.state = vehicleState;
        this.timeToFinishState = timeToFinishState;
    }

    public int getAndDecrementTimeToFinishState() {
        return this.timeToFinishState--;
    }

    public VehicleState getState() {
        return this.state;
    }

    public void addRequest(Request request, int time) throws StackIsFullException {
        if (this.requests.size() >= this.capacity) throw new StackIsFullException("Vehicle " + this.id + " is full.");
        request.setStartTime(time);
        request.setVehicleStartLocation(this.location);
        this.currentRequest = request;
        this.requests.add(request);
    }

    public Request currentRequest(){
        return this.currentRequest;
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

    public boolean isEmpty(){
        return this.stack.isEmpty();
    }

    public Location getLocation(){
        return this.location;
    }

    public int getSpeed(){
        return this.speed;
    }

    public void loadBox(Box box) throws BoxNotAccessibleException, StackIsFullException {
        if (this.isFull()) throw new StackIsFullException("Vehicle " + this.id + " is full!");

        // The box has been loaded, so the stack is free again
        box.getStack().resetUsedByVehicle();
        box.getStack().removeBox(box);
        this.stack.add(box);
        box.setStack(null);
    }

    public void unloadBox(Box box, Storage storage) throws BoxNotAccessibleException, StackIsFullException {
        if (!this.stack.contains(box)) throw new BoxNotAccessibleException("Vehicle " + this.id + " does not contain box " + box.getId() + "!");

        // The box has been unloaded, so the storage is free again
        storage.resetUsedByVehicle();
        this.stack.remove(box);
        storage.addBox(box);
    }

    public void unload(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.getAndDecrementTimeToFinishState() == 0) {
            // Vehicle finished unloading
            this.unloadBox(this.currentRequest().getBox(), this.currentRequest().getDestination());
            this.outputWriter.writeLine(this, time, Operation.UNLOAD);
            this.requests.remove(this.currentRequest);
            // TODO: implement some logic to make it more efficient
            if (this.requests.isEmpty()) {
                this.state = VehicleState.IDLE;
                return;
            }
            this.currentRequest = this.requests.get(0);

            if (this.isEmpty()) {
                // Vehicle is empty -> set to idle (let the warehouse decide what to do next)
                this.state = VehicleState.IDLE;
            }
            else {
                // Vehicle is not empty -> start moving to next delivery
                this.initCurrentRequest(time);
                int timeToFinishState = this.location.manhattanDistance(this.currentRequest().getDestination().getLocation()) / this.speed;
                this.initNextState(VehicleState.MOVING_TO_DELIVERY, timeToFinishState);
                this.moveToDelivery(time);
            }
        }
    }

    public boolean load(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.getAndDecrementTimeToFinishState() == 0) {
            // Vehicle finished loading
            this.loadBox(this.currentRequest().getBox());
            this.outputWriter.writeLine(this, time, Operation.LOAD);


            if (this.isFull() || this.doneAllRequests) {
                // Vehicle is full -> start moving to delivery
                this.initCurrentRequest(time);
                int timeToFinishState = this.location.manhattanDistance(this.currentRequest().getDestination().getLocation()) / this.speed;
                this.initNextState(VehicleState.MOVING_TO_DELIVERY, timeToFinishState);
                this.moveToDelivery(time);
            }
            // Else the vehicle is not done yet, let the warehouse decide what to do next
            return true;
        }
        return false;
    }

    public void moveToDelivery(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.canStartUnloading()) {
            // Vehicle arrived at delivery -> start unloading
            this.location = this.currentRequest().getDestination().getLocation();
            this.initNextState(VehicleState.UNLOADING, this.loadingDuration);
            this.currentRequest().getDestination().setUsedByVehicle(this.id);
            this.unload(time);
        }
    }

    private boolean canStartUnloading() {
        // (We have arrived at the delivery location OR we were already there) AND the delivery location is free
        boolean timeIsUp = this.getAndDecrementTimeToFinishState() <= 0;
        boolean locationIsSame = this.location.equals(this.currentRequest().getDestination().getLocation());
        boolean canBeUsedByVehicle = this.currentRequest().getDestination().canBeUsedByVehicle(this.id);
        return (timeIsUp || locationIsSame) && canBeUsedByVehicle;
    }

    public void moveToPickup(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.canStartLoading()) {
            // Vehicle arrived at pickup -> start loading
            this.location = this.currentRequest().getPickup().getLocation();
            this.initNextState(VehicleState.LOADING, this.loadingDuration);
            this.currentRequest().getPickup().setUsedByVehicle(this.id);
            this.load(time);
            // IDK why, but it works
            getAndDecrementTimeToFinishState();
        }
    }

    private boolean canStartLoading() {
        // (We have arrived at the pickup location OR we were already there) AND the pickup location is free
        boolean timeIsUp = this.getAndDecrementTimeToFinishState() <= 0;
        boolean locationIsSame = this.location.equals(this.currentRequest().getPickup().getLocation());
        boolean canBeUsedByVehicle = this.currentRequest().getPickup().canBeUsedByVehicle(this.id);
        return (timeIsUp || locationIsSame) && canBeUsedByVehicle;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + this.id +
                ", location=" + this.location +
                ", speed=" + this.speed +
                ", capacity=" + this.capacity +
                ", boxes=" + this.stack +
                ", name='" + this.name + '\'' +
                ", loadingDuration=" + this.loadingDuration +
                '}';
    }

    public void initCurrentRequest(int time) {
        this.currentRequest().setStartTime(time);
        this.currentRequest().setVehicleStartLocation(this.location);
    }

    public void setDoneAllRequests(boolean doneAllRequests) {
        this.doneAllRequests = doneAllRequests || this.capacity == this.requests.size() - 1;
    }
}

