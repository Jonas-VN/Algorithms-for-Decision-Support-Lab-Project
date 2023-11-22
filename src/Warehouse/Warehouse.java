package Warehouse;

import Input.JSONParser;
import Output.OutputWriter;
import Utils.Clock;
import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Warehouse {
    private final Clock clock;
    private final ArrayList<Storage> storages;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<Request> requests;
    private final ArrayList<Box> usedBoxes = new ArrayList<>();

    public Warehouse(String problem) throws IOException, StackIsFullException {
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.storages = parser.parseBufferPoints();
        this.storages.addAll(parser.parseStacks());
        OutputWriter outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        this.vehicles = parser.parseVehicles(outputWriter);
        this.requests = parser.parseRequests(storages);
        for (Request request : requests) {
            this.usedBoxes.add(request.getBox());
        }
        this.clock = new Clock();
    }

    public void solve() throws BoxNotAccessibleException, StackIsFullException {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getState() == VehicleState.IDLE) {
                Request nextRequest = this.findNextRequest(vehicle);
                if (nextRequest != null) {
                    vehicle.addRequest(nextRequest, clock.getTime());
                    int timeToFinishState = vehicle.getLocation().manhattanDistance(nextRequest.getPickup().getLocation()) / vehicle.getSpeed();
                    vehicle.initNextState(VehicleState.MOVING_TO_PICKUP, timeToFinishState);
                }
            }
        }
        clock.tick();

        while (true) {
            int numberOfVehiclesIdle = 0;
            for (Vehicle vehicle : vehicles) {
                switch (vehicle.getState()) {
                    case MOVING_TO_PICKUP -> vehicle.moveToPickup(clock.getTime());
                    case MOVING_TO_DELIVERY -> vehicle.moveToDelivery(clock.getTime());
                    case UNLOADING -> vehicle.unload(clock.getTime());
                    case LOADING -> {
                        if (vehicle.load(clock.getTime())) {
//                            System.out.println("test");
                            // If the vehicle is not full yet, we can add another request to the list
                            if (!vehicle.isFull()) {
                                Request nextRequest = this.findNextRequest(vehicle);
//                                System.out.println(nextRequest);
                                if (nextRequest == null) {
                                    vehicle.setDoneAllRequests(true);
                                    Request request = vehicle.getCurrentRequest();
                                    int timeToFinishState = vehicle.getLocation().manhattanDistance(request.getDestination().getLocation()) / vehicle.getSpeed();
                                    vehicle.initNextState(VehicleState.MOVING_TO_DELIVERY, timeToFinishState);
                                    vehicle.getAndDecrementTimeToFinishState();
                                }
                                else {
                                    vehicle.addRequest(nextRequest, clock.getTime());
                                    int timeToFinishState = vehicle.getLocation().manhattanDistance(nextRequest.getPickup().getLocation()) / vehicle.getSpeed();
                                    vehicle.initNextState(VehicleState.MOVING_TO_PICKUP, timeToFinishState);
                                }
                            }
                        }
                    }
                }

                if (vehicle.getState() == VehicleState.IDLE) {
                    Request nextRequest = this.findNextRequest(vehicle);
                    if (nextRequest != null) {
                        vehicle.addRequest(nextRequest, clock.getTime());
                        int timeToFinishState = vehicle.getLocation().manhattanDistance(nextRequest.getPickup().getLocation()) / vehicle.getSpeed();
                        vehicle.initNextState(VehicleState.MOVING_TO_PICKUP, timeToFinishState);
                    }
                }

                // Vehicle didn't find a request to do
                if (vehicle.getState() == VehicleState.IDLE) {
                    numberOfVehiclesIdle++;
                }
            }
            clock.tick();

            // Check if all vehicles are idle and there are no more requests -> end simulation
            if (requests.isEmpty() && numberOfVehiclesIdle == vehicles.size()) return;
        }
    }

    private Request findNextRequest(Vehicle vehicle) {
        this.requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));

        for (Request request : this.requests) {
            Storage pickup = request.getPickup();
            // If the stack is free && the box is still in that stack (if this is not the case it means that the relocation should still be undone)
            if (pickup.canBeUsedByVehicle(vehicle.getId())) {
                if (pickup.canRemoveBox(request.getBox())) {
                    // The box is accessible
                    this.requests.remove(request);
                    return request;
                }
            }
//            else {
//                // Do a relocation
//                vehicle.setIsRelocating(true);
//                Box box = request.getBox();
//                int numberOfRelocationsNeeded = Math.min(pickup.numberOfBoxesOnTop(box), vehicle.numberOfFreeSpaces());
//                Storage relocationStorage = this.getRelocationStorage(vehicle, pickup, numberOfRelocationsNeeded);
//                vehicle.setRequestThatCausedRelocation(request);
//                if (usedBoxes.contains(box)) {
//                    // We need to undo the relocation
//                    vehicle.addRequestToBeUndone(new Request(-1, relocationStorage, pickup, box));
//                }
//                // We can just move it to the relocation storage
//                return new Request(-1, pickup, relocationStorage, box);
//            }
        }
        Request request = this.requests.get(0);
        Storage pickup = request.getPickup();
        // If the stack is free && the box is still in that stack (if this is not the case it means that the relocation should still be undone)
        // Do a relocation
        vehicle.setIsRelocating(true);
        Box box = request.getBox();
        int numberOfRelocationsNeeded = Math.min(pickup.numberOfBoxesOnTop(box), vehicle.numberOfFreeSpaces());
        Storage relocationStorage = this.getRelocationStorage(vehicle, pickup, numberOfRelocationsNeeded);
        vehicle.setRequestThatCausedRelocation(request);
        if (usedBoxes.contains(box)) {
            // We need to undo the relocation
            vehicle.addRequestToBeUndone(new Request(-1, relocationStorage, pickup, box));
        }
        // We can just move it to the relocation storage
        return new Request(-1, pickup, relocationStorage, box);

    }




    private Storage getRelocationStorage(Vehicle vehicle, Storage storage, int numberOfRelocationsNeeded) {
        this.storages.sort((storage1, storage2) -> Storage.compareByLocationBox(storage1, storage2, storage));
        for (Storage relocationStorage : storages) {
            if (relocationStorage.canBeUsedByVehicle(vehicle.getId()) &&
                    !relocationStorage.isFull() &&
                    relocationStorage != storage &&
                    storage.getFreeSpaces() >= numberOfRelocationsNeeded &&
                    relocationStorage instanceof Stack) {
                return relocationStorage;
            }
        }
        return null;
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
