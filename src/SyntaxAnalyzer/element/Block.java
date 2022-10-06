package SyntaxAnalyzer.element;

import java.util.ArrayList;

public class Block extends SyntaxNode{

    public final ArrayList<BlockItem> blockItems;

    public Block(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
        childrenNode.addAll(blockItems);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("LBRACE {\n");
        for(BlockItem blockItem:blockItems){
            res.append(blockItem.toString());
        }
        res.append("RBRACE }\n");
        res.append("<Block>\n");
        return res.toString();
    }

    public boolean isLastReturn(){
        int lastItemIndex = blockItems.size() - 1;
        if(lastItemIndex < 0) return false;
        BlockItem blockItem = blockItems.get(lastItemIndex);
        if(blockItem.getStmt()!=null){
            return blockItem.getStmt().getType() == Stmt.Type.Return;
        }
        return false;
    }
}
