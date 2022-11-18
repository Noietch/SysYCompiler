package Middle.IRElement.Instructions;

import Middle.IRElement.Value;

public class StoreInstruction extends BaseInstruction {
    public StoreInstruction(Value value1, Value value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public String toString() {
        return "store " + value1.getType() + " " + value1.getName() + ", " + value2.getDescriptor();
    }
}
