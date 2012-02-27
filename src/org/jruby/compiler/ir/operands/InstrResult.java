package org.jruby.compiler.ir.operands;

import java.util.List;
import java.util.Map;

import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.compiler.ir.instructions.Instr;
import org.jruby.compiler.ir.instructions.CopyInstr;
import org.jruby.compiler.ir.instructions.CallBase;
import org.jruby.compiler.ir.representations.InlinerInfo;

public class InstrResult extends Operand {
    public final Instr i;

    public InstrResult(Instr i) {
        this.i = i;
    }

    @Override
    public String toString() { 
        return "\n\t\t" + i;
    }

    @Override
    public Operand cloneForInlining(InlinerInfo ii) {
        throw new RuntimeException("cloneForInlining: InstrResult should not be used outside interpretation.");
    }

    @Override
    public Operand getSimplifiedOperand(Map<Operand, Operand> valueMap, boolean force) {
        throw new RuntimeException("getSimplifiedOperand: InstrResult should not be used outside interpretation.");
    }

    @Override
    public void addUsedVariables(List<Variable> l) {
        throw new RuntimeException("addUsedVariables: InstrResult should not be used outside interpretation.");
    }

    @Override
    public Object retrieve(ThreadContext context, IRubyObject self, DynamicScope currDynScope, Object[] temp) {
        // This switch is present to maximize inlining opportunities
        switch (i.getOperation()) {
            case ATTR_ASSIGN:
            case CALL: {
                CallBase c = (CallBase)i;
                IRubyObject object = (IRubyObject)c.getReceiver().retrieve(context, self, currDynScope, temp);
                return c.getCallAdapter().call(context, self, object, currDynScope, temp);
            }
            case COPY: {
                return ((CopyInstr)i).getSource().retrieve(context, self, currDynScope, temp);
            }
            case GET_FIELD: {
                return i.interpret(context, currDynScope, self, temp, null);
            }
            case NOT: {
                return i.interpret(context, currDynScope, self, temp, null);
            }
            case YIELD: {
                return i.interpret(context, currDynScope, self, temp, null);
            }
            default: {
                return i.interpret(context, currDynScope, self, temp, null);
            }
        }
    }
}
