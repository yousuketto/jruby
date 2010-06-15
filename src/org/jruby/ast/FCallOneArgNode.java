/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jruby.ast;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.jruby.Ruby;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author enebo
 */
public class FCallOneArgNode extends FCallNode {
    private static final long serialVersionUID = 0L;
    private Node arg1;

    public FCallOneArgNode() {
        super();
    }
    
    public FCallOneArgNode(ISourcePosition position, String name, ArrayNode args) {
        super(position, name, args, null);
        
        assert args.size() == 1 : "args.size() is 1";
        
        arg1 = args.get(0);
    }

    @Override
    public Node setIterNode(Node iterNode) {
        return new FCallOneArgBlockNode(getPosition(), getName(), (ArrayNode) getArgsNode(), (IterNode) iterNode);
    }
    
    @Override
    public IRubyObject interpret(Ruby runtime, ThreadContext context, IRubyObject self, Block aBlock) {
        return callAdapter.call(context, self, self, arg1.interpret(runtime, context, self, aBlock));
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(arg1);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        arg1 = (Node)in.readObject();
    }
}
