package org.jruby.ir.instructions;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.ir.Operation;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;

public class ReceiveKeywordArgInstr extends ReceiveArgBase {
    public final int numUsedArgs;

    public ReceiveKeywordArgInstr(Variable result, int numUsedArgs) {
        super(Operation.RECV_KW_ARG, result, -1);
        this.numUsedArgs = numUsedArgs;
    }

    @Override
    public String toString() {
        return (isDead() ? "[DEAD]" : "") + (hasUnusedResult() ? "[DEAD-RESULT]" : "") + getResult() + " = " + getOperation() + "(" + numUsedArgs + ")";
    }

    @Override
    public IRubyObject receiveArg(ThreadContext context, int kwArgHashCount, int numArgs, IRubyObject arg0, IRubyObject[] args) {
        if (kwArgHashCount == 0) {
            return UndefinedValue.UNDEFINED;
        } else {
            if (numUsedArgs == numArgs) {
                /* throw ArgumentError */
                Arity.raiseArgumentError(context.getRuntime(), numArgs-1, numUsedArgs, -1);
            }

            RubyHash lastArg = (RubyHash)ReceiveArgBase.fetchArgFromArgs(numArgs - 1, arg0, args);
            // If the key exists in the hash, delete and return it.
            RubySymbol argName = context.getRuntime().newSymbol(getResult().getName());
            if (lastArg.fastARef(argName) != null) {
                // SSS FIXME: Can we use an internal delete here?
                return lastArg.delete(context, argName, Block.NULL_BLOCK);
            } else {
                return UndefinedValue.UNDEFINED;
            }
        }
    }
}
