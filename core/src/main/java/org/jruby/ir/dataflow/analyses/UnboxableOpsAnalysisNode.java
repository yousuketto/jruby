package org.jruby.ir.dataflow.analyses;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jruby.ir.IRClosure;
import org.jruby.ir.IREvalScript;
import org.jruby.ir.IRScope;
import org.jruby.ir.Operation;
import org.jruby.ir.dataflow.DataFlowConstants;
import org.jruby.ir.dataflow.DataFlowProblem;
import org.jruby.ir.dataflow.FlowGraphNode;
import org.jruby.ir.instructions.BFalseInstr;
import org.jruby.ir.instructions.BTrueInstr;
import org.jruby.ir.instructions.BranchInstr;
import org.jruby.ir.instructions.CallBase;
import org.jruby.ir.instructions.CopyInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.instructions.ReturnInstr;
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
import org.jruby.ir.operands.WrappedIRClosure;
import org.jruby.ir.representations.BasicBlock;
import org.jruby.ir.util.Edge;

public class UnboxableOpsAnalysisNode extends FlowGraphNode {
    public UnboxableOpsAnalysisNode(DataFlowProblem prob, BasicBlock n) {
        super(prob, n);
    }

    @Override
    public void init() {
        outTypes = new HashMap<Variable, Class>();
        unboxedVarsOut = new HashSet<Variable>();
    }

    public void buildDataFlowVars(Instr i) {
        // Nothing to do -- because we are going to simply use variables as our data flow variables
        // rather than build a new data flow type for it
    }

    public void initSolnForNode() {
        inTypes = new HashMap<Variable, Class>();
        unboxedVarsIn = new HashSet<Variable>();
    }

    public void compute_MEET(Edge e, BasicBlock source, FlowGraphNode pred) {
        UnboxableOpsAnalysisNode n = (UnboxableOpsAnalysisNode) pred;
        Map<Variable, Class> predOutTypes = n.outTypes;
        for (Variable v: predOutTypes.keySet()) {
            Class c1 = predOutTypes.get(v);
            Class c2 = inTypes.get(v);
            if (c2 == null) {
                inTypes.put(v, c1);  // TOP --> class
                outTypes.put(v, c1); // Init outTypes to inTypes
            } else if (c1 != c2) {
                inTypes.put(v, Object.class); // TOP/class --> BOTTOM
                outTypes.put(v, Object.class); // Init outTypes to inTypes
            }
        }


        // Ignore rescue entries -- everything is unboxed, as necessary.
        if (!source.isRescueEntry()) unboxedVarsIn.addAll(n.unboxedVarsOut);
    }

    private Class getOperandType(Map<Variable, Class> types, Operand o) {
        // FIXME: This does not walk up lexical scope hierarchy
        //
        // For example, local vars in closures might belong to an outer scope
        // and we might know something about the type there.
        if (o instanceof Float) {
            return Float.class;
        } else if (o instanceof Fixnum) {
            return Fixnum.class;
        } else if (o instanceof Variable) {
            return types.get((Variable)o);
        } else {
            return null;
        }
    }

    private boolean setOperandType(Map<Variable, Class> types, Variable v, Class newType) {
        Class vType = types.get(v);
        if (newType == null) {
            types.remove(v);
            return false;
        } else {
            types.put(v, newType);
            return true;
        }
    }

    private void updateUnboxedVarsInfo(Instr i, Set<Variable> unboxedVars, Set<Variable> unboxedDirtyVars, Variable dst, boolean hasRescuer) {
        // Special treatment for instructions that can raise exceptions
        if (i.canRaiseException()) {
            if (hasRescuer) {
                // If we are going to be rescued,
                // box all unboxed dirty vars before we execute the instr
                unboxedDirtyVars.clear();
            } else {
                // We are going to exit if an exception is raised.
                // So, only need to bother with dirty live local vars for closures
                if (this.problem.getScope() instanceof IRClosure) {
                    HashSet<Variable> varsToRemove = new HashSet<Variable>();
                    for (Variable v: unboxedDirtyVars) {
                        if (v instanceof LocalVariable) {
                            varsToRemove.add(v);
                        }
                    }
                    unboxedDirtyVars.removeAll(varsToRemove);
                }
            }
        }

        // B_TRUE and B_FALSE have unboxed forms and their operands
        // needn't get boxed back.
        Operation op = i.getOperation();
        if (op != Operation.B_TRUE && op != Operation.B_FALSE) {
            // Vars used by this instruction that only exist in unboxed form
            // will have to get boxed before it is executed
            unboxedDirtyVars.removeAll(i.getUsedVariables());
        }

        // If the instruction writes into 'dst', it will be in boxed form.
        if (dst != null) {
            unboxedVars.remove(dst);
            unboxedDirtyVars.remove(dst);
        }
    }

