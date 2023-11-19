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

    public Warehouse(String problem) throws IOException, StackIsFullException {
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.storages = parser.parseBufferPoints();
        this.storages.addAll(parser.parseStacks());
        OutputWriter outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        this.vehicles = parser.parseVehicles(outputWriter);
        this.requests = parser.parseRequests(storages);
        this.clock = new Clock();
    }

    public void solve() throws BoxNotAccessibleException, StackIsFullException {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getState() == VehicleState.IDLE) {
                this.findNextRequest(vehicle);
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
                            // If the vehicle is not full yet, we can add another request to the list
                            if (!vehicle.isFull()) {
                                this.findNextRequest(vehicle);

                                // Requests are empty but the vehicle isn't full yet -> we have done everything we could
                                if (requests.isEmpty()) vehicle.setDoneAllRequests(true);

                            }
                        }
                    }
                }

                if (vehicle.getState() == VehicleState.IDLE) {
                    vehicle.clearRequests();
                    this.findNextRequest(vehicle);
                }

                // Vehicle didn't find a request to do
                if (vehicle.getState() == VehicleState.IDLE) numberOfVehiclesIdle++;
            }
            clock.tick();

            // Check if all vehicles are idle and there are no more requests -> end simulation
            if (requests.isEmpty() && numberOfVehiclesIdle == vehicles.size()) return;
        }
    }

    private void findNextRequest(Vehicle vehicle) throws StackIsFullException {
        this.requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));

        // Priority 1: Requests that are accessible
        for (Request request : this.requests) {
            // If the stack is free && the box is still in that stack (if this is not the case it means that the relocation should still be undone)
            if (request.getPickup().canBeUsedByVehicle(vehicle.getId()) && request.getPickup().contains(request.getBox())) {
                if (request.getPickup().canRemoveBox(request.getBox())) {
                    // The box is accessible
                    this.requests.remove(request);
                    vehicle.addRequest(request, clock.getTime());
                    int timeToFinishState = vehicle.getLocation().manhattanDistance(vehicle.currentRequest().getPickup().getLocation()) / vehicle.getSpeed();
                    vehicle.initNextState(VehicleState.MOVING_TO_PICKUP, timeToFinishState);
                    return;
                }
            }
        }

        // Priority 2: Requests that are not accessible but can be made accessible by relocating a box and it fits in the vehicle
//        for (Request request : this.requests) {
//            if (vehicle.getStack().getFreeSpaces() >= request.getPickup().numberOfBoxesOnTop(request.getBox()) + 1) {
//                System.out.println("Relocation needed");
//                // The box is not accessible, but the vehicle has enough space to do all the relocations and the pickup
//                Storage relocationStorage = getRelocationStorage(vehicle, request.getPickup(), request.getPickup().numberOfBoxesOnTop(request.getBox()));
//
//                // Make a new request for the relocation
//                Request doRelocationRequest = new Request(-1, request.getPickup(), relocationStorage, request.getPickup().peek());
//
//                // We have to undo this relocation because if this box is in another request that request will never be able to be completed
//                // But we can only undo this relocation after the original box is moved
//                Request undoRelocationRequest = new Request(-1, relocationStorage,request.getPickup() , request.getPickup().peek());
//                vehicle.addUndoRelocationRequest(undoRelocationRequest);
//
//                vehicle.addRequest(doRelocationRequest);
//                vehicle.startNextLoadingRequest(clock.getTime());
//                int timeToFinishState = vehicle.getLocation().manhattanDistance(vehicle.getCurrentRequest().getPickup().getLocation()) / vehicle.getSpeed();
//                vehicle.initNextState(VehicleState.MOVING_TO_PICKUP, timeToFinishState);
//                return;
//            }
//        }
    }

    private Storage getRelocationStorage(Vehicle vehicle, Storage storage, int numberOfRelocationsNeeded) {
        this.storages.sort((storage1, storage2) -> Storage.compareByLocationBox(storage1, storage2, storage));
        for (Storage relocationStorage : storages) {
            // IF the storage is free      && not full                    && not the same storage         && has enough free spaces
            if (relocationStorage.canBeUsedByVehicle(vehicle.getId()) && !relocationStorage.isFull() && relocationStorage != storage && storage.getFreeSpaces() >= numberOfRelocationsNeeded) {
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
