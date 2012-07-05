package org.jruby.ir.targets.dalvik;

import java.util.Stack;
import java.util.ArrayList;

import com.google.dexmaker.BinaryOp;
import com.google.dexmaker.Code;
import com.google.dexmaker.Comparison;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.Label;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;
import com.google.dexmaker.UnaryOp;

/**
 *
 * @author lynnewallace
 */
public class DexMethodAdapter {
    private Code code;
    private Stack<Local> stack = new Stack<Local>();
    private ArrayList<Local> localVariables;
    
    // Variables to allow casting
    private Local<Integer> intLocal;
    private Local<Float> floatLocal;
    private Local<Double> doubleLocal;
    private Local<Short> shortLocal;
    private Local<Long> longLocal;
    private Local<Byte> byteLocal;
    private Local<Character> charLocal;
    
    public DexMethodAdapter(DexMaker dexmaker, int flags, MethodId method, ArrayList<TypeId> types) {
        setMethodVisitor(dexmaker.declare(method, flags));
        setLocalVariables(types);
        setCastingVariables();
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
    
    public void setCastingVariables() {
        intLocal = code.newLocal(TypeId.INT);
        floatLocal = code.newLocal(TypeId.FLOAT);
        doubleLocal = code.newLocal(TypeId.DOUBLE);
        shortLocal = code.newLocal(TypeId.SHORT);
        longLocal = code.newLocal(TypeId.LONG);
        byteLocal = code.newLocal(TypeId.BYTE);
        charLocal = code.newLocal(TypeId.CHAR);
    }
    
    public Code getMethodVisitor() {
        return code;
    }
    
    public void setMethodVisitor(Code code) {
        this.code = code;
    }
    
    /**
     * Short-hand for specifying a set of aloads
     *
     * @param args list of aloads you want
     */
    public void aloadMany(int... args) {
        for (int arg: args) {
            aload(arg, Object.class);
        }
    }
    
    public void aload(int arg0, Class type) {
        Local local = getMethodVisitor().getParameter(arg0, TypeId.get(type));
        stack.push(local);
    }
    
    public void iload(int arg0) {
        Local local = getMethodVisitor().getParameter(arg0, TypeId.INT);
        stack.push(local);
    }
    
    public void lload(int arg0) {
        Local local = getMethodVisitor().getParameter(arg0, TypeId.LONG);
        stack.push(local);
    }
    
    public void fload(int arg0) {
        Local local = getMethodVisitor().getParameter(arg0, TypeId.FLOAT);
        stack.push(local);
    }
    
    public void dload(int arg0) {
        Local local = getMethodVisitor().getParameter(arg0, TypeId.DOUBLE);
        stack.push(local);
    }
    
    public void astore(int arg0, Class type) {
        Local local = stack.pop();
        Local target = getMethodVisitor().getParameter(arg0, TypeId.get(type));
        getMethodVisitor().move(target, local);
    }
    
    public void istore(int arg0) {
        Local local = stack.pop();
        Local target = getMethodVisitor().getParameter(arg0, TypeId.INT);
        getMethodVisitor().move(target, local);
    }
    
    public void lstore(int arg0) {
        Local local = stack.pop();
        Local target = getMethodVisitor().getParameter(arg0, TypeId.LONG);
        getMethodVisitor().move(target, local);
    }
    
    public void fstore(int arg0) {
        Local local = stack.pop();
        Local target = getMethodVisitor().getParameter(arg0, TypeId.FLOAT);
        getMethodVisitor().move(target, local);
    }
    
    public void dstore(int arg0) {
        Local local = stack.pop();
        Local target = getMethodVisitor().getParameter(arg0, TypeId.DOUBLE);
        getMethodVisitor().move(target, local);
    }
    
    public void ldc(Object arg0) {
        Local local = localVariables.get(0);
        getMethodVisitor().loadConstant(local, arg0);
        stack.push(local);
        localVariables.remove(0);
    }
    
    public void bipush(int arg) {
        pushValue(arg);
    }
    
    public void sipush(int arg) {
        pushValue(arg);
    }
        
    public void pushInt(int value) {
        pushValue(value);
    }
    
    public void pushBoolean(boolean bool) {
        if (bool) iconst_1(); else iconst_0();
    }
    
    public void areturn() {
        Local local = stack.pop();
        getMethodVisitor().returnValue(local);
    }

    public void ireturn() {
        Local local = stack.pop();
        getMethodVisitor().returnValue(local);
    }
    
    public void freturn() {
        Local local = stack.pop();
        getMethodVisitor().returnValue(local);
    }
    
    public void lreturn() {
        Local local = stack.pop();
        getMethodVisitor().returnValue(local);
    }
    
    public void dreturn() {
        Local local = stack.pop();
        getMethodVisitor().returnValue(local);
    }
    
    public void voidreturn() {
        getMethodVisitor().returnVoid();
    }
    
    public void iconst_m1() {
        pushValue(-1);
    }
    
    public void iconst_0() {
        pushValue(0);
    }
    
    public void iconst_1() {
        pushValue(1);
    }
    
    public void iconst_2() {
        pushValue(2);
    }
    
    public void iconst_3() {
        pushValue(3);
    }
    
    public void iconst_4() {
        pushValue(4);
    }
    
    public void iconst_5() {
        pushValue(5);
    }
    
    public void lconst_0() {
        pushValue(0);
    }
    
    public void aconst_null() {
        Local local = localVariables.get(0);
        getMethodVisitor().loadConstant(local, null);
        stack.push(local);
        localVariables.remove(0);
    }
    
    public void label(Label label) {
        getMethodVisitor().mark(label);
    }
    
    public void arrayload() {
        aaload();
    }
    
    public void arraystore() {
        aastore();
    }
    
    public void iarrayload() {
        iaload();
    }
    
    public void barrayload() {
        baload();
    }
    
    public void barraystore() {
        bastore();
    }
    
    public void aaload() {
        arrayloader();
    }
    
    public void aastore() {
        arraystorer();
    }
    
    public void iaload() {
        arrayloader();
    }
    
    public void iastore() {
        arraystorer();
    }
    
    public void laload() {
        arrayloader();
    }
    
    public void lastore() {
        arraystorer();
    }
    
    public void baload() {
        arrayloader();
    }
    
    public void bastore() {
        arraystorer();
    }
    
    public void saload() {
        arrayloader();
    }
    
    public void sastore() {
        arraystorer();
    }
    
    public void caload() {
        arrayloader();
    }
    
    public void castore() {
        arraystorer();
    }
    
    public void faload() {
        arrayloader();
    }
    
    public void fastore() {
        arraystorer();
    }
    
    public void daload() {
        arrayloader();
    }
    
    public void dastore() {
        arraystorer();
    }
    
    public void fcmpl() {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, -1);
        stack.push(intLocal);
    }
    
