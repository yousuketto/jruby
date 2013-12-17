package org.jruby.ir.passes;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jruby.ir.IRClosure;
import org.jruby.ir.IRScope;
import org.jruby.ir.Operation;
import org.jruby.ir.instructions.BFalseInstr;
import org.jruby.ir.instructions.BTrueInstr;
import org.jruby.ir.instructions.BranchInstr;
import org.jruby.ir.instructions.CallBase;
import org.jruby.ir.instructions.CopyInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.instructions.ResultInstr;
import org.jruby.ir.instructions.boxing.AluInstr;
import org.jruby.ir.instructions.boxing.BoxFloatInstr;
import org.jruby.ir.instructions.boxing.UnboxFloatInstr;
import org.jruby.ir.operands.Float;
import org.jruby.ir.operands.Fixnum;
import org.jruby.ir.operands.LocalVariable;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.TemporaryVariable;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.representations.BasicBlock;

public class TypeAnalysisPass extends CompilerPass {

    public static List<Class<? extends CompilerPass>> DEPENDENCIES = Arrays.<Class<? extends CompilerPass>>asList(CFGBuilder.class);

    private Map<IRScope, Map<Variable, Class>> types;

    public TypeAnalysisPass() {
        this.types = new HashMap<IRScope, Map<Variable, Class>>();
    }

    @Override
    public String getLabel() {
        return "Type Analysis";
    }

    @Override
    public List<Class<? extends CompilerPass>> getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public Object previouslyRun(IRScope scope) {
        return types.get(scope);
    }

    private Class getOperandType(IRScope s, Operand o) {
        Map<Variable, Class> scopeTypes = types.get(s);
        if (scopeTypes == null) {
            return null;
        }

        if (o instanceof Float) {
            return Float.class;
        } else if (o instanceof Fixnum) {
            return Fixnum.class;
        } else if (o instanceof Variable) {
            Variable v = (Variable)o;
            Class vType = scopeTypes.get(v);
            if (vType == null && s instanceof IRClosure && v instanceof LocalVariable) {
                // Looking in parent scope
                IRScope lexicalParent = s.getLexicalParent();
                if (lexicalParent != null) {
                    LocalVariable lv = (LocalVariable)v;
                    // BUGGY: Need to deal with for-loops
                    v = lv.cloneForDepth(lv.getScopeDepth() - 1);
                    return getOperandType(lexicalParent, o);
                } else {
                    return null;
                }
            }
            return vType;
        } else {
            return null;
        }
    }

    private void setOperandType(IRScope s, Instr i, Variable v, Class type) {
        Map<Variable, Class> scopeTypes = types.get(s);
        if (scopeTypes == null) {
            return;
        }

        /*
        if (type != null) {
            System.out.println("type of " + v + " @ " + i + " is " + type.getName());
        }
        */

        // Delegate to right owner
        if (s instanceof IRClosure && v instanceof LocalVariable) {
            LocalVariable lv = (LocalVariable)v;
            // BUGGY: Need to deal with for-loops
            //
            // Also, type can be modified by code in the closure
            // if this is actually an iterator. Optimistically assuming
            // that type won't change. But, okay to make the assumption
            // since we are only looking for ALU-type calls where there
            // is a high probability of getting this right.
            int n = lv.getScopeDepth();
            if (n > 0) {
                setOperandType(s.getLexicalParent(), i, lv.cloneForDepth(n - 1), type);
                return;
            }
        }

        if (type == null) {
            scopeTypes.remove(v);
        } else {
            scopeTypes.put(v, type);
        }
    }

    private TemporaryVariable getUnboxedResultVar(IRScope scope, Map<Variable, TemporaryVariable> unboxMap, Variable dst) {
        TemporaryVariable unboxedDst = unboxMap.get(dst);
        if (unboxedDst == null) {
            unboxedDst = scope.getNewFloatVariable();
            unboxMap.put(dst, unboxedDst);
        }
        return unboxedDst;
    }

    private Operand getUnboxedOperand(IRScope scope, Map<Variable, TemporaryVariable> unboxMap, Operand arg, List<Instr> newInstrs, boolean unbox) {
        if (arg instanceof Variable) {
            TemporaryVariable unboxedArg = unboxMap.get(arg);
            if (unboxedArg == null && unbox) {
                unboxedArg = scope.getNewFloatVariable();
                unboxMap.put((Variable)arg, unboxedArg);
                newInstrs.add(new UnboxFloatInstr(unboxedArg, arg));
            }
            return unboxedArg;
        } else {
            return arg;
        }
    }

    private Operand getUnboxedOperand(IRScope scope, Map<Variable, TemporaryVariable> unboxMap, Operand arg, List<Instr> newInstrs) {
        return getUnboxedOperand(scope, unboxMap, arg, newInstrs, true);
    }

    private void boxRequiredVars(Instr i, Set<Variable> dirtyVars, Map<Variable, TemporaryVariable> unboxMap, List<Instr> newInstrs) {
        for (Variable v: i.getUsedVariables()) {
            TemporaryVariable unboxedVar = unboxMap.get(v);
            if (v != null && dirtyVars.contains(v)) {
                newInstrs.add(new BoxFloatInstr(v, unboxedVar));
                dirtyVars.remove(v);
            }
        }
    }

