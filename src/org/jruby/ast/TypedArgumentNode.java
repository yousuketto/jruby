/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jruby.ast;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import org.jruby.lexer.yacc.ISourcePosition;

/**
 *
 * @author enebo
 */
public class TypedArgumentNode extends ArgumentNode {
    private static final long serialVersionUID = 0L;
    private Node typeNode;

    public TypedArgumentNode() {
        super();
    }
    
    public TypedArgumentNode(ISourcePosition position, String identifier, Node typeNode) {
        super(position, identifier);
        
        this.typeNode = typeNode;
    }
    
    public Node getTypeNode() {
        return typeNode;
    }
    
    public List<Node> childNodes() {
        return createList(typeNode);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(typeNode);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        typeNode = (Node)in.readObject();
    }
}
