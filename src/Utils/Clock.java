package Utils;

public class Clock {
    int time = 0;

    public int getTime(){
        return time;
    }

    public void tick(){
        time++;
    }

    @Override
    public String toString() {
        return "Clock{" +
                "time=" + time +
                '}';
    }
}
