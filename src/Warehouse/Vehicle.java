package Warehouse;

import Output.OutputWriter;
import Utils.Location;
import Warehouse.Exceptions.BoxNotAccessibleException;

import java.util.ArrayList;

public class Vehicle {
    private final int id;
    private Location location;
    private final int speed;
    private final int capacity;
    private final String name;
    private final int loadingDuration;
    private final Stack boxes;
    private final ArrayList<Request> queue = new ArrayList<>();
    private int queueIndex = 0;
    private VehicleState state = VehicleState.IDLE;
    private int timeToFinishState = 0;
    private final OutputWriter outputWriter;

    public Vehicle(int id, Location location, int speed, int capacity, String name, int loadingDuration, OutputWriter outputWriter){
        this.id = id;
        this.location = location;
        this.speed = speed;
        this.capacity = capacity;
        this.name = name;
        this.loadingDuration = loadingDuration;
        this.boxes = new Stack(-1, null, capacity, "Vehicle " + id);
        this.outputWriter = outputWriter;
    }

    public void clearRequests() {
        this.queue.clear();
        this.queueIndex = 0;
    }

    public void setTimeToFinishState(int timeToFinish) {
        this.timeToFinishState = timeToFinish;
    }

    public int getAndDecrementTimeToFinishState() {
        return this.timeToFinishState--;
    }

    public int getLoadingDuration() {
        return this.loadingDuration;
    }

    public void setState(VehicleState state) {
        this.state = state;
    }

    public VehicleState getState() {
        return this.state;
    }

    public boolean addRequest(Request request){
        if (this.queue.size() >= this.capacity) return false;
        return this.queue.add(request);
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
        return this.boxes.isFull();
    }

    public boolean isEmpty(){
        return this.boxes.isEmpty();
    }

    public Location getLocation(){
        return this.location;
    }

    public int getSpeed(){
        return this.speed;
    }

    public void loadBox(Box box) throws BoxNotAccessibleException {
        if (!this.isFull()) {
            box.getStack().removeBox(box);
            this.boxes.addBox(box);
            box.setStack(this.boxes);
            System.out.println("Vehicle " + this.id + " loaded box " + box.getId() + ".");
        }
        else {
            System.out.println("Vehicle " + this.id + " is full.");
        }
    }

    public void unloadBox(Box box, Storage storage) throws BoxNotAccessibleException {
        if (this.boxes.contains(box)){
            this.boxes.removeBox(box);
            storage.addBox(box);
            System.out.println("Vehicle " + this.id + " has unloaded box " + box.getId() + ".");
        }
        else {
            System.out.println("Vehicle " + this.id + " does not contain box " + box.getId() + ".");
        }
    }

    public void moveTo(Location location){
        this.location = location;
    }

    public void unload(int time) {
        if (this.getAndDecrementTimeToFinishState() == 0) {
            // Vehicle finished unloading
            try {
                this.unloadBox(this.getCurrentRequest().getBox(), this.getCurrentRequest().getDestination());
            }
            catch (BoxNotAccessibleException exception) {
                // TODO: Need relocations
                System.out.println("ERROR: Box " + this.getCurrentRequest().getBox().getId() + " is not accessible in " + this.getCurrentRequest().getDestination().getName() + "!");
            }
            this.outputWriter.writeLine(this, this.getCurrentRequest(), time, Operation.UNLOAD);
            // Clock.skipNextTick();
            if (this.isEmpty()) {
                // Vehicle is empty -> set to idle
                this.state = VehicleState.IDLE;
            }
            else {
                // Vehicle is not empty -> start moving to next delivery
                this.startNextUnloadingRequest();
                this.getCurrentRequest().setStartTime(time);
                this.getCurrentRequest().setVehicleStartLocation(this.location);
                this.state = VehicleState.MOVING_TO_DELIVERY;
                this.timeToFinishState = this.location.manhattenDistance(this.getCurrentRequest().getDestination().getLocation()) * this.speed;
                this.moveToDelivery(time);
            }
        }
    }
    public void load(int time) {
        if (this.getAndDecrementTimeToFinishState() == 0) {
            // Vehicle finished loading
            try {
                this.loadBox(this.getCurrentRequest().getBox());
            }
            catch (BoxNotAccessibleException exception) {
                System.out.println("ERROR: Box " + this.getCurrentRequest().getBox().getId() + " is not accessible in " + this.getCurrentRequest().getPickup().getName() + "!");
            }
            this.outputWriter.writeLine(this, this.getCurrentRequest(), time, Operation.LOAD);
            // Clock.skipNextTick();
            if (this.isFull() || this.doneAllRequests()) {
                // Vehicle is full -> start moving to delivery
                this.state = VehicleState.MOVING_TO_DELIVERY;
                this.getCurrentRequest().setStartTime(time);
                this.getCurrentRequest().setVehicleStartLocation(this.location);
                this.timeToFinishState = this.location.manhattenDistance(this.getCurrentRequest().getDestination().getLocation()) * this.speed;
                this.moveToDelivery(time);
            }
            else {
                // Vehicle is not full -> start moving to next pickup request
                this.startNextLoadingRequest();
                this.getCurrentRequest().setStartTime(time);
                this.getCurrentRequest().setVehicleStartLocation(this.location);
                this.state = VehicleState.MOVING_TO_PICKUP;
                this.timeToFinishState = this.location.manhattenDistance(this.getCurrentRequest().getPickup().getLocation()) * this.speed;
                this.moveToPickup(time);
            }
        }
    }

    public void moveToDelivery(int time) {
        // If the vehicle arrived at the delivery OR already was at the delivery
        if (this.getAndDecrementTimeToFinishState() == 0 || this.location.equals(this.getCurrentRequest().getDestination().getLocation())) {
            // Vehicle arrived at delivery -> start unloading
            this.state = VehicleState.UNLOADING;
            this.timeToFinishState = this.loadingDuration;
            this.location = this.getCurrentRequest().getDestination().getLocation();
            // clock.skipNextTick();
            this.unload(time);
        }
    }

    public void moveToPickup(int time) {
        // If the vehicle arrived at the pickup OR already was at the pickup
        if (this.getAndDecrementTimeToFinishState() == 0 || this.location.equals(this.getCurrentRequest().getPickup().getLocation())) {
            // Vehicle arrived at pickup -> start loading
            this.state = VehicleState.LOADING;
            this.timeToFinishState = this.loadingDuration;
            this.location = this.getCurrentRequest().getPickup().getLocation();
            // clock.skipNextTick();
            this.load(time);
        }
    }

    public void idle(int time) {
        this.clearRequests();
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + this.id +
                ", location=" + this.location +
                ", speed=" + this.speed +
                ", capacity=" + this.capacity +
                ", boxes=" + this.boxes +
                ", name='" + this.name + '\'' +
                ", loadingDuration=" + this.loadingDuration +
                '}';
    }

    public void startNextLoadingRequest() {
        this.queueIndex = Math.min(this.queueIndex + 1, this.queue.size() - 1);
    }

    public void startNextUnloadingRequest() {
        this.queueIndex = Math.max(this.queueIndex - 1, 0);
    }

    public boolean doneAllRequests() {
        return this.queueIndex == this.queue.size() - 1;
    }
}

