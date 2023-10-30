package Warehouse;

import Input.JSONParser;
import Output.OutputWriter;
import Utils.Clock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Warehouse {
    private final Clock clock;
    private final ArrayList<Storage> storages;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<Request> requests;
    private final OutputWriter outputWriter;

    public Warehouse(String problem) throws IOException {
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        this.storages = parser.parseBufferPoints();
        this.storages.addAll(parser.parseStacks());
        this.vehicles = parser.parseVehicles();
        this.requests = parser.parseRequests(storages);
        this.clock = new Clock();
    }

    public void solve(){
        while (true) {
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getState() == VehicleState.IDLE) {
                    vehicle.clearRequests();
//                    System.out.println(vehicle.getName() + " is idle");
                    if (requests.isEmpty()) {
                        return;
                    }
                    // Reserve requests for this vehicle
                    requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));
                    while (!requests.isEmpty() && vehicle.addRequest(requests.get(0))) {
                        requests.remove(0);
                    }
                    vehicle.setState(VehicleState.MOVING_TO_PICKUP);
                    vehicle.setTimeToFinishState(vehicle.getLocation().manhattenDistance(vehicle.getCurrentRequest().getPickup().getLocation()) * vehicle.getSpeed());
                    vehicle.getCurrentRequest().setStartTime(clock.getTime());
                }

                switch (vehicle.getState()) {
                    case MOVING_TO_PICKUP -> {
//                        System.out.println(vehicle.getName() + " is moving to pickup");
                        // If the vehicle arrived at the pickup OR already was at the pickup
                        if (vehicle.getAndDecrementTimeToFinishState() == 0 || vehicle.getLocation().equals(vehicle.getCurrentRequest().getPickup().getLocation())) {
                            // Vehicle arrived at pickup -> start loading
                            vehicle.setState(VehicleState.LOADING);
                            vehicle.setTimeToFinishState(vehicle.getLoadingDuration());
                            vehicle.moveTo(vehicle.getCurrentRequest().getPickup().getLocation());
                            clock.skipNextTick();
                        }
                    }
                    case MOVING_TO_DELIVERY -> {
//                        System.out.println(vehicle.getName() + " is moving to delivery");
                        // If the vehicle arrived at the delivery OR already was at the delivery
                        if (vehicle.getAndDecrementTimeToFinishState() == 0 || vehicle.getLocation().equals(vehicle.getCurrentRequest().getDestination().getLocation())) {
                            // Vehicle arrived at delivery -> start unloading
                            vehicle.setState(VehicleState.UNLOADING);
                            vehicle.setTimeToFinishState(vehicle.getLoadingDuration());
                            vehicle.moveTo(vehicle.getCurrentRequest().getDestination().getLocation());
                            clock.skipNextTick();
                        }
                    }
                    case LOADING -> {
//                        System.out.println(vehicle.getName() + " is loading");
                        if (vehicle.getAndDecrementTimeToFinishState() == 0) {
                            // Vehicle finished loading
                            outputWriter.writeLine(vehicle, vehicle.getCurrentRequest(), clock.getTime(), Operation.LOAD);
                            vehicle.loadBox(vehicle.getCurrentRequest().getBox());
                            clock.skipNextTick();
                            if (vehicle.isFull() || vehicle.doneAllRequests()) {
                                // Vehicle is full -> start moving to delivery
                                vehicle.setState(VehicleState.MOVING_TO_DELIVERY);
                                vehicle.getCurrentRequest().setStartTime(clock.getTime());
                                vehicle.setTimeToFinishState(vehicle.getLocation().manhattenDistance(vehicle.getCurrentRequest().getDestination().getLocation()) * vehicle.getSpeed());
                            }
                            else {
                                // Vehicle is not full -> start moving to next pickup request
                                vehicle.startNextLoadingRequest();
                                vehicle.getCurrentRequest().setStartTime(clock.getTime());
                                vehicle.setState(VehicleState.MOVING_TO_PICKUP);
                                vehicle.setTimeToFinishState(vehicle.getLocation().manhattenDistance(vehicle.getCurrentRequest().getPickup().getLocation()) * vehicle.getSpeed());
                            }
                        }
                    }
                    case UNLOADING -> {
//                        System.out.println(vehicle.getName() + " is unloading");
                        if (vehicle.getAndDecrementTimeToFinishState() == 0) {
                            // Vehicle finished unloading
                            outputWriter.writeLine(vehicle, vehicle.getCurrentRequest(), clock.getTime(), Operation.UNLOAD);
                            vehicle.unloadBox(vehicle.getCurrentRequest().getBox(), vehicle.getCurrentRequest().getDestination());
                            clock.skipNextTick();
                            if (vehicle.isEmpty()) {
                                // Vehicle is empty -> set to idle
                                vehicle.setState(VehicleState.IDLE);
                            }
                            else {
                                // Vehicle is not empty -> start moving to next delivery
                                vehicle.startNextUnloadingRequest();
                                vehicle.getCurrentRequest().setStartTime(clock.getTime());
                                vehicle.setState(VehicleState.MOVING_TO_DELIVERY);
                                vehicle.setTimeToFinishState(vehicle.getLocation().manhattenDistance(vehicle.getCurrentRequest().getDestination().getLocation()) * vehicle.getSpeed());
                            }
                        }
                    }
                }
            }
            clock.tick();
        }
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "clock=" + clock +
                ", storages=" + storages +
                ", vehicles=" + vehicles +
                ", requests=" + requests +
                '}';
    }
}
