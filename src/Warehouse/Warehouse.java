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
    private final HashMap<Storage, Integer> requestsToStack; //TODO, misschien niet nodig
    private WarehouseState warehouseState = WarehouseState.MOVING_TO_BUFFERPOINT;
    private final HashMap<Storage, Vehicle> freedStorages = new HashMap<>();
    private final ArrayList<Request> undoRequests = new ArrayList<>();
    private final HashMap<Storage, Integer> relocationPickupStorages = new HashMap<>();
    private final ArrayList<Storage> relocationRequests = new ArrayList<>();

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
        for (Vehicle ignored : this.vehicles) {
            this.relocationRequests.add(null);
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
                if (vehicle.getState() == VehicleState.IDLE) {
                    this.relocationRequests.set(vehicle.getId(), null);
                    this.allocateNextRequest(vehicle);
                }
                // Didn't find a new request, so the vehicle is still idle
                if (vehicle.getState() == VehicleState.IDLE) numberOfVehiclesIdle++;
            }

            if (numberOfVehiclesIdle == numberOfVehicles) {
                this.warehouseState = this.warehouseState.next();
                System.out.println("Warehouse state: " + this.warehouseState);
                if (this.warehouseState == WarehouseState.MOVING_TO_BUFFERPOINT_WITH_RELOCATION) {
                    this.setupRelocations();
                }
                else if (this.warehouseState == WarehouseState.UNDOING_RELOCATION) {
                    System.out.println(requests);
                }
                if (this.warehouseState == WarehouseState.FINISHED) {
                    System.out.println(requests);
                    break;
                }
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

                    if (pickup.canRemoveBox(request.getBox()) && request.getDestination() instanceof BufferPoint) {
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
                            System.out.println(vehicle.getName() + ": Accessible request: " + vehicle.getCurrentRequest());
                            this.relocationPickupStorages.put(request.getPickup(), this.relocationPickupStorages.get(request.getPickup()) - 1);
                            this.relocationRequests.set(vehicle.getId(), request.getPickup());
                            return;
                        }
                        else if (!this.relocationRequests.contains(request.getPickup())) {
                            // The request is going to a bufferPoint
                            Storage relocationStorage = this.getRelocationStorage(pickup);
                            if (relocationStorage == null) {
                                System.out.println("No relocation storage found for request: " + request);
                                return;
                            }
                            this.relocationRequests.set(vehicle.getId(), request.getPickup());
                            // We need to undo the relocation
                            this.undoRequests.add(new Request(-1, relocationStorage, pickup, pickup.peek()));
                            // We can just move it to the relocation storage
                            vehicle.addRequest(new Request(0, pickup, relocationStorage, pickup.peek()), clock.getTime());
                            System.out.println(vehicle.getName() + ": Nieuwe request: " + vehicle.getCurrentRequest());
                            if (this.requestsToStack.containsKey(request.getDestination())) {
                                this.requestsToStack.put(request.getDestination(), this.requestsToStack.get(request.getDestination()) - 1);
                            }
                            return;
                        }
                    }
                }
            }

            // #3. Undo all the relocations
            if (this.warehouseState == WarehouseState.UNDOING_RELOCATION) {
                this.undoRequests.sort((request1, request2) -> Request.compareTo(request1, request2, vehicle));
                for (Request request : this.undoRequests) {
                    if (request.getPickup().canRemoveBox(request.getBox()) && !request.isClaimed()) {
                        // The box is accessible and can be delivered
                        request.claim();
                        System.out.print(vehicle.getName());
                        System.out.println(request);
                        this.doAccessibleRequest(vehicle, request);
                        return;
                    }
                }
            }

            // #4. Do all the other requests where the boxes are directly accessible
            if (this.warehouseState == WarehouseState.MOVING_TO_STACK) {
                for (Request request : this.requests) {
                    if (request.getPickup().canRemoveBox(request.getBox())) {
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
            if (relocationStorage != pickup &&                                                                     // The storage is not the same as the pickup storage
                    relocationStorage instanceof Stack &&                                                          // The storage is a stack
                    !relocationStorage.willBeFull(this.requestsToStack.get(relocationStorage) + 1) && // The storage will not be full after the relocation
                    this.relocationPickupStorages.get(relocationStorage) == 0                                      // The relocation storage is not used anymore for pickups
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
