public class Location {
    private int x;
    private int y;

    public Location(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }

    public double getDistance(Location location){
        int dx = this.x - location.x;
        int dy = this.y - location.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }
    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
}
