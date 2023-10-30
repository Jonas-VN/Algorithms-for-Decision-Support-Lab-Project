package Warehouse;

import Utils.Location;

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

    public Vehicle(int id, Location location, int speed, int capacity, String name, int loadingDuration){
        this.id = id;
        this.location = location;
        this.speed = speed;
        this.capacity = capacity;
        this.name = name;
        this.loadingDuration = loadingDuration;
        this.boxes = new Stack(-1, null, capacity, "Vehicle " + id);
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

    public void loadBox(Box box){
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

    public void unloadBox(Box box, Storage storage){
        if (this.boxes.contains(box)){
            this.boxes.removeBox(box);
            storage.addBox(box);
            box.setStack(storage);
            System.out.println("Vehicle " + this.id + " has unloaded box " + box.getId() + ".");
        }
        else {
            System.out.println("Vehicle " + this.id + " does not contain box " + box.getId() + ".");
        }
    }

    public void moveTo(Location location){
        this.location = location;
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

