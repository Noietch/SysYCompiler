package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class GetElementPtr extends BaseInstruction {
    public Value bound1;
    public Value bound2;

    public GetElementPtr(Value value1, Value value2, Value bound1, Value bound2) {
        this.value1 = value1;
        this.value2 = value2;
        this.bound1 = bound1;
        this.bound2 = bound2;
    }
    public GetElementPtr(Value value1, Value value2, Value bound1) {
        this.value1 = value1;
        this.value2 = value2;
        this.bound1 = bound1;
    }

    @Override
    public String toString() {
        if(bound2 != null)
            return String.format("%s = getelementptr %s, %s, %s, %s", value1.getName(), value2.getInnerType(), value2.getDescriptor(), bound1.getDescriptor(), bound2.getDescriptor());
        else
            return String.format("%s = getelementptr %s, %s, %s", value1.getName(), value2.getInnerType(), value2.getDescriptor(), bound1.getDescriptor());
    }
}
