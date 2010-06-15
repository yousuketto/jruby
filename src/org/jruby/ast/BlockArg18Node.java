package org.jruby.ast;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import org.jruby.ast.visitor.NodeVisitor;
import org.jruby.lexer.yacc.ISourcePosition;

/**
 * Similiar to BlockArg, but with idiosyncracies that 1.8.7 allows:
 *
 * proc { |a,&b| }
 * proc { |a,&FOO| }
 * proc { |a,b.c| }
 * proc { |a,b[0]| }
 *
 */
public class BlockArg18Node extends Node {
    private static final long serialVersionUID = 0L;
    private Node normalBlockArgs;
    private Node blockArgAssignee;

    public BlockArg18Node() {
        super();
    }

    public BlockArg18Node(ISourcePosition position, Node blockArgAssignee,
            Node normalBlockArgs) {
        super(position);

        assert blockArgAssignee != null : "Must be a value to assign too";

        this.blockArgAssignee = blockArgAssignee;
        this.normalBlockArgs = normalBlockArgs;
    }

    public Node getArgs() {
        return normalBlockArgs;
    }

    public Node getBlockArg() {
        return blockArgAssignee;
    }

    @Override
    public Object accept(NodeVisitor visitor) {
        return visitor.visitBlockArg18Node(this);
    }

    @Override
    public List<Node> childNodes() {
        return createList(normalBlockArgs, blockArgAssignee);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.BLOCKARG18NODE;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(normalBlockArgs);
        out.writeObject(blockArgAssignee);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        normalBlockArgs = (Node)in.readObject();
        blockArgAssignee = (Node)in.readObject();
    }
}
