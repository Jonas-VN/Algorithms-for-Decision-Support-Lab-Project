public class Box {
    private int id;
    private Stack stack;

    public Box(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public void setStack(Stack stack){
        this.stack = stack;
    }
    public Stack getStack(){
        return stack;
    }
}
