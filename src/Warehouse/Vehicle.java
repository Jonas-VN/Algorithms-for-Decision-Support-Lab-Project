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
    private final Stack stack;
    private final ArrayList<Request> queue = new ArrayList<>();
    private int queueIndex = 0;
    private VehicleState state = VehicleState.IDLE;
    private int timeToFinishState = 0;
    private final OutputWriter outputWriter;
    private final ArrayList<Request> undoRelocationRequests = new ArrayList<>();
    private boolean doneAllRequests = false;

    public Vehicle(int id, Location location, int speed, int capacity, String name, int loadingDuration, OutputWriter outputWriter){
        this.id = id;
        this.location = location;
        this.speed = speed;
        this.capacity = capacity;
        this.name = name;
        this.loadingDuration = loadingDuration;
        this.stack = new Stack(-1, null, capacity, "Vehicle " + id);
        this.outputWriter = outputWriter;
    }

    public void clearRequests() {
        this.queue.clear();
        this.queueIndex = 0;
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

    public void addRequest(Request request) throws StackIsFullException {
        if (this.queue.size() >= this.capacity) throw new StackIsFullException("Vehicle " + this.id + " is full.");
        else this.queue.add(request);
    }

    public Request getCurrentRequest(){
        return this.queue.get(this.queueIndex);
    }

    public String getName() {
        return this.name;
    }

    public int getId(){
        return this.id;
    }

    public boolean isFull(){
        return this.stack.isFull();
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
        if (!this.isFull()) {
            box.getStack().removeBox(box);
            this.stack.addBox(box);
            box.setStack(this.stack);
        }
        else {
            throw new StackIsFullException("Vehicle " + this.id + " is full!");
        }
        // The box has been loaded, so the stack is free again
        box.getStack().resetUsedByVehicle();
    }

    public void unloadBox(Box box, Storage storage) throws BoxNotAccessibleException, StackIsFullException {
        if (this.stack.contains(box)){
            this.stack.removeBox(box);
            storage.addBox(box);
        }
        else {
            throw new BoxNotAccessibleException("Vehicle " + this.id + " does not contain box " + box.getId() + "!");
        }
        // The box has been unloaded, so the storage is free again
        storage.resetUsedByVehicle();
    }

    public void unload(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.getAndDecrementTimeToFinishState() == 0) {
            // Vehicle finished unloading
            this.unloadBox(this.getCurrentRequest().getBox(), this.getCurrentRequest().getDestination());
            this.outputWriter.writeLine(this, time, Operation.UNLOAD);

            if (this.isEmpty()) {
                // Vehicle is empty -> set to idle (let the warehouse decide what to do next)
                this.state = VehicleState.IDLE;
            }
            else {
                // Vehicle is not empty -> start moving to next delivery
                this.startNextUnloadingRequest(time);
                int timeToFinishState = this.location.manhattanDistance(this.getCurrentRequest().getDestination().getLocation()) / this.speed;
                this.initNextState(VehicleState.MOVING_TO_DELIVERY, timeToFinishState);
                this.moveToDelivery(time);
            }
        }
    }

    public boolean load(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.getAndDecrementTimeToFinishState() == 0) {
            // Vehicle finished loading
            this.loadBox(this.getCurrentRequest().getBox());
            this.outputWriter.writeLine(this, time, Operation.LOAD);

            if (this.isFull() || this.doneAllRequests()) {
                // Vehicle is full -> start moving to delivery
                this.startCurrentRequest(time);
                int timeToFinishState = this.location.manhattanDistance(this.getCurrentRequest().getDestination().getLocation()) / this.speed;
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
            this.location = this.getCurrentRequest().getDestination().getLocation();
            this.initNextState(VehicleState.UNLOADING, this.loadingDuration);
            this.getCurrentRequest().getDestination().setUsedByVehicle(this.id);
            this.unload(time);
        }
    }

    private boolean canStartUnloading() {
        // (We have arrived at the delivery location OR we were already there) AND the delivery location is free
        boolean timeIsUp = this.getAndDecrementTimeToFinishState() <= 0;
        boolean locationIsSame = this.location.equals(this.getCurrentRequest().getDestination().getLocation());
        boolean canBeUsedByVehicle = this.getCurrentRequest().getDestination().canBeUsedByVehicle(this.id);
        return (timeIsUp || locationIsSame) && canBeUsedByVehicle;
    }

    public void moveToPickup(int time) throws BoxNotAccessibleException, StackIsFullException {
        if (this.canStartLoading()) {
            // Vehicle arrived at pickup -> start loading
            this.location = this.getCurrentRequest().getPickup().getLocation();
            this.initNextState(VehicleState.LOADING, this.loadingDuration);
            this.getCurrentRequest().getPickup().setUsedByVehicle(this.id);
            this.load(time);
            // IDK why, but it works
            getAndDecrementTimeToFinishState();
        }
    }

    private boolean canStartLoading() {
        // (We have arrived at the pickup location OR we were already there) AND the pickup location is free
        boolean timeIsUp = this.getAndDecrementTimeToFinishState() <= 0;
        boolean locationIsSame = this.location.equals(this.getCurrentRequest().getPickup().getLocation());
        boolean canBeUsedByVehicle = this.getCurrentRequest().getPickup().canBeUsedByVehicle(this.id);
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

    public void startCurrentRequest(int time) {
        this.getCurrentRequest().setStartTime(time);
        this.getCurrentRequest().setVehicleStartLocation(this.location);
    }

    public void startNextLoadingRequest(int time) {
        this.queueIndex = Math.min(this.queueIndex + 1, this.queue.size() - 1);
        this.startCurrentRequest(time);
    }

    public void startNextUnloadingRequest(int time) {
        this.queueIndex = Math.max(this.queueIndex - 1, 0);
        this.startCurrentRequest(time);
    }

    public void setDoneAllRequests(boolean doneAllRequests) {
        this.doneAllRequests = doneAllRequests || this.queueIndex == this.queue.size() - 1;
    }

    public boolean doneAllRequests() {
        return this.doneAllRequests;
    }
}

