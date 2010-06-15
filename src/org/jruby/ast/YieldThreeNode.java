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
public class YieldThreeNode extends YieldNode {
    private static final long serialVersionUID = 0L;
    private Node argument1;
    private Node argument2;
    private Node argument3;

    public YieldThreeNode() {
        super();
    }

    public YieldThreeNode(ISourcePosition position, ArrayNode args) {
        super(position, args, true);

        argument1 = args.get(0);
        argument2 = args.get(1);
        argument3 = args.get(2);
    }

    @Override
    public IRubyObject interpret(Ruby runtime, ThreadContext context, IRubyObject self, Block aBlock) {
        return context.getCurrentFrame().getBlock().yieldSpecific(context,
                argument1.interpret(runtime, context, self, aBlock),
                argument2.interpret(runtime, context, self, aBlock),
                argument3.interpret(runtime, context, self, aBlock));
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(argument1);
        out.writeObject(argument2);
        out.writeObject(argument3);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        argument1 = (Node)in.readObject();
        argument2 = (Node)in.readObject();
        argument3 = (Node)in.readObject();
    }
}