    public boolean applyTransferFunction() {
        boolean changed = false;

        // Rescue node, if any
        boolean hasRescuer = getNonExitBBExceptionTargetNode() != null;

        Set<Variable> unboxedVars = new HashSet<Variable>(unboxedVarsIn);
        Set<Variable> unboxedDirtyVars = new HashSet<Variable>();

        for (Instr i : basicBlock.getInstrs()) {
            if (i instanceof ResultInstr) {
                boolean dirtied = false;
                Variable dst = ((ResultInstr) i).getResult();

                if (i instanceof CopyInstr) {
                    // Copies are easy
                    Operand src = ((CopyInstr)i).getSource();
                    Class srcType = getOperandType(outTypes, src);
                    if (setOperandType(outTypes, dst, srcType)) {
                        changed = true;
                    }

                    // If we have an unboxed type for 'src', we can leave this unboxed.
                    //
                    // FIXME: However, if 'src' is a constant, this could unnecessarily
                    // leave 'src' unboxed and lead to a boxing instruction further down
                    // at the use site of 'dst'. This indicates that leaving this unboxed
                    // should ideally be done 'on-demand'. This indicates that this could
                    // be a backward-flow algo OR that this algo should be run on a
                    // dataflow graph / SSA graph.
                    if (srcType == Float.class) {
                        dirtied = true;
                    }
                } else if (i instanceof CallBase) {
                    // Process calls specially -- these are what we want to optimize!
                    CallBase c = (CallBase)i;
                    Operand  o = c.getClosureArg(null);
                    if (o != null) {
                        // We have to either force all types to bottom after the call
                        // OR mimic how LVA handles this.
                        // FIXME: To be completed
                    } else {
                        MethAddr m = c.getMethodAddr();
                        Operand  r = c.getReceiver();
                        Operand[] args = c.getCallArgs();
                        if (args.length == 1 && m.resemblesALUOp()) {
                            Operand a = args[0];
                            Class receiverType = getOperandType(outTypes, r);
                            Class argType = getOperandType(outTypes, a);
                            // Optimistically assume that call is an ALU op
                            if (receiverType == Float.class ||
                                (receiverType == Fixnum.class && argType == Float.class))
                            {
                                if (setOperandType(outTypes, dst, Float.class)) {
                                    changed = true;
                                }

                                // If 'r' and 'a' are not already in unboxed forms at this point,
                                // they will get unboxed after this, because we want to opt. this call
                                if (r instanceof Variable) {
                                    unboxedVars.add((Variable)r);
                                }
                                if (a instanceof Variable) {
                                    unboxedVars.add((Variable)a);
                                }
                                dirtied = true;
                            } else if (receiverType == Fixnum.class && argType == Fixnum.class) {
                                if (setOperandType(outTypes, dst, Fixnum.class)) {
                                    changed = true;
                                }
                            } else {
                                if (setOperandType(outTypes, dst, Object.class)) {
                                    changed = true;
                                }
                            }
                        } else {
                            if (setOperandType(outTypes, dst, Object.class)) {
                                changed = true;
                            }
                        }
                    }
                } else {
                    // We dont know how to optimize this instruction.
                    // So, we assume we dont know type of the result.
                    // TOP/class --> BOTTOM
                    if (setOperandType(outTypes, dst, Object.class)) {
                        changed = true;
                    }
                }

                if (dirtied) {
                    unboxedVars.add(dst);
                    unboxedDirtyVars.add(dst);
                } else {
                    // Since the instruction didn't run in unboxed form,
                    // dirty unboxed vars will have to get boxed here.
                    updateUnboxedVarsInfo(i, unboxedVars, unboxedDirtyVars, dst, hasRescuer);
                }
            } else {
                // Since the instruction didn't run in unboxed form,
                // dirty unboxed vars will have to get boxed here.
                updateUnboxedVarsInfo(i, unboxedVars, unboxedDirtyVars, null, hasRescuer);
            }
        }

        changed = changed || !unboxedVars.equals(unboxedVarsOut);
        if (changed) {
            unboxedVarsOut = unboxedVars;
        }

        return changed;
    }

