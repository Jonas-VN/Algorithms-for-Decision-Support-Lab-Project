package Utils;

public class Clock {
    int time = 0;
    boolean skipNextTick = false;

    public int getTime(){
        return time;
    }

    public void tick(){
        if (skipNextTick) skipNextTick = false;
        else time++;
    }

    public void skipNextTick() {
        this.skipNextTick = true;
    }

    @Override
    public String toString() {
        return "Clock{" +
                "time=" + time +
                '}';
    }
}
