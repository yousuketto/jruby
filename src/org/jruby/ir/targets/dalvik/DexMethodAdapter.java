package org.jruby.ir.targets.dalvik;

import java.util.Stack;

import com.google.dexmaker.BinaryOp;
import com.google.dexmaker.Code;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.Label;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;

/**
 *
 * @author lynnewallace
 */
public class DexMethodAdapter {
    
    private Code code;
    private DexMaker dexmaker;
    private Label start;
    private Label end;
    private Stack stack;
    
    public DexMethodAdapter(TypeId<?> classtype, DexMaker dexmaker, int flags, MethodId method) {
        setMethodVisitor(dexmaker.declare(method, flags));
        this.dexmaker = dexmaker;
        this.start = new Label();
        this.end = new Label();
        this.stack = new Stack();
    }
    
    public Code getMethodVisitor() {
        return code;
    }
    
    public void setMethodVisitor(Code code) {
        this.code = code;
    }
    
    public void start() {
        getMethodVisitor().mark(start);
    }
    
    public void end() {
        getMethodVisitor().mark(end);
    }
    
    public void voidreturn() {
        getMethodVisitor().returnVoid();
    }
    
    public void iadd() {
        integerArithmetic(BinaryOp.ADD);
    }
    
    public void ladd() {
        longArithmetic(BinaryOp.ADD);
    }
    
    public void fadd() {
        floatArithmetic(BinaryOp.ADD);
    }
    
    public void dadd() {
        doubleArithmetic(BinaryOp.ADD);
    }
    
    public void isub() {
        integerArithmetic(BinaryOp.SUBTRACT);
    }
    
    public void lsub() {
        longArithmetic(BinaryOp.SUBTRACT);
    }
    
    public void fsub() {
        floatArithmetic(BinaryOp.SUBTRACT);
    }
    
    public void dsub() {
        doubleArithmetic(BinaryOp.SUBTRACT);
    }
    
    public void idiv() {
        integerArithmetic(BinaryOp.DIVIDE);
    }
    
    public void irem() {
        integerArithmetic(BinaryOp.REMAINDER);
    }
    
    public void ldiv() {
        longArithmetic(BinaryOp.DIVIDE);
    }
    
    public void lrem() {
        longArithmetic(BinaryOp.REMAINDER);
    }
    
    public void fdiv() {
        floatArithmetic(BinaryOp.DIVIDE);
    }
    
    public void frem() {
        floatArithmetic(BinaryOp.REMAINDER);
    }
    
    public void ddiv() {
        doubleArithmetic(BinaryOp.DIVIDE);
    }
    
    public void drem() {
        doubleArithmetic(BinaryOp.REMAINDER);
    }
    
     public void imul() {
        integerArithmetic(BinaryOp.MULTIPLY);
    }
    
    public void lmul() {
        longArithmetic(BinaryOp.MULTIPLY);
    }
    
    public void fmul() {
        floatArithmetic(BinaryOp.MULTIPLY);
    }
    
    public void dmul() {
        doubleArithmetic(BinaryOp.MULTIPLY);
    }
        
    public void integerArithmetic(BinaryOp operation) {
        Local<Integer> local = code.newLocal(TypeId.INT);
        Local<Integer> arg1 = (Local<Integer>) stack.pop();
        Local<Integer> arg2 = (Local<Integer>) stack.pop();
        getMethodVisitor().op(operation, local, arg1, arg2);
        stack.push(local);
    }
    
    public void longArithmetic(BinaryOp operation) {
        Local<Long> local = code.newLocal(TypeId.LONG);
        Local<Long> arg1 = (Local<Long>) stack.pop();
        Local<Long> arg2 = (Local<Long>) stack.pop();
        getMethodVisitor().op(operation, local, arg1, arg2);
        stack.push(local);
    }
    
    public void floatArithmetic(BinaryOp operation) {
        Local<Float> local = code.newLocal(TypeId.FLOAT);
        Local<Float> arg1 = (Local<Float>) stack.pop();
        Local<Float> arg2 = (Local<Float>) stack.pop();
        getMethodVisitor().op(operation, local, arg1, arg2);
        stack.push(local);
    }
    
    public void doubleArithmetic(BinaryOp operation) {
        Local<Double> local = code.newLocal(TypeId.DOUBLE);
        Local<Double> arg1 = (Local<Double>) stack.pop();
        Local<Double> arg2 = (Local<Double>) stack.pop();
        getMethodVisitor().op(operation, local, arg1, arg2);
        stack.push(local);
    }
}