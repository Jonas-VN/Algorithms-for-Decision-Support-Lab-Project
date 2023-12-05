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
    private final HashMap<Storage, Integer> requestsToStack;
    private WarehouseState warehouseState = WarehouseState.MOVING_TO_BUFFERPOINT;
    private final HashMap<Storage, Vehicle> freedStorages = new HashMap<>();
    private final ArrayList<Request> undoRequests = new ArrayList<>();
    private final HashMap<Storage, Integer> nonRelocatableStacks = new HashMap<>();

    public Warehouse(String problem) throws IOException, StackIsFullException {
        JSONParser parser = new JSONParser(new File("src/Input/src/I" + problem + ".json"));
        this.storages = parser.parseBufferPoints();
        ArrayList<Stack> stacks = parser.parseStacks();
        this.storages.addAll(stacks);
        OutputWriter outputWriter = new OutputWriter(new File("src/Output/src/output" + problem + ".txt"));
        this.vehicles = parser.parseVehicles(outputWriter);
        this.requests = parser.parseRequests(storages);
        this.requestsToStack = new HashMap<>(stacks.size());
        for (Stack stack : stacks) {
            this.requestsToStack.put(stack, 0);
        }
        for (Request request : this.requests) {
            this.usedBoxes.add(request.getBox());
            if (request.getDestination() instanceof Stack && this.requestsToStack.containsKey(request.getDestination())) {
                this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) + 1);
            }
        }
        this.clock = new Clock();
    }

    public void solve() throws BoxNotAccessibleException, StackIsFullException {
        final int numberOfVehicles = this.vehicles.size();
        for (Vehicle vehicle : vehicles) {
            this.allocateNextRequest(vehicle);
        }

        while (!requests.isEmpty() || this.warehouseState != WarehouseState.FINISHED) {
            int numberOfVehiclesIdle = 0;
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getCurrentRequest() != null &&
                        vehicle.getCurrentRequest().getDestination() != null &&
                        this.freedStorages.containsKey(vehicle.getCurrentRequest().getDestination())) {
                    Vehicle freedByVehicle = this.freedStorages.get(vehicle.getCurrentRequest().getDestination());
                    if (freedByVehicle.getId() > vehicle.getId()) {
                        vehicle.skipTick();
                    }
                }
            }
            this.freedStorages.clear();

            for (Vehicle vehicle : vehicles) {
                switch (vehicle.getState()) {
                    case MOVING_TO_PICKUP -> vehicle.moveToPickup(clock.getTime());
                    case MOVING_TO_DELIVERY -> vehicle.moveToDelivery(clock.getTime());
                    case UNLOADING -> vehicle.unload(clock.getTime());
                    case LOADING -> vehicle.load(clock.getTime());
                }
                Storage freedStorage = vehicle.getFreedStorage();
                if (freedStorage != null) this.freedStorages.put(vehicle.getFreedStorage(), vehicle);
            }

            for (Vehicle vehicle : vehicles) {
                if (vehicle.getState() == VehicleState.IDLE) this.allocateNextRequest(vehicle);
                // Didn't find a new request, so the vehicle is still idle
                if (vehicle.getState() == VehicleState.IDLE) numberOfVehiclesIdle++;
            }

            if (numberOfVehiclesIdle == numberOfVehicles) {
                this.warehouseState = this.warehouseState.next();
                if (this.warehouseState == WarehouseState.UNDOING_RELOCATION) {
                    this.requests.addAll(this.undoRequests);
                } else if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT_WITH_RELOCATION) {
                    this.setupRelocations();
                }
                else if (this.warehouseState == WarehouseState.MOVING_TO_STACK) {
                    System.out.println(requests);
                } else if (this.warehouseState == WarehouseState.FINISHED) {
                    System.out.println(requests);
                    break;
                }
                System.out.println("WarehouseState: " + this.warehouseState);
            }
            else clock.tick();
        }
    }

    private void allocateNextRequest(Vehicle vehicle) throws StackIsFullException, BoxNotAccessibleException {
        // Sort the requests by distance to the vehicle
        this.requests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));
        if (!this.requests.isEmpty()) {
            // #1: do all the request where the boxes are directly accessible.
            if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT) {
                for (Request request : this.requests) {
                    Storage pickup = request.getPickup();

                    if (pickup.canRemoveBox(request.getBox()) && pickup.canBeUsedByVehicle(vehicle.getId()) && request.getDestination() instanceof BufferPoint) {
                        // The box is accessible and can be delivered
                        this.doAccessibleRequest(vehicle, request);
                        return;
                    }
                }
            }

            // #2. No request (to the bufferPoint) can be done without relocation, so we need to do some relocations first
            if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT_WITH_RELOCATION) {
                for (Request request : this.requests) {
                    Storage pickup = request.getPickup();
                    if (request.getDestination() instanceof BufferPoint) {
                        if (request.getPickup().canRemoveBox(request.getBox())) {
                            this.doAccessibleRequest(vehicle, request);
                            this.nonRelocatableStacks.put(request.getPickup(), this.nonRelocatableStacks.get(request.getPickup()) - 1);
                            return;
                        }
                        Storage relocationStorage = this.getRelocationStorage(pickup);
                        if (relocationStorage == null) {
                            System.out.println("No relocation storage found for request: " + request);
                            return;
                        }
                        // We need to undo the relocation
                        this.undoRequests.add(new Request(-1, relocationStorage, pickup, pickup.peek()));
                        // We can just move it to the relocation storage
                        vehicle.addRequest(new Request(0, pickup, relocationStorage, pickup.peek()), clock.getTime());
                        if (request.getDestination() instanceof Stack && this.requestsToStack.containsKey(request.getDestination())) {
                            this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) - 1);
                        }
                        return;
                    }
                }
                System.out.println("No request to bufferpoint can be done without relocation");
            }

            // #3. Undo all the relocations
            if (this.warehouseState == WarehouseState.UNDOING_RELOCATION) {
                for (Request request : this.requests) {
                    Storage pickup = request.getPickup();
                    Storage destination = request.getDestination();

                    if (request.getId() == -1 && pickup.canRemoveBox(request.getBox())) {
                        // The box is accessible and can be delivered
                        this.doAccessibleRequest(vehicle, request);
                        return;
                    }
                }
            }

            // #4. Do all the other requests where the boxes are directly accessible
            if (this.warehouseState == WarehouseState.MOVING_TO_STACK) {
                for (Request request : this.requests) {
                    Storage pickup = request.getPickup();
                    Storage destination = request.getDestination();

                    if (pickup.canRemoveBox(request.getBox()) && pickup.canBeUsedByVehicle(vehicle.getId()) && !destination.isFull()) {
                        // The box is accessible and can be delivered
                        this.doAccessibleRequest(vehicle, request);
                        return;
                    }
                }
            }

        }
        // #5. Delivering because the vehicle cant do anything else
        if (!vehicle.isEmpty()) {
            vehicle.setupMoveToDelivery(this.clock.getTime());
        }
    }

    private Storage getRelocationStorage(Storage pickup) {
        this.storages.sort((storage1, storage2) -> Storage.compareByLocationBox(storage1, storage2, pickup));
        for (Storage relocationStorage : storages) {
            if (relocationStorage != pickup &&                               // The storage is not the same as the pickup storage
                    relocationStorage instanceof Stack &&                     // The storage is a stack
                    !relocationStorage.willBeFull(this.requestsToStack.get(relocationStorage) + 1) && // The storage will not be full after the relocation
                    this.nonRelocatableStacks.get(relocationStorage) == 0      // The relocation storage is not needed anymore
            ) {
                return relocationStorage;
            }
        }
        return null;
    }

    private void doAccessibleRequest(Vehicle vehicle, Request request) throws StackIsFullException, BoxNotAccessibleException {
        this.requests.remove(request);
        vehicle.addRequest(request, clock.getTime());
        if (request.getDestination() instanceof Stack && this.requestsToStack.containsKey(request.getDestination())) {
            this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) - 1);
        }
    }

    private void doRelocationRequest(Vehicle vehicle, Request request) {

    }

    private void setupRelocations() {
        for (Storage storage : this.storages) this.nonRelocatableStacks.put(storage, 0);

        for (Request request : this.requests) {
            if (request.getDestination() instanceof BufferPoint) {
                this.nonRelocatableStacks.put(request.getPickup(), this.nonRelocatableStacks.get(request.getPickup()) + 1);
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
