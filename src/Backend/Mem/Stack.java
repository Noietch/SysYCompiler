package Backend.Mem;

public class Stack {
    public int stackPos;

    public Stack(int stackPos) {
        this.stackPos = stackPos;
    }

    public int getStackPos(){
        return stackPos*4;
    }

    @Override
    public String toString() {
        return Integer.toString(getStackPos());
    }
}
