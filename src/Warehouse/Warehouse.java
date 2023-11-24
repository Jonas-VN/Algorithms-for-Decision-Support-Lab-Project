package Warehouse;

import Input.JSONParser;
import Output.OutputWriter;
import Utils.Clock;
import Warehouse.Exceptions.BoxNotAccessibleException;
import Warehouse.Exceptions.StackIsFullException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Warehouse {
    private final Clock clock;
    private final ArrayList<Storage> storages;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<Request> requests;
    private final ArrayList<Box> usedBoxes = new ArrayList<>();
    private HashMap<Storage, Vehicle> previousCycleStorageIDs;

    public Warehouse(String problem) throws IOException, StackIsFullException {
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.storages = parser.parseBufferPoints();
        this.storages.addAll(parser.parseStacks());
        OutputWriter outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        this.vehicles = parser.parseVehicles(outputWriter);
        this.requests = parser.parseRequests(storages);
        for (Request request : this.requests) {
            this.usedBoxes.add(request.getBox());
        }
        this.previousCycleStorageIDs = new HashMap<>(this.vehicles.size());
        this.clock = new Clock();
    }

    public void solve() throws BoxNotAccessibleException, StackIsFullException {
        final int numberOfVehicles = this.vehicles.size();
        boolean allVehiclesIdle = false;
        HashMap<Storage, Vehicle> currentCycleStorageIDs = new HashMap<>(this.vehicles.size());

        while (!requests.isEmpty() || !allVehiclesIdle) {
            int numberOfVehiclesIdle = 0;
            this.previousCycleStorageIDs = currentCycleStorageIDs;
            currentCycleStorageIDs = new HashMap<>(numberOfVehicles);

            for (Vehicle vehicle : vehicles) {
                switch (vehicle.getState()) {
                    case MOVING_TO_PICKUP -> vehicle.moveToPickup(clock.getTime());
                    case MOVING_TO_DELIVERY -> vehicle.moveToDelivery(clock.getTime());
                    case UNLOADING -> vehicle.unload(clock.getTime());
                    case LOADING -> vehicle.load(clock.getTime());
                }

                ArrayList<Request> undoRequests = vehicle.getRequestsToBeUndone();
                if (undoRequests != null && !undoRequests.isEmpty()) {
                    this.requests.addAll(undoRequests);
                }

                if (vehicle.getState() == VehicleState.IDLE) this.allocateNextRequest(vehicle);

                Storage storage = vehicle.getFreedStorage();
                if (storage != null) {
                    currentCycleStorageIDs.put(storage, vehicle);
                }
//                System.out.println("Requests: " + this.requests.size() + " ... " + clock.getTime());
//                if (requests.size() == 2) {
//                    for (Request request : requests) {
//                        System.out.println(request);
//                    }
//                }

                allVehiclesIdle = vehicle.getState() == VehicleState.IDLE && ++numberOfVehiclesIdle == numberOfVehicles;
            }
            clock.tick();
        }
    }

    private void allocateNextRequest(Vehicle vehicle) throws StackIsFullException, BoxNotAccessibleException {
        // Sort the requests by distance to the vehicle
        this.requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));
        if (!this.requests.isEmpty()) {
            // #1: do all the request where the boxes are directly accessible.
            for (Request request : this.requests) {
                if (request.getId() == -1) break;
                Storage pickup = request.getPickup();
                Storage destination = request.getDestination();

                if (pickup.canRemoveBox(request.getBox()) && pickup.canBeUsedByVehicle(vehicle.getId()) && !destination.isFull() && request.getDestination() instanceof BufferPoint) {
                    // The box is accessible and can be delivered
//                    System.out.println("Box is accessible and can be delivered by " + vehicle.getName() + " ... " + request.getBox() + " ... " + clock.getTime());
                    this.requests.remove(request);

                    // If vehicle1 cleared this in the previous cycle, vehicle0 can skip a tick because it could have started moving in the previous cycle
                    if (this.previousCycleStorageIDs.containsKey(pickup)) {
                        Vehicle previousVehicle = this.previousCycleStorageIDs.get(pickup);
                        if (previousVehicle.getId() > vehicle.getId()) vehicle.setCanSkipATick();
                    }
                    vehicle.setIsRelocating(false);
                    vehicle.addRequest(request, clock.getTime());
                    return;
                }
            }

            // #2. No request (to the bufferPoint) can be done without relocation, so we need to do some relocations first
            for (Request request : this.requests) {
                Storage pickup = request.getPickup();
                if (pickup.canBeUsedByVehicle(vehicle.getId()) && request.getDestination() instanceof BufferPoint) {
                    Storage relocationStorage = this.getRelocationStorage(vehicle, pickup);
                    if (relocationStorage == null) {
                        // No relocation storage found, so we can't do this request
                        return;
                    }
                    System.out.println("Relocating with " + vehicle.getName());
                    vehicle.setIsRelocating(true);
                    if (this.previousCycleStorageIDs.containsKey(pickup)) {
                        Vehicle previousVehicle = this.previousCycleStorageIDs.get(pickup);
                        if (previousVehicle.getId() > vehicle.getId()) vehicle.setCanSkipATick();
                    }
                    // We need to undo the relocation
                    if (this.usedBoxes.contains(pickup.peek()))
                        vehicle.addRequestToBeUndone(new Request(-1, relocationStorage, pickup, pickup.peek()));
                    // We can just move it to the relocation storage
                    vehicle.addRequest(new Request(-1, pickup, relocationStorage, pickup.peek()), clock.getTime());
                    return;
                }
            }

            // #3. Do all the other requests where the boxes are directly accessible
            for (Request request : this.requests) {
                Storage pickup = request.getPickup();
                Storage destination = request.getDestination();

                if (pickup.canRemoveBox(request.getBox()) && pickup.canBeUsedByVehicle(vehicle.getId()) && !destination.isFull()) {
                    // The box is accessible and can be delivered
                    this.requests.remove(request);

                    // If vehicle1 cleared this in the previous cycle, vehicle0 can skip a tick because it could have started moving in the previous cycle
                    if (this.previousCycleStorageIDs.containsKey(pickup)) {
                        Vehicle previousVehicle = this.previousCycleStorageIDs.get(pickup);
                        if (previousVehicle.getId() > vehicle.getId()) vehicle.setCanSkipATick();
                    }
                    vehicle.setIsRelocating(false);
                    vehicle.addRequest(request, clock.getTime());
                    return;
                }
            }
        }
