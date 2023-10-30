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

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
    	this.y = y;
    }

    public double distanceSquared(Location other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return dx * dx + dy * dy;
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


