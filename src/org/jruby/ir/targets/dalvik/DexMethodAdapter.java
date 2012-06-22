package org.jruby.ir.targets.dalvik;

import java.util.Stack;
import java.util.ArrayList;

import com.google.dexmaker.BinaryOp;
import com.google.dexmaker.Code;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;

/**
 *
 * @author lynnewallace
 */
public class DexMethodAdapter {
    private Code code;
    private Stack<Local> stack = new Stack<Local>();
    private ArrayList<Local> localVariables;
    
    public DexMethodAdapter(DexMaker dexmaker, int flags, MethodId method, ArrayList<TypeId> types) {
        setMethodVisitor(dexmaker.declare(method, flags));
        setLocalVariables(types);
    }
    
    /**
     * Local variables need to be set before other operations can take place (dexmaker issue)
     * So we take a list of all the variable types to be used in the order they will happen
     * and initialize and place them in a local variable list to be used throughout.
     */
    public void setLocalVariables(ArrayList<TypeId> types) {
        localVariables = new ArrayList<Local>(); 
        for(int i=0; i<types.size(); i++) {
            Local local = code.newLocal(types.get(i));
            localVariables.add(local);
        }
    }
    
    public Code getMethodVisitor() {
        return code;
    }
    
    public void setMethodVisitor(Code code) {
        this.code = code;
    }
    
    public void voidreturn() {
        getMethodVisitor().returnVoid();
    }
    
    public void iadd() {
        binaryOperations(BinaryOp.ADD);
    }
    
    public void ladd() {
        binaryOperations(BinaryOp.ADD);
    }
    
    public void fadd() {
        binaryOperations(BinaryOp.ADD);
    }
    
    public void dadd() {
        binaryOperations(BinaryOp.ADD);
    }
    
    public void isub() {
        binaryOperations(BinaryOp.SUBTRACT);
    }
    
    public void lsub() {
        binaryOperations(BinaryOp.SUBTRACT);
    }
    
    public void fsub() {
        binaryOperations(BinaryOp.SUBTRACT);
    }
    
    public void dsub() {
        binaryOperations(BinaryOp.SUBTRACT);
    }
    
    public void idiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void irem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void ldiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void lrem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void fdiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void frem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void ddiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void drem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void imul() {
        binaryOperations(BinaryOp.MULTIPLY);
    }
    
    public void lmul() {
        binaryOperations(BinaryOp.MULTIPLY);
    }
    
    public void fmul() {
        binaryOperations(BinaryOp.MULTIPLY);
    }
    
    public void dmul() {
        binaryOperations(BinaryOp.MULTIPLY);
    }
    
    /**
     * Pop arguments off the stack, perform operation then push result onto the stack
     * |2|  (op)  -->  |1 op 2|
     * |1|
     */
    public void binaryOperations(BinaryOp operation) {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().op(operation, arg1, arg1, arg2);
        stack.push(arg1);
    }
}