//        System.out.println("TIME: " + clock.getTime());
//        for (Request request : this.requests) {
//            System.out.println(request);
//        }


        // No request found -> start unloading
        if (!vehicle.isEmpty()) {
            System.out.println("Not full but delivering");
            vehicle.setupMoveToDelivery(clock.getTime());
            vehicle.moveToDelivery(clock.getTime());
            return;
        }
        vehicle.incrementTimeIdle();
        vehicle.getAndDecrementTimeToFinishState();

//        else if (!this.requests.isEmpty()) {
//            // There are still requests that need to go to a stack
//        }

        // Then try to find a request that can be done without any problems
//        for (Request request : this.requests) {
//            Storage pickup = request.getPickup();
//            Storage destination = request.getDestination();
//
//            if (pickup.canBeUsedByVehicle(vehicle.getId()) && pickup.canRemoveBox(request.getBox()) && !destination.isFull()) {
//                // The box is accessible and can be delivered
//                this.requests.remove(request);
//                vehicle.setIsRelocating(false);
////                System.out.println(vehicle.getName() + " can do request with box: " + request.getBox().getId());
//                vehicle.addRequest(request, clock.getTime());
//                System.out.println(request);
//                return;
//            }
//        }
//
//        // At last, so we do a relocation for the closest request
//        if (!requests.isEmpty()) {
//            Request request = this.requests.get(0);
//            Storage pickup = request.getPickup();
//            if (request.getDestination() instanceof BufferPoint) {
//                System.out.println("Buffer point");
//                System.out.println(request);
//
//                Box box = request.getBox();
//                if (!pickup.contains(box)) {
//                    // The box has been moved and still needs to be undone
////                System.out.println("[ERROR] Box has been moved and still needs to be undone!");
//                    return;
//                }
//                Storage relocationStorage = this.getRelocationStorage(vehicle, pickup, request.getDestination());
//                if (relocationStorage == null) {
////                System.out.println("[ERROR] No relocation storage found!");
//                    return;
//                }
////            System.out.println(vehicle.getName() + " needs relocation to reach box: " + box.getId());
//                vehicle.setIsRelocating(true);
////            System.out.println(pickup);
//                // We need to undo the relocation
//                vehicle.addRequestToBeUndone(new Request(-1, relocationStorage, pickup, pickup.peek()));
//                // We can just move it to the relocation storage
//                vehicle.addRequest(new Request(-1, pickup, relocationStorage, pickup.peek()), clock.getTime());
//            }
//        }
    }

    private Storage getRelocationStorage(Vehicle vehicle, Storage pickup) {
        this.storages.sort((storage1, storage2) -> Storage.compareByLocationBox(storage1, storage2, pickup));
        for (Storage relocationStorage : storages) {
            if (relocationStorage.canBeUsedByVehicle(vehicle.getId()) &&          // The vehicle can use this storage
                    !relocationStorage.isFull() &&                                // The storage is not full
                    relocationStorage != pickup &&                               // The storage is not the same as the pickup storage
                    relocationStorage instanceof Stack) {                         // The storage is a stack
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