    private TemporaryVariable getUnboxedVar(Map<Variable, TemporaryVariable> unboxMap, Variable v) {
        TemporaryVariable unboxedVar = unboxMap.get(v);
        if (unboxedVar == null) {
            unboxedVar = this.problem.getScope().getNewFloatVariable();
            unboxMap.put(v, unboxedVar);
        }
        return unboxedVar;
    }

    private Operand getUnboxedOperand(Set<Variable> unboxedVars, Map<Variable, TemporaryVariable> unboxMap, Operand arg, List<Instr> newInstrs, boolean unbox) {
        if (arg instanceof Variable) {
            Variable v = (Variable)arg;
            boolean isUnboxed = unboxedVars.contains(v);
            if (unbox) {
                // Get a temp var for 'v' if we dont already have one
                TemporaryVariable unboxedVar = getUnboxedVar(unboxMap, v);
                // Unbox if 'v' is not already unboxed
                if (!isUnboxed) {
                    newInstrs.add(new UnboxFloatInstr(unboxedVar, v));
                }

                return unboxedVar;
            } else {
                // Get a temp var for 'v' if we dont already have one
                // Else, don't unbox
                return isUnboxed ? getUnboxedVar(unboxMap, v) : arg;
            }
        } else {
            return arg;
        }
    }

    private Operand getUnboxedOperand(Set<Variable> unboxedVars, Map<Variable, TemporaryVariable> unboxMap, Operand arg, List<Instr> newInstrs) {
        return getUnboxedOperand(unboxedVars, unboxMap, arg, newInstrs, true);
    }

    private void boxRequiredVars(Instr i, Set<Variable> unboxedVars, Set<Variable> unboxedDirtyVars, Map<Variable, TemporaryVariable> unboxMap, Variable dst, boolean hasRescuer, List<Instr> newInstrs) {
        // Special treatment for instructions that can raise exceptions
        HashSet<Variable> varsToBox = new HashSet<Variable>();
        if (i.canRaiseException()) {
            if (hasRescuer) {
                // If we are going to be rescued,
                // box all unboxed dirty vars before we execute the instr
                varsToBox.addAll(unboxedDirtyVars);
            } else {
                // We are going to exit if an exception is raised.
                // So, only need to bother with dirty live local vars for closures
                if (this.problem.getScope() instanceof IRClosure) {
                    for (Variable v: unboxedDirtyVars) {
                        if (v instanceof LocalVariable) {
                            varsToBox.add(v);
                        }
                    }
                }
            }
        }

        // B_TRUE and B_FALSE have unboxed forms and their operands
        // needn't get boxed back.
        Operation op = i.getOperation();
        boolean isBranch = op == Operation.B_TRUE || op == Operation.B_FALSE;
        if (!isBranch) {
            // Vars used by this instruction that only exist in unboxed form
            // will have to get boxed before it is executed
            for (Variable v: i.getUsedVariables()) {
                // if (unboxedDirtyVars.contains(v)) {
                if (unboxedVars.contains(v)) {
                    varsToBox.add(v);
                }
            }
        }

        // Add boxing instrs.
        for (Variable v: varsToBox) {
            newInstrs.add(new BoxFloatInstr(v, getUnboxedVar(unboxMap, v)));
            unboxedDirtyVars.remove(v);
        }

        // Add 'i' itself
        if (isBranch) {
            BranchInstr bi = (BranchInstr)i;
            Operand a = bi.getArg1();
            Operand ua = getUnboxedOperand(unboxedVars, unboxMap, a, newInstrs, false);
            if (ua == a) {
                newInstrs.add(i);
            } else if (op == Operation.B_TRUE) {
                newInstrs.add(new BTrueInstr(Operation.B_TRUE_UNBOXED, ua, bi.getJumpTarget()));
            } else {
                newInstrs.add(new BFalseInstr(Operation.B_FALSE_UNBOXED, ua, bi.getJumpTarget()));
            }
        } else {
            newInstrs.add(i);
        }

        // If the instruction writes into 'dst', it will be in boxed form.
        if (dst != null) {
            unboxedVars.remove(dst);
            unboxedDirtyVars.remove(dst);
        }
    }