    private void boxDirtyLocalVars(Set<Variable> dirtyVars, Map<Variable, TemporaryVariable> unboxMap, List<Instr> newInstrs) {
        for (Variable v: unboxMap.keySet()) {
            if (v instanceof LocalVariable && dirtyVars.contains(v)) {
                newInstrs.add(new BoxFloatInstr(v, unboxMap.get(v)));
                dirtyVars.remove(v);
            }
        }
    }

    @Override
    public Object execute(IRScope scope, Object... data) {
        // System.out.println("Scope: " + scope.getName());
        Map<Variable, Class> scopeTypes = new HashMap<Variable, Class>();
        types.put(scope, scopeTypes);

        ListIterator<BasicBlock> it = scope.cfg().getReversePostOrderTraverser();
        while (it.hasPrevious()) {
            boolean addedInstr = false;
            BasicBlock bb = it.previous();
            List<Instr> newInstrs = new ArrayList<Instr>();
            Map<Variable, TemporaryVariable> unboxMap = new HashMap<Variable, TemporaryVariable>();
            Set<Variable> dirtyVars = new HashSet<Variable>();
            for (Instr i: bb.getInstrs()) {
                Operation op = i.getOperation();
                if (i instanceof ResultInstr) {
                    boolean modified = false;
                    Variable dst = ((ResultInstr)i).getResult();
                    if (i instanceof CopyInstr) {
                        Operand src = ((CopyInstr)i).getSource();
                        Class srcType = getOperandType(scope, src);
                        if (srcType == null) {
                            setOperandType(scope, i, dst, null);
                        } else {
                            setOperandType(scope, i, dst, srcType);
                            if (src instanceof Variable && srcType == Float.class) {
                                TemporaryVariable unboxedSrc = unboxMap.get(src);
                                if (unboxedSrc != null) {
                                    TemporaryVariable unboxedDst = getUnboxedResultVar(scope, unboxMap, dst);
                                    newInstrs.add(new CopyInstr(Operation.COPY_UNBOXED, unboxedDst, unboxedSrc));
                                    modified = true;
                                }
                            }
                        }
                    } else if (i instanceof CallBase) {
                        CallBase c = (CallBase)i;
                        MethAddr m = c.getMethodAddr();
                        Operand  r = c.getReceiver();
                        Operand[] args = c.getCallArgs();
                        if (args.length == 1 && m.resemblesALUOp()) {
                            Operand a = args[0];
                            Class receiverType = getOperandType(scope, r);
                            Class argType = getOperandType(scope, a);
                            // Assume call is an ALU op
                            if (receiverType == Float.class ||
                                (receiverType == Fixnum.class && argType == Float.class))
                            {
                                setOperandType(scope, i, dst, Float.class);
                                r = getUnboxedOperand(scope, unboxMap, r, newInstrs);
                                a = getUnboxedOperand(scope, unboxMap, a, newInstrs);
                                TemporaryVariable unboxedDst = getUnboxedResultVar(scope, unboxMap, dst);
                                newInstrs.add(new AluInstr(m.getUnboxedOp(Float.class), unboxedDst, r, a));
                                modified = true;
                            } else if (receiverType == Fixnum.class && argType == Fixnum.class) {
                                setOperandType(scope, i, dst, Fixnum.class);
                            } else {
                                setOperandType(scope, i, dst, null);
                            }
                        } else {
                            setOperandType(scope, i, dst, null);
                        }
                    } else {
                        setOperandType(scope, i, dst, null);
                    }

                    if (modified) {
                        addedInstr = true;
                        dirtyVars.add(dst);
                    } else {
                        boxRequiredVars(i, dirtyVars, unboxMap, newInstrs);
                        unboxMap.remove(dst);
                        dirtyVars.remove(dst);
                        newInstrs.add(i);
                    }
                } else if (op == Operation.B_TRUE || op == Operation.B_FALSE) {
                    boxDirtyLocalVars(dirtyVars, unboxMap, newInstrs);
                    BranchInstr bi = (BranchInstr)i;
                    Operand a = bi.getArg1();
                    Operand ua = getUnboxedOperand(scope, unboxMap, a, newInstrs, false);
                    if (ua != a) {
                        if (op == Operation.B_TRUE) {
                            newInstrs.add(new BTrueInstr(Operation.B_TRUE_UNBOXED, ua, bi.getJumpTarget()));
                        } else {
                            newInstrs.add(new BFalseInstr(Operation.B_FALSE_UNBOXED, ua, bi.getJumpTarget()));
                        }
                    }
                } else {
                    boxRequiredVars(i, dirtyVars, unboxMap, newInstrs);
                    if (i.getOperation().transfersControl()) {
                        boxDirtyLocalVars(dirtyVars, unboxMap, newInstrs);
                    }
                    newInstrs.add(i);
                }
            }

            boxDirtyLocalVars(dirtyVars, unboxMap, newInstrs);

            if (addedInstr) {
                bb.replaceInstrs(newInstrs);
            }

            /*
            System.out.println("-- new instrs for BB " + bb + "--");
            for (Instr i: newInstrs) {
                System.out.println(i);
            }
            */
        }

        /*
        System.out.println("-- known types --");
        for (Variable v: scopeTypes.keySet()) {
            System.out.println("Type for " + v + " = " + scopeTypes.get(v).getName());
        }
        */

        for (IRClosure c: scope.getClosures()) {
            execute(c, data);
        }

        return scopeTypes;
    }

    @Override
    public void invalidate(IRScope scope) {
        types.remove(scope);

        for (IRClosure c: scope.getClosures()) {
            invalidate(c);
        }
    }
}
