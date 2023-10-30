package Warehouse;

public enum Operation {
    LOAD, // Pick Up
    UNLOAD; // Place

    @Override
    public String toString() {
        return switch (this) {
            case LOAD -> "PU";
            case UNLOAD -> "PL";
        };
    }
}
