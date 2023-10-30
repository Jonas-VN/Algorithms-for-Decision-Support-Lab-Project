package Utils;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
    
    public int manhattenDistance(Location other) {
    	int dx = this.x - other.x;
    	int dy = this.y - other.y;
    	return Math.abs(dx) + Math.abs(dy);
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}