    public void unbox(Map<Variable, TemporaryVariable> unboxMap) {
/*
        System.out.println("BB : " + basicBlock + " in " + this.problem.getScope().getName());
        System.out.println("-- known types on entry:");
        for (Variable v: inTypes.keySet()) {
            if (inTypes.get(v) != Object.class) {
                System.out.println(v + "-->" + inTypes.get(v));
            }
        }
        System.out.print("-- unboxed vars on entry:");
        for (Variable v: unboxedVarsIn) {
            System.out.print(" " + v);
        }
        System.out.println("------");
        System.out.print("-- unboxed vars on exit:");
        for (Variable v: unboxedVarsOut) {
            System.out.print(" " + v);
        }
        System.out.println("------");
*/

        // Compute UNION(unboxedVarsIn(all-successors)) - this.unboxedVarsOut
        // All vars in this new set have to be unboxed on exit from this BB
        Set<Variable> succUnboxedVars = new HashSet<Variable>();
        for (Edge e: problem.getScope().cfg().getOutgoingEdges(basicBlock)) {
            BasicBlock b = (BasicBlock)e.getDestination().getData();
            UnboxableOpsAnalysisNode x = (UnboxableOpsAnalysisNode)problem.getFlowGraphNode(b);
            succUnboxedVars.addAll(x.unboxedVarsIn);
        }

        succUnboxedVars.removeAll(unboxedVarsOut);

        // Only worry about vars live on exit
        LiveVariablesProblem lvp = (LiveVariablesProblem)problem.getScope().getDataFlowSolution(DataFlowConstants.LVP_NAME);
        BitSet liveVarsSet = ((LiveVariableNode)lvp.getFlowGraphNode(basicBlock)).getLiveInBitSet();

        // Rescue node, if any
        IRScope scope = this.problem.getScope();
        boolean hasRescuer = getNonExitBBExceptionTargetNode() != null;

        Set<Variable> unboxedVars = new HashSet<Variable>(unboxedVarsIn);
        Set<Variable> unboxedDirtyVars = new HashSet<Variable>();
        List<Instr> newInstrs = new ArrayList<Instr>();
        boolean unboxedLiveVars = false;

        for (Instr i : basicBlock.getInstrs()) {
            //System.out.println("ORIG: " + i);
            if (i.getOperation().transfersControl()) {
                // Add unboxing instrs.
                for (Variable v: succUnboxedVars) {
                    if (liveVarsSet.get(lvp.getDFVar(v).getId())) {
                        newInstrs.add(new UnboxFloatInstr(getUnboxedVar(unboxMap, v), v));
                    }
                }
                unboxedLiveVars = true;
            }

            if (i instanceof ResultInstr) {
                boolean dirtied = false;
                Variable dst = ((ResultInstr) i).getResult();

                if (i instanceof CopyInstr) {
                    // Copies are easy
                    Operand src = ((CopyInstr)i).getSource();
                    Class srcType = getOperandType(inTypes, src);
                    setOperandType(inTypes, dst, srcType);

                    // If we have an unboxed type for 'src', we can leave this unboxed.
                    //
                    // FIXME: However, if 'src' is a constant, this could unnecessarily
                    // leave 'src' unboxed and lead to a boxing instruction further down
                    // at the use site of 'dst'. This indicates that leaving this unboxed
                    // should ideally be done 'on-demand'. This indicates that this could
                    // be a backward-flow algo OR that this algo should be run on a
                    // dataflow graph / SSA graph.
                    if (srcType == Float.class) {
                        Operand unboxedSrc = src instanceof Variable ? getUnboxedVar(unboxMap, (Variable)src) : src;
                        TemporaryVariable unboxedDst = getUnboxedVar(unboxMap, dst);
                        newInstrs.add(new CopyInstr(Operation.COPY_UNBOXED, unboxedDst, unboxedSrc));
                        dirtied = true;
                    }
                } else if (i instanceof CallBase) {
                    // Process calls specially -- these are what we want to optimize!
                    CallBase c = (CallBase)i;
                    Operand  o = c.getClosureArg(null);
                    if (o != null) {
                        // We have to either force all types to bottom after the call
                        // OR mimic how LVA handles this.
                        // FIXME: To be completed
                    } else {
                        MethAddr m = c.getMethodAddr();
                        Operand  r = c.getReceiver();
                        Operand[] args = c.getCallArgs();
                        if (args.length == 1 && m.resemblesALUOp()) {
                            Operand a = args[0];
                            Class receiverType = getOperandType(inTypes, r);
                            Class argType = getOperandType(inTypes, a);
                            // Optimistically assume that call is an ALU op
                            if (receiverType == Float.class ||
                                (receiverType == Fixnum.class && argType == Float.class))
                            {
                                setOperandType(inTypes, dst, Float.class);
                                r = getUnboxedOperand(unboxedVars, unboxMap, r, newInstrs);
                                a = getUnboxedOperand(unboxedVars, unboxMap, a, newInstrs);
                                TemporaryVariable unboxedDst = getUnboxedVar(unboxMap, dst);
                                newInstrs.add(new AluInstr(m.getUnboxedOp(Float.class), unboxedDst, r, a));
                                dirtied = true;
                            } else if (receiverType == Fixnum.class && argType == Fixnum.class) {
                                setOperandType(inTypes, dst, Fixnum.class);
                            } else {
                                setOperandType(inTypes, dst, Object.class);
                            }
                        } else {
                            setOperandType(inTypes, dst, Object.class);
                        }
                    }
                } else {
                    // We dont know how to optimize this instruction.
                    // So, we assume we dont know type of the result.
                    // TOP/class --> BOTTOM
                    setOperandType(inTypes, dst, Object.class);
                }

                if (dirtied) {
                    unboxedVars.add(dst);
                    unboxedDirtyVars.add(dst);
                } else {
                    // Since the instruction didn't run in unboxed form,
                    // dirty unboxed vars will have to get boxed here.
                    boxRequiredVars(i, unboxedVars, unboxedDirtyVars, unboxMap, dst, hasRescuer, newInstrs);
                }
            } else {
                // Since the instruction didn't run in unboxed form,
                // dirty unboxed vars will have to get boxed here.
                boxRequiredVars(i, unboxedVars, unboxedDirtyVars, unboxMap, null, hasRescuer, newInstrs);
            }
        }

        // Add unboxing instrs.
        if (!unboxedLiveVars) {
            for (Variable v: succUnboxedVars) {
                if (liveVarsSet.get(lvp.getDFVar(v).getId())) {
                    newInstrs.add(new UnboxFloatInstr(getUnboxedVar(unboxMap, v), v));
                }
            }
        }

/*
        System.out.println("------");
        for (Instr i : newInstrs) {
            System.out.println("NEW: " + i);
        }
*/

        basicBlock.replaceInstrs(newInstrs);
        if (problem.getScope().getName().equals("mandelbrot_foo")) {
        }
    }

    @Override
    public String toString() {
        return "";
    }

    Set<Variable> unboxedVarsIn;    // On entry to flow graph node: variables that exist in unboxed form
    Set<Variable> unboxedVarsOut;   // On exit from flow graph node: variables that exist in unboxed form

    Map<Variable, Class> inTypes;   // On entry to flow graph node:  known types of variables
    Map<Variable, Class> outTypes;  // On exit from flow graph node: known types of variables
}
