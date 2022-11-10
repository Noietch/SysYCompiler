package Backend.Mem;

public class Stack {
    public int stackPos;

    public Stack(int stackPos) {
        this.stackPos = stackPos;
    }

    public Stack offset(int offset){
        return new Stack(stackPos+offset);
    }

    public int getStackPos(){
        return stackPos*4;
    }

    @Override
    public String toString() {
        return Integer.toString(-getStackPos());
    }
}
