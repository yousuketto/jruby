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
import org.jruby.runtime.CallSite;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author enebo
 */
public class AttrAssignThreeArgNode extends AttrAssignNode {
    private static final long serialVersionUID = 0L;
    private Node arg1;
    private Node arg2;
    private Node arg3;

    public AttrAssignThreeArgNode() {
        super();
    }
    
    public AttrAssignThreeArgNode(ISourcePosition position, Node receiverNode, String name, ArrayNode argsNode) {
        super(position, receiverNode, name, argsNode);
        
        assert argsNode.size() == 3 : "argsNode.size() is 3";
        
        arg1 = argsNode.get(0);
        arg2 = argsNode.get(1);
        arg3 = argsNode.get(2);
    }
    

    @Override
    public IRubyObject interpret(Ruby runtime, ThreadContext context, IRubyObject self, Block aBlock) {
        IRubyObject receiver = receiverNode.interpret(runtime, context, self, aBlock);
        IRubyObject param1 = arg1.interpret(runtime, context, self, aBlock);
        IRubyObject param2 = arg2.interpret(runtime, context, self, aBlock);
        IRubyObject param3 = arg3.interpret(runtime, context, self, aBlock);        
        
        assert hasMetaClass(receiver) : receiverClassName(receiver);
        
        // If reciever is self then we do the call the same way as vcall
        CallSite callSite = selectCallSite(self, receiver);
        callSite.call(context, self, receiver, param1, param2, param3);

        return param3;
    }
    
        
    @Override
    public IRubyObject assign(Ruby runtime, ThreadContext context, IRubyObject self, IRubyObject value, Block aBlock, boolean checkArity) {
        IRubyObject receiver = receiverNode.interpret(runtime, context, self, aBlock);
        IRubyObject param1 = arg1.interpret(runtime, context, self, aBlock);
        IRubyObject param2 = arg2.interpret(runtime, context, self, aBlock);
        IRubyObject param3 = arg2.interpret(runtime, context, self, aBlock);        
        
        assert hasMetaClass(receiver) : receiverClassName(receiver);
        
        // If reciever is self then we do the call the same way as vcall
        CallSite callSite = selectCallSite(self, receiver);
        callSite.call(context, self, receiver, param1, param2, param3, value);
        
        return runtime.getNil();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(arg1);
        out.writeObject(arg2);
        out.writeObject(arg3);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        arg1 = (Node)in.readObject();
        arg2 = (Node)in.readObject();
        arg3 = (Node)in.readObject();
    }
}