    public void fcmpg() {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, 1);
        stack.push(intLocal);
    }
    
    public void dcmpl() {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, -1);
        stack.push(intLocal);
    }
    
    public void dcmpg() {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, 1);
        stack.push(intLocal);
    }
    
    public void ifeq(Label arg0) {
        comparisonzero(Comparison.EQ, arg0);
    }

    public void iffalse(Label arg0) {
        ifeq(arg0);
    }
    
    public void ifne(Label arg0) {
        comparisonzero(Comparison.NE, arg0);
    }

    public void iftrue(Label arg0) {
        ifne(arg0);
    }
    
    public void if_acmpne(Label arg0) {
        comparison(Comparison.NE, arg0);
    }
    
    public void if_acmpeq(Label arg0) {
        comparison(Comparison.EQ, arg0);
    }
    
    public void if_icmple(Label arg0) {
        comparison(Comparison.LE, arg0);
    }
    
    public void if_icmpgt(Label arg0) {
        comparison(Comparison.GT, arg0);
    }
    
    public void if_icmplt(Label arg0) {
        comparison(Comparison.LT, arg0);
    }
    
    public void if_icmpne(Label arg0) {
        comparison(Comparison.NE, arg0);
    }
    
    public void if_icmpeq(Label arg0) {
        comparison(Comparison.EQ, arg0);
    }
    
    public void ishr() {
        binaryOperations(BinaryOp.SHIFT_RIGHT);
    }
     
    public void ishl() {
        binaryOperations(BinaryOp.SHIFT_LEFT);
    }
    
    public void iushr() {
        binaryOperations(BinaryOp.UNSIGNED_SHIFT_RIGHT);
    }
    
    public void lshr() {
        binaryOperations(BinaryOp.SHIFT_RIGHT);
    }
    
    public void lshl() {
        binaryOperations(BinaryOp.SHIFT_LEFT);
    }
    
    public void lushr() {
        binaryOperations(BinaryOp.UNSIGNED_SHIFT_RIGHT);
    }
    
    public void lcmp() {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareLongs(intLocal, arg1, arg2);
        stack.push(intLocal);
    }
    
    public void iand() {
        binaryOperations(BinaryOp.AND);
    }
    
    public void ior() {
        binaryOperations(BinaryOp.OR);
    }
    
    public void ixor() {
        binaryOperations(BinaryOp.XOR);
    }
    
    public void land() {
        binaryOperations(BinaryOp.AND);
    }
    
    public void lor() {
        binaryOperations(BinaryOp.OR);
    }
    
    public void lxor() {
        binaryOperations(BinaryOp.XOR);
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
    
    public void ineg() {
        unaryOperations(UnaryOp.NEGATE);
    }
    
    public void ldiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void lrem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void lneg() {
        unaryOperations(UnaryOp.NEGATE);
    }
    
    public void fdiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void frem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void fneg() {
        unaryOperations(UnaryOp.NEGATE);
    }
    
    public void ddiv() {
        binaryOperations(BinaryOp.DIVIDE);
    }
    
    public void drem() {
        binaryOperations(BinaryOp.REMAINDER);
    }
    
    public void dneg() {
        unaryOperations(UnaryOp.NEGATE);
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
    
    public void i2d() {
        casting(doubleLocal);
    }
       
    public void i2l() {
        casting(longLocal);
    }
    
    public void i2f() {
        casting(floatLocal);
    }
    
    public void i2s() {
        casting(shortLocal);
    }
    
    public void i2c() {
        casting(charLocal);
    }
    
    public void i2b() {
        casting(byteLocal);
    }
    
    public void l2d() {
        casting(doubleLocal);
    }
    
    public void l2i() {
        casting(intLocal);
    }
    
    public void l2f() {
        casting(floatLocal);
    }
    
    public void f2d() {
        casting(doubleLocal);
    }
    
    public void f2i() {
        casting(intLocal);
    }
    
    public void f2l() {
        casting(longLocal);
    }
    
    public void d2f() {
        casting(floatLocal);
    }
    
    public void d2i() {
        casting(intLocal);
    }
    
    public void d2l() {
        casting(longLocal);
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
    
    // Pop argument off the stack, perform operation then push result onto the stack
    public void unaryOperations(UnaryOp operation) {
        Local arg = stack.pop();
        getMethodVisitor().op(operation, arg, arg);
        stack.push(arg);
    }
    
    // Cast local's type to argument's type and push back onto the stack
    public void casting(Local newtype) {
        Local oldtype = stack.pop();
        getMethodVisitor().cast(newtype, oldtype);
        stack.push(newtype);
    }
    
    // Take a local from the local list, load the value into it and push onto stack
    public void pushValue(int value) {
        Local local = localVariables.get(0);
        getMethodVisitor().loadConstant(local, value);
        stack.push(local);
        localVariables.remove(0);
    }
    
    /**
     * Pop arguments off the stack, assign target the value at local[index]
     * then push target onto the stack
     */
    public void arrayloader() {
        Local target = localVariables.get(0);
        Local index = stack.pop();
        Local local = stack.pop();
        
        getMethodVisitor().aget(target, local, index);
        stack.push(target);
        localVariables.remove(0);
    }
    
    /**
     * Pop arguments off the stack, set element at index in local to the value source
     * then push local onto the stack
     */
    public void arraystorer() {
       Local source = stack.pop();
       Local index = stack.pop();
       Local local = stack.pop();
        
       getMethodVisitor().aput(local, index, source);
       stack.push(local); 
    }
    
    public void comparison(Comparison comp, Label arg0) {
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compare(comp, arg0, arg1, arg2);
    }
    
    public void comparisonzero(Comparison comp, Label arg0) {
        Local arg1 = stack.pop();
        code.loadConstant(intLocal, 0);
        getMethodVisitor().compare(comp, arg0, arg1, intLocal);
    }
}