package org.jruby.compiler.ir.compiler_pass.opts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jruby.compiler.ir.IRClosure;
import org.jruby.compiler.ir.IRScope;
import org.jruby.compiler.ir.instructions.CallInstr;
import org.jruby.compiler.ir.instructions.CallBase;
import org.jruby.compiler.ir.instructions.CopyInstr;
import org.jruby.compiler.ir.instructions.Instr;
import org.jruby.compiler.ir.instructions.ResultInstr;
import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.operands.TemporaryVariable;
import org.jruby.compiler.ir.compiler_pass.CompilerPass;
import org.jruby.compiler.ir.representations.BasicBlock;
import org.jruby.compiler.ir.representations.CFG;

public class LocalOptimizationPass extends CompilerPass {
    public static String[] NAMES = new String[] { "lo", "LO", "local_optimization" };
    
    public String getLabel() {
        return "Local Optimizations";
    }
    
    // Should we run this pass on the current scope before running it on nested scopes?
    public boolean isPreOrder() {
        return false;
    }

    public Object execute(IRScope s, Object... data) {
        // Run this pass on nested closures first!
        // This let us compute execute scope flags for a method based on what all nested closures do
        for (IRClosure c: s.getClosures()) {
            run(c);
        }

        // Now, run on current scope
        CFG cfg = s.getCFG();
        if (cfg == null) {
            runLocalOpts(s);
        } else {
            for (BasicBlock b: cfg.getBasicBlocks()) {
                runLocalOptsOnInstrList(s, b.getInstrs().listIterator(), false);
            }
        }
        // Only after running local opts, compute various execution scope flags
        s.computeScopeFlags();
        
        return null;
    }

