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
    private final boolean doEarlyMovesToStack;
    private final Clock clock;
    private final ArrayList<Storage> storages;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<Request> requests;
    private final HashMap<Storage, Integer> requestsToStack;
    private final HashMap<Storage, Integer> requestsFromStack;
    private WarehouseState warehouseState = WarehouseState.MOVING_TO_BUFFERPOINT;
    private final HashMap<Storage, Integer> relocationPickupStorages = new HashMap<>();
    private final ArrayList<Storage> relocationRequests = new ArrayList<>();
    private HashMap<Storage, Vehicle> previousFreedStorages = new HashMap<>();

    public Warehouse(String problem, boolean doEarlyMovesToStack) throws IOException, StackIsFullException {
        this.doEarlyMovesToStack = doEarlyMovesToStack;
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.storages = parser.parseBufferPoints();
        ArrayList<Stack> stacks = parser.parseStacks();
        this.storages.addAll(stacks);
        OutputWriter outputWriter;
        if (!doEarlyMovesToStack) outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        else outputWriter = new OutputWriter(new File("src/Output/src2/output" + problem + ".txt"));
        this.vehicles = parser.parseVehicles(outputWriter);
        this.requests = parser.parseRequests(storages);
        this.requestsToStack = new HashMap<>(stacks.size());
        this.requestsFromStack = new HashMap<>(stacks.size());
        for (Stack stack : stacks) {
            this.requestsToStack.put(stack, 0);
            this.requestsFromStack.put(stack, 0);
        }
        for (Request request : this.requests) {
            if (request.getDestination() instanceof Stack && this.requestsToStack.containsKey(request.getDestination())) {
                this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) + 1);
            }
            if (request.getPickup() instanceof Stack && this.requestsFromStack.containsKey(request.getPickup())) {
                this.requestsFromStack.put(request.getPickup(), this.requestsFromStack.get(request.getPickup()) + 1);
            }
        }
        for (Vehicle ignored : this.vehicles) {
            this.relocationRequests.add(null);
        }
        this.clock = new Clock();
    }

    public void solve() throws BoxNotAccessibleException, StackIsFullException {
        HashMap<Storage, Vehicle> currentFreedStorages = new HashMap<>();
        final int numberOfVehicles = this.vehicles.size();
        for (Vehicle vehicle : vehicles) {
            this.allocateNextRequest(vehicle);
        }

        while (!requests.isEmpty() || this.warehouseState != WarehouseState.FINISHED) {
            int numberOfVehiclesIdle = 0;

            this.previousFreedStorages = new HashMap<>(currentFreedStorages);
            currentFreedStorages.clear();

            for (Vehicle vehicle : vehicles) {
                switch (vehicle.getState()) {
                    case MOVING_TO_PICKUP -> vehicle.moveToPickup(clock.getTime());
                    case MOVING_TO_DELIVERY -> vehicle.moveToDelivery(clock.getTime());
                    case UNLOADING -> vehicle.unload(clock.getTime());
                    case LOADING -> vehicle.load(clock.getTime());
                }
                Storage freedStorage = vehicle.getFreedStorage();
                if (freedStorage != null) currentFreedStorages.put(freedStorage, vehicle);
                this.checkForSkip(vehicle);
            }

            for (Vehicle vehicle : vehicles) {
                if (vehicle.getState() == VehicleState.IDLE) {
                    this.relocationRequests.set(vehicle.getId(), null);
                    this.allocateNextRequest(vehicle);
                }
                // Didn't find a new request, so the vehicle is still idle
                if (vehicle.getState() == VehicleState.IDLE) numberOfVehiclesIdle++;
            }

            if (numberOfVehiclesIdle == numberOfVehicles) {
                this.warehouseState = this.warehouseState.next();
                if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT_WITH_RELOCATION) {
                    this.setupRelocations();
                }
            }
            else clock.tick();
        }
    }

    private void checkForSkip(Vehicle vehicle) {
        // If vehicle 1 freed a storage, vehicle 0 couldn't know this, this method checks if the vehicle needs to skip a clock tick to counter this
        if (
                vehicle.needsToCheckForSkip() &&
                vehicle.getCurrentRequest() != null &&
                this.previousFreedStorages.containsKey(vehicle.getCurrentRequest().getDestination())
        ) {
            Vehicle freedByVehicle = this.previousFreedStorages.get(vehicle.getCurrentRequest().getDestination());
            if (freedByVehicle.getId() > vehicle.getId()) {
                vehicle.skipTick();
            }
        }
    }

    private void allocateNextRequest(Vehicle vehicle) throws StackIsFullException, BoxNotAccessibleException {
        if (!this.requests.isEmpty()) {
            // #1: do all the request where the boxes are directly accessible.
            if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT) {
                if (this.doEarlyMovesToStack && vehicle.isAtABufferPoint())
                    this.requests.sort((request1, request2) -> Request.compareToDestination(request1, request2, vehicle));
                else this.requests.sort((request1, request2) -> Request.compareToPickup(request1, request2, vehicle));

                for (Request request : this.requests) {
                    if (this.doEarlyMovesToStack && this.canMoveBoxToStackEarly(vehicle, request)) {
                        this.doAccessibleRequest(vehicle, request);
                        return;
                    }
                    if (request.getPickup().canRemoveBox(request.getBox()) && request.getDestination() instanceof BufferPoint) {
                        this.doAccessibleRequest(vehicle, request);
                        return;
                    }
                }
            }

            // #2. No request (to the bufferPoint) can be done without relocation, so we need to do some relocations first
            if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT_WITH_RELOCATION) {
                this.requests.sort((request1, request2) -> Request.compareToPickup(request1, request2, vehicle));
                for (Request request : this.requests) {
                    if (request.getDestination() instanceof BufferPoint) {
                        if (request.getPickup().canRemoveBox(request.getBox())) {
                            this.doAccessibleRequest(vehicle, request);
                            this.relocationPickupStorages.put(request.getPickup(), this.relocationPickupStorages.get(request.getPickup()) - 1);
                            this.relocationRequests.set(vehicle.getId(), request.getPickup());
                            return;
                        }
                        else if (!this.relocationRequests.contains(request.getPickup())) {
                            this.doRelocationRequest(vehicle, request);
                            return;
                        }
                    }
                }
            }

            // #3. Do all the other requests where the boxes are directly accessible
            if (this.warehouseState == WarehouseState.MOVING_TO_STACK) {
                this.requests.sort((request1, request2) -> Request.compareToPickup(request1, request2, vehicle));
                for (Request request : this.requests) {
                    if (!request.getDestination().isFull()) {
                        this.doAccessibleRequest(vehicle, request);
                    }
                    else {
                        // Find how many boxes should be removed from the destination stack
                        int numberOfBoxesToRemove = 0;
                        for (Request request2 : this.requests) {
                            if (request2.getDestination() == request.getDestination()) {
                                numberOfBoxesToRemove++;
                            }
                        }
                        for (int i = 0; i < numberOfBoxesToRemove; i++) {
                            Box boxToRemove = request.getDestination().getBox(i);
                            Storage relocationStorage = this.getRelocationStorage(request.getDestination());
                            Request relocationRequest = new Request(0, request.getDestination(), relocationStorage, boxToRemove);
                            if (i == 0) vehicle.addRequest(relocationRequest, clock.getTime());
                            else this.requests.add(relocationRequest);
                        }
                    }
                    return;
                }
            }

        }

        // #4. Delivering because the vehicle cant do anything else
        if (!vehicle.isEmpty()) {
            vehicle.setupMoveToDelivery(this.clock.getTime());
        }
    }

    private boolean canMoveBoxToStackEarly(Vehicle vehicle, Request request) {
        return (vehicle.isAtABufferPoint() &&
                request.getDestination() instanceof Stack &&
                this.requestsFromStack.get(request.getDestination()) == 0

        );
    }

    private Storage getRelocationStorage(Storage pickup) {
        this.storages.sort((storage1, storage2) -> Storage.compareByLocationBox(storage1, storage2, pickup));
        for (Storage relocationStorage : storages) {
            if (
                    relocationStorage != pickup &&                                                                 // The storage is not the same as the pickup storage
                    relocationStorage instanceof Stack &&                                                          // The storage is a stack
                    !relocationStorage.willBeFull(this.requestsToStack.get(relocationStorage) + 1) && // The storage will not be full after all the requests
                    this.relocationPickupStorages.get(relocationStorage) == 0                                      // The relocation storage is not used (anymore) for pickups
            ) {
                return relocationStorage;
            }
        }
        return null;
    }

    private void doAccessibleRequest(Vehicle vehicle, Request request) throws StackIsFullException, BoxNotAccessibleException {
        request.claim();
        this.requests.remove(request);
        vehicle.addRequest(request, clock.getTime());
        if (request.getDestination() instanceof Stack && this.requestsToStack.containsKey(request.getDestination())) {
            this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) - 1);
        }
        if (request.getPickup() instanceof Stack && this.requestsFromStack.containsKey(request.getPickup())) {
            this.requestsFromStack.put(request.getPickup(), this.requestsFromStack.get(request.getPickup()) - 1);
        }
    }

    private void doRelocationRequest(Vehicle vehicle, Request request) throws StackIsFullException, BoxNotAccessibleException {
        Storage pickup = request.getPickup();
        Storage relocationStorage = this.getRelocationStorage(pickup);
        if (relocationStorage == null) {
            return;
        }
        this.relocationRequests.set(vehicle.getId(), request.getPickup());

        // We can just move it to the relocation storage
        vehicle.addRequest(new Request(0, pickup, relocationStorage, pickup.peek()), clock.getTime());
        if (this.requestsToStack.containsKey(request.getDestination())) {
            this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) - 1);
        }
    }

    private void setupRelocations() {
        for (Storage storage : this.storages) {
            this.relocationPickupStorages.put(storage, 0);
        }
        for (Request request : this.requests) {
            if (request.getDestination() instanceof BufferPoint) {
                this.relocationPickupStorages.put(request.getPickup(), this.relocationPickupStorages.get(request.getPickup()) + 1);
            }
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
