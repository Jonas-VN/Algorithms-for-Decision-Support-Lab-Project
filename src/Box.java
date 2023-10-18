public class Box {
    private String id;
    private Stack stack;

    public Box(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }
    public void setStack(Stack stack){
        this.stack = stack;
    }
    public Stack getStack(){
        return stack;
    }
}