    private static void optimizeTmpVars(IRScope s) {
        // Pass 1: Analyze instructions and find use and def count of temporary variables
        Map<TemporaryVariable, List<Instr>> tmpVarUses = new HashMap<TemporaryVariable, List<Instr>>();
        Map<TemporaryVariable, List<Instr>> tmpVarDefs = new HashMap<TemporaryVariable, List<Instr>>();
        for (Instr i: s.getInstrs()) {
            for (Variable v: i.getUsedVariables()) {
                 if (v instanceof TemporaryVariable) {
                     TemporaryVariable tv = (TemporaryVariable)v;
                     List<Instr> uses = tmpVarUses.get(tv);
                     if (uses == null) {
                         uses = new ArrayList<Instr>();
                         tmpVarUses.put(tv, uses);
                     }
                     uses.add(i);
                 }
            }
            if (i instanceof ResultInstr) {
                Variable v = ((ResultInstr)i).getResult();
                if (v instanceof TemporaryVariable) {
                     TemporaryVariable tv = (TemporaryVariable)v;
                     List<Instr> defs = tmpVarDefs.get(tv);
                     if (defs == null) {
                         defs = new ArrayList<Instr>();
                         tmpVarDefs.put(tv, defs);
                     }
                     defs.add(i);
                }
            }
        }

        // Pass 2: Transform code and do additional analysis:
        // * If the result of this instr. has not been used, mark it dead
        // * Find copies where constant values are set
        Map<TemporaryVariable, Variable> removableCopies = new HashMap<TemporaryVariable, Variable>();
        ListIterator<Instr> instrs = s.getInstrs().listIterator();
        while (instrs.hasNext()) {
            Instr i = instrs.next();

            if (i instanceof ResultInstr) {
                Variable v = ((ResultInstr)i).getResult();
                if (v instanceof TemporaryVariable) {
                    // Deal with this code pattern:
                    //    %v = ...
                    // %v not used anywhere
                    List<Instr> uses = tmpVarUses.get((TemporaryVariable)v);
                    List<Instr> defs = tmpVarDefs.get((TemporaryVariable)v);
                    if (uses == null) {
                        if (i instanceof CopyInstr) {
                            i.markDead();
                            instrs.remove();
                        } else if (i instanceof CallInstr) {
                            instrs.set(((CallInstr)i).discardResult());
                        } else {
                            i.markUnusedResult();
                        }
                    }
                    // Deal with this code pattern:
                    //    %v = <some-operand>
                    //    .... %v ...
                    // %v not used or defined anywhere else
                    // So, %v can be replaced by the operand
                    else if ((uses.size() == 1) && (defs != null) && (defs.size() == 1) && (i instanceof CopyInstr)) {
                        CopyInstr ci = (CopyInstr)i;
                        Operand src = ci.getSource();
                        i.markDead();
                        instrs.remove();

                        // Fix up use
                        Map<Operand, Operand> copyMap = new HashMap<Operand, Operand>();
                        copyMap.put(v, src);
                        Instr soleUse = uses.get(0);
                        soleUse.simplifyOperands(copyMap, true);
                    }
                }
                // Deal with this code pattern:
                //    1: %v = ... (not a copy)
                //    2: x = %v
                // If %v is not used anywhere else, the result of 1. can be updated to use x and 2. can be removed
                //
                // NOTE: consider this pattern:
                //    %v = <operand> (copy instr)
                //    x = %v
                // This code will have been captured in the previous if branch which would have deleted %v = 5
                // Hence the check for whether the src def instr is dead
                else if (i instanceof CopyInstr) {
                    CopyInstr ci = (CopyInstr)i;
                    Operand src = ci.getSource();
                    if (src instanceof TemporaryVariable) {
                        TemporaryVariable vsrc = (TemporaryVariable)src;
                        List<Instr> uses = tmpVarUses.get(vsrc);
                        List<Instr> defs = tmpVarDefs.get(vsrc);
                        if ((uses.size() == 1) && (defs.size() == 1)) {
                            Instr soleDef = defs.get(0);
                            if (!soleDef.isDead()) {
                                // Fix up def
                                ((ResultInstr)soleDef).updateResult(ci.getResult());
                                ci.markDead();
                                instrs.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void recordSimplification(Variable res, Operand val, Map<Operand, Operand> valueMap, Map<Variable, List<Variable>> simplificationMap) {
        valueMap.put(res, val);

        // For all variables used by val, record a reverse mapping to let us track
        // Read-After-Write scenarios when any of these variables are modified.
        List<Variable> valVars = new ArrayList<Variable>(); 
        val.addUsedVariables(valVars);
        for (Variable v: valVars) {
           List<Variable> x = simplificationMap.get(v);
           if (x == null) {
              x = new ArrayList<Variable>();
              simplificationMap.put(v, x);
           }
           x.add(res);
        }
    }

    public static void runLocalOptsOnInstrList(IRScope s, ListIterator<Instr> instrs, boolean preCFG) {
        // Reset value map if this instruction is the start/end of a basic block
        //
        // Right now, calls are considered hard boundaries for optimization and
        // information cannot be propagated across them!
        //
        // SSS FIXME: Rather than treat all calls with a broad brush, what we need
        // is to capture different attributes about a call :
        //   - uses closures
        //   - known call target
        //   - can modify scope,
        //   - etc.
        //
        // This information is probably already present in the AST Inspector
        Map<Operand,Operand> valueMap = new HashMap<Operand,Operand>();
        Map<Variable,List<Variable>> simplificationMap = new HashMap<Variable,List<Variable>>();
        while (instrs.hasNext()) {
            Instr i = instrs.next();
            Operation iop = i.getOperation();
            if (preCFG && iop.startsBasicBlock()) {
                valueMap = new HashMap<Operand,Operand>();
                simplificationMap = new HashMap<Variable,List<Variable>>();
            }

            // Simplify instruction and record mapping between target variable and simplified value
            // System.out.println("BEFORE: " + i);
            Operand  val = i.simplifyAndGetResult(s, valueMap);
            // FIXME: This logic can be simplified based on the number of res != null checks only done if doesn't
            Variable res = i instanceof ResultInstr ? ((ResultInstr) i).getResult() : null;

            // System.out.println("For " + i + "; dst = " + res + "; val = " + val);
            // System.out.println("AFTER: " + i);

            if (res != null && val != null) {
                if (!res.equals(val)) { 
                    recordSimplification(res, val, valueMap, simplificationMap);
                } else if (!i.hasSideEffects()) {
                    if (i instanceof CopyInstr) {
                        if (i.canBeDeleted(s)) {
                            i.markDead();
                            instrs.remove();
                        }
                    } else {
                        instrs.set(new CopyInstr(res, val));
                    }
                }
            } else if (res != null && val == null) {
                // If we didn't get a simplified value, remove any existing simplifications for the result
                // to get rid of RAW hazards!
                valueMap.remove(res);
            }

            // Purge all entries in valueMap that have 'res' as their simplified value to take care of RAW scenarios (because we aren't in SSA form yet!)
            if ((res != null) && !res.equals(val)) {
                List<Variable> simplifiedVars = simplificationMap.get(res);
                if (simplifiedVars != null) {
                    for (Variable v: simplifiedVars) {
                        valueMap.remove(v);
                    }
                    simplificationMap.remove(res);
                }
            }

            // If the call has been optimized away in the previous step, it is no longer a hard boundary for opts!
            if ((preCFG && iop.endsBasicBlock()) || (iop.isCall() && !i.isDead())) {
                valueMap = new HashMap<Operand,Operand>();
                simplificationMap = new HashMap<Variable,List<Variable>>();
            }
        }
    }

    private static void runLocalOpts(IRScope s) {
        optimizeTmpVars(s);
        runLocalOptsOnInstrList(s, s.getInstrs().listIterator(), s.getCFG() == null);
    }
}
