package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class GetElementPtr extends BaseInstruction {
    public Value bound1;
    public Value bound2;


    public GetElementPtr(Value result, Value value1, Value bound1, Value bound2) {
        this.result = result;
        this.value1 = value1;
        this.bound1 = bound1;
        this.bound2 = bound2;
    }
    public GetElementPtr(Value result, Value value1, Value bound1) {
        this.result = result;
        this.value1 = value1;
        this.bound1 = bound1;
    }

    @Override
    public String toString() {
        if(bound2 != null)
            return String.format("%s = getelementptr %s, %s, %s, %s", result.getName(), value1.getInnerType(), value1.getDescriptor(), bound1.getDescriptor(), bound2.getDescriptor());
        else
            return String.format("%s = getelementptr %s, %s, %s", result.getName(), value1.getInnerType(), value1.getDescriptor(), bound1.getDescriptor());
    }
}
