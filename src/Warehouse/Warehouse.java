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

    public Warehouse(String problem) throws IOException {
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.storages = parser.parseBufferPoints();
        this.storages.addAll(parser.parseStacks());
        OutputWriter outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        this.vehicles = parser.parseVehicles(outputWriter);
        this.requests = parser.parseRequests(storages);
        this.clock = new Clock();
    }

    public void solve() {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getState() == VehicleState.IDLE) {
                // Reserve requests for this vehicle
                requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));
                while (!requests.isEmpty() && vehicle.addRequest(requests.get(0))) {
                    requests.remove(0);
                }
                vehicle.setState(VehicleState.MOVING_TO_PICKUP);
                vehicle.setTimeToFinishState(vehicle.getLocation().manhattenDistance(vehicle.getCurrentRequest().getPickup().getLocation()) * vehicle.getSpeed());
                vehicle.getCurrentRequest().setStartTime(clock.getTime());
                vehicle.getCurrentRequest().setVehicleStartLocation(vehicle.getLocation());
            }
        }

        while (true) {
            for (Vehicle vehicle : vehicles) {
                switch (vehicle.getState()) {
                    case MOVING_TO_PICKUP -> {
//                       System.out.println(vehicle.getName() + " is moving to pickup");
                        vehicle.moveToPickup(clock.getTime());
                    }
                    case MOVING_TO_DELIVERY -> {
//                       System.out.println(vehicle.getName() + " is moving to delivery");
                        vehicle.moveToDelivery(clock.getTime());
                    }
                    case LOADING -> {
//                       System.out.println(vehicle.getName() + " is loading");
                        vehicle.load(clock.getTime());
                    }
                    case UNLOADING -> {
//                       System.out.println(vehicle.getName() + " is unloading");
                        vehicle.unload(clock.getTime());
                    }
                }

                if (vehicle.getState() == VehicleState.IDLE) {
                    vehicle.clearRequests();
//                    System.out.println(vehicle.getName() + " is idle at time " + clock.getTime());
                    if (requests.isEmpty()) {
                        // TODO: this won't work with multiple vehicles
                        return;
                    }
                    // Reserve requests for this vehicle
                    requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));
                    while (!requests.isEmpty() && vehicle.addRequest(requests.get(0))) {
                        requests.remove(0);
                    }
                    vehicle.setState(VehicleState.MOVING_TO_PICKUP);
                    // IDK why - 1, but it works
                    vehicle.setTimeToFinishState(vehicle.getLocation().manhattenDistance(vehicle.getCurrentRequest().getPickup().getLocation()) * vehicle.getSpeed() - 1);
                    vehicle.getCurrentRequest().setStartTime(clock.getTime());
                    vehicle.getCurrentRequest().setVehicleStartLocation(vehicle.getLocation());
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
