package Warehouse;

public enum WarehouseState {
    MOVING_TO_BUFFERPOINT,
    MOVING_TO_BUFFERPOINT_WITH_RELOCATION,
    MOVING_TO_STACK,
    FINISHED;

    public WarehouseState next() {
        return switch (this) {
            case MOVING_TO_BUFFERPOINT -> MOVING_TO_BUFFERPOINT_WITH_RELOCATION;
            case MOVING_TO_BUFFERPOINT_WITH_RELOCATION -> MOVING_TO_STACK;
            case MOVING_TO_STACK, FINISHED -> FINISHED;
        };
    }
}
