package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.*;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author lynnewallace
 */
public class DexMethodAdapter {
    private Code code;
    private Stack<Local> stack = new Stack<Local>();
    private HashMap<TypeId, Local> localVariables;
    
    public DexMethodAdapter(DexMaker dexmaker, int flags, MethodId method, ArrayList<TypeId> types) {
        setMethodVisitor(dexmaker.declare(method, flags));
        setCastingVariables();
        setLocalVariables(types);
    }
    
    /**
     * Local variables need to be set before other operations can take place (dexmaker issue)
     * So we take a list of all the variable types to be used in the order they will happen
     * and initialize and place them in a local variable list to be used throughout.
     */
    public void setLocalVariables(ArrayList<TypeId> types) {
        localVariables = new HashMap<TypeId, Local>(); 
        for(int i = 0; i < types.size(); i++) {
            Local local = code.newLocal(types.get(i));
            if (!localVariables.containsKey(types.get(i))) {
                localVariables.put(types.get(i), local);
            }
        }
    }
    
    public void setCastingVariables() {
        localVariables.put(TypeId.INT, code.newLocal(TypeId.INT));
        localVariables.put(TypeId.FLOAT, code.newLocal(TypeId.FLOAT));
        localVariables.put(TypeId.DOUBLE, code.newLocal(TypeId.DOUBLE));
        localVariables.put(TypeId.SHORT, code.newLocal(TypeId.SHORT));
        localVariables.put(TypeId.LONG, code.newLocal(TypeId.LONG));
        localVariables.put(TypeId.BYTE, code.newLocal(TypeId.BYTE));
        localVariables.put(TypeId.CHAR, code.newLocal(TypeId.CHAR));
        localVariables.put(TypeId.OBJECT, code.newLocal(TypeId.OBJECT));
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
     * @param classes list of classes for the aloads
     */
    public void aloadMany(int[] args, Class[] classes) {
        for (int i = 0; i < args.length; i++) {
            aload(args[i], classes[i]);
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
        Local local = localVariables.get(TypeId.get(arg0.getClass()));
        getMethodVisitor().loadConstant(local, arg0);
        stack.push(local);
    }
    
    public void bipush(int arg) {
        pushValue(TypeId.BYTE, arg);
    }
    
    public void sipush(int arg) {
        pushValue(TypeId.SHORT, arg);
    }
    
    public void pushInt(int value) {
        if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
            switch (value) {
            case -1:
                iconst_m1();
                break;
            case 0:
                iconst_0();
                break;
            case 1:
                iconst_1();
                break;
            case 2:
                iconst_2();
                break;
            case 3:
                iconst_3();
                break;
            case 4:
                iconst_4();
                break;
            case 5:
                iconst_5();
                break;
            default:
                bipush(value);
                break;
            }
        } else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
            sipush(value);
        } else {
            ldc(value);
        }
    }
    
    public void pushBoolean(boolean bool) {
        if (bool) iconst_1(); else iconst_0();
    }
    
    public void invokestatic(TypeId arg1, TypeId arg2, String arg3, TypeId... arg4) {
        MethodId method = arg1.getMethod(arg2, arg3, arg4);
        Local target = localVariables.get(arg2);
        
        Local[] params = new Local[arg4.length];
        for (int i = arg4.length - 1; i == 0; i--) {
            params[i] = stack.pop();
        }
        
        getMethodVisitor().invokeStatic(method, target, params);      
    }
    
    public void invokespecial(TypeId arg1, TypeId arg2, String arg3, TypeId... arg4) {
        MethodId method = arg1.getMethod(arg2, arg3, arg4);
        Local target;
        
        Local[] params = new Local[arg4.length];
        for (int i = arg4.length - 1; i == 0; i--) {
            params[i] = stack.pop();
        }
        
        Local instance = stack.pop();
        
        if (arg2 == TypeId.VOID) {
           target = null; 
        } else {
            target = localVariables.get(arg2);
        }
        getMethodVisitor().invokeDirect(method, target, instance, params); 
        
    }
    
    public void invokevirtual(TypeId arg1, TypeId arg2, String arg3, TypeId... arg4) {
        MethodId method = arg1.getMethod(arg2, arg3, arg4);
        Local target;
        
        Local[] params = new Local[arg4.length];
        for (int i = arg4.length - 1; i == 0; i--) {
            params[i] = stack.pop();
        }
        
        Local instance = stack.pop();
        
        if (arg2 == TypeId.VOID) {
           target = null; 
        } else {
            target = localVariables.get(arg2);
        }
        
        getMethodVisitor().invokeVirtual(method, target, instance, params); 
        
    }
    
    public void invokeinterface(TypeId arg1, TypeId arg2, String arg3, TypeId... arg4) {
        MethodId method = arg1.getMethod(arg2, arg3, arg4);
        Local target;
        
        Local[] params = new Local[arg4.length];
        for (int i = arg4.length - 1; i == 0; i--) {
            params[i] = stack.pop();
        }
        
        Local instance = stack.pop();
        
        if (arg2 == TypeId.VOID) {
           target = null; 
        } else {
            target = localVariables.get(arg2);
        }
        getMethodVisitor().invokeInterface(method, target, instance, params); 
        
    }
    
    public void aprintln() {
        dup();
        getstatic(TypeId.get(System.class), TypeId.get(PrintStream.class), "out");
        swap();
        invokevirtual(TypeId.get(PrintStream.class), TypeId.VOID, "println", TypeId.OBJECT);
    }
    
    public void iprintln() {
        dup();
        getstatic(TypeId.get(System.class), TypeId.get(PrintStream.class), "out");
        swap();
        invokevirtual(TypeId.get(PrintStream.class), TypeId.VOID, "println", TypeId.INT);
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
    
    public void newobj(Class arg0, int argAmount) {
        Local local = localVariables.get(TypeId.get(arg0));
        
        TypeId objMethodType = TypeId.get(arg0);
        
        TypeId[] params = new TypeId[argAmount];
        Local[] args = new Local[argAmount];
        for (int i = argAmount - 1; i == 0; i--) {
            args[i] = stack.pop();
            params[i] = args[i].getType();
        }
        
        MethodId objMethod = objMethodType.getConstructor(params);
        getMethodVisitor().newInstance(local, objMethod, args);
    }
    
    public void dup() {
        Local local = stack.peek();
        stack.push(local);
    }
    
    public void swap() {
        Local first = stack.pop();
        Local second = stack.pop();
        stack.push(first);
        stack.push(second);
    }
    
    public void swap2() {
        dup2_x2();
        pop2();
    }
    
    public void getstatic(TypeId arg1, TypeId arg2, String arg3) {
        Local local = localVariables.get(arg2);
        
        FieldId field = arg1.getField(arg2, arg3);
        getMethodVisitor().sget(field, local);
        stack.push(local);
    }
    
    public void putstatic(TypeId arg1, TypeId arg2, String arg3) {
        FieldId field = arg1.getField(arg2, arg3);
        Local local = stack.pop();
        getMethodVisitor().sput(field, local);
    }
    
    public void getfield(TypeId arg1, TypeId arg2, String arg3) {
        Local local = localVariables.get(arg2);
        Local intLocal = localVariables.get(TypeId.INT);
        FieldId field = arg1.getField(arg2, arg3); 
        getMethodVisitor().iget(field, intLocal, intLocal);
        stack.push(local);
    }
    
    public void putfield(TypeId arg1, TypeId arg2, String arg3) {
        Local intLocal = localVariables.get(TypeId.INT);
        FieldId field = arg1.getField(arg2, arg3);
        Local local = stack.pop();
        getMethodVisitor().iput(field, intLocal, intLocal);
    }
    
    public void voidreturn() {
        getMethodVisitor().returnVoid();
    }
    
    public void newarray(TypeId type) {
        Local local = localVariables.get(type);
        
        Local length = stack.pop();
        getMethodVisitor().newArray(local, length);
        stack.push(local);
    }
    
    public void iconst_m1() {
        pushValue(TypeId.INT, -1);
    }
    
    public void iconst_0() {
        pushValue(TypeId.INT, 0);
    }
    
    public void iconst_1() {
        pushValue(TypeId.INT, 1);
    }
    
    public void iconst_2() {
        pushValue(TypeId.INT, 2);
    }
    
    public void iconst_3() {
        pushValue(TypeId.INT, 3);
    }
    
    public void iconst_4() {
        pushValue(TypeId.INT, 4);
    }
    
    public void iconst_5() {
        pushValue(TypeId.INT, 5);
    }
    
    public void lconst_0() {
        pushValue(TypeId.LONG, 0);
    }
    
    public void aconst_null() {
        Local local = localVariables.get(TypeId.OBJECT);
        getMethodVisitor().loadConstant(local, null);
        stack.push(local);
    }
    
    public void label(Label label) {
        getMethodVisitor().mark(label);
    }
    
    public void nop() {    
    }
    
    public void pop() {
        stack.pop();
    }
    
    public void pop2() {
        Local local = stack.pop();
        
        if (local.getType() != TypeId.LONG && local.getType() != TypeId.DOUBLE) {
            stack.pop();
        }
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
        arrayloader(TypeId.OBJECT);
    }
    
    public void aastore() {
        arraystorer();
    }
    
    public void iaload() {
        arrayloader(TypeId.INT);
    }
    
    public void iastore() {
        arraystorer();
    }
    
    public void laload() {
        arrayloader(TypeId.LONG);
    }
    
    public void lastore() {
        arraystorer();
    }
    
    public void baload() {
        arrayloader(TypeId.BYTE);
    }
    
    public void bastore() {
        arraystorer();
    }
    
    public void saload() {
        arrayloader(TypeId.SHORT);
    }
    
    public void sastore() {
        arraystorer();
    }
    
    public void caload() {
        arrayloader(TypeId.CHAR);
    }
    
    public void castore() {
        arraystorer();
    }
    
    public void faload() {
        arrayloader(TypeId.FLOAT);
    }
    
    public void fastore() {
        arraystorer();
    }
    
    public void daload() {
        arrayloader(TypeId.DOUBLE);
    }
    
    public void dastore() {
        arraystorer();
    }
    
    public void fcmpl() {
        Local intLocal = localVariables.get(TypeId.INT);
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, -1);
        stack.push(intLocal);
    }
    
    public void fcmpg() {
        Local intLocal = localVariables.get(TypeId.INT);
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, 1);
        stack.push(intLocal);
    }
    
    public void dcmpl() {
        Local intLocal = localVariables.get(TypeId.INT);
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, -1);
        stack.push(intLocal);
    }
    
    public void dcmpg() {
        Local intLocal = localVariables.get(TypeId.INT);
        Local arg2 = stack.pop();
        Local arg1 = stack.pop();
        getMethodVisitor().compareFloatingPoint(intLocal, arg1, arg2, 1);
        stack.push(intLocal);
    }
    
    public void dup_x2() {
        Local top = stack.pop();
        Local second = stack.pop();
        Local third = stack.pop();
        stack.push(top);
        stack.push(third);
        stack.push(second);
        stack.push(top);
    }
    
    public void dup_x1() {
        Local top = stack.pop();
        Local second = stack.pop();
        stack.push(top);
        stack.push(second);
        stack.push(top);
    }
    
    public void dup2_x2() {
        Local top = stack.pop();
        if (top.getType() == TypeId.LONG || top.getType() == TypeId.DOUBLE) {
            
            Local second = stack.pop();
            if (second.getType() == TypeId.LONG || second.getType() == TypeId.DOUBLE) {
                stack.push(top);
                stack.push(second);
                stack.push(top);
            } else {
                Local third = stack.pop();
                stack.push(top);
                stack.push(third);
                stack.push(second);
                stack.push(top);
            }
            
        } else {
            
            Local second = stack.pop();
            Local third = stack.pop();
            if (third.getType() == TypeId.LONG || third.getType() == TypeId.DOUBLE) {
                stack.push(second);
                stack.push(top); 
                stack.push(third);
                stack.push(second);
                stack.push(top);
            } else {
                Local fourth = stack.pop();    
                stack.push(second);
                stack.push(top); 
                stack.push(fourth);
                stack.push(third);
                stack.push(second);
                stack.push(top);
            }
        }
    }
    
    public void dup2_x1() {
        Local top = stack.pop();
        if (top.getType() == TypeId.LONG || top.getType() == TypeId.DOUBLE) {
            Local second = stack.pop();
            stack.push(top);
            stack.push(second);
            stack.push(top);
        } else {
            Local second = stack.pop();
            Local third = stack.pop();
            stack.push(second);
            stack.push(top);
            stack.push(third);
            stack.push(second);
            stack.push(top);
        }
        
    }
    
    public void dup2() {
        Local top = stack.pop();
        if (top.getType() == TypeId.LONG || top.getType() == TypeId.DOUBLE) {
            stack.push(top);
            stack.push(top);
        } else {
            Local second = stack.pop();
            stack.push(second);
            stack.push(top);
            stack.push(second);
            stack.push(top);
        }
    }
    
    public void go_to(Label arg0) {
        getMethodVisitor().jump(arg0);
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
    
    public void checkcast(Class checkType) {
        Local source = stack.peek();
        Local check = localVariables.get(TypeId.get(checkType));
        getMethodVisitor().cast(check, source);
    }
    
    public void ifnonnull(Label arg0) {
        comparisonzero(Comparison.NE, arg0);
    }
    
    public void ifnull(Label arg0) {
        comparisonzero(Comparison.EQ, arg0);
    }
    
    public void iflt(Label arg0) {
        comparisonzero(Comparison.LT, arg0);
    }
    
    public void ifle(Label arg0) {
        comparisonzero(Comparison.LE, arg0);
    }
    
    public void ifgt(Label arg0) {
        comparisonzero(Comparison.GT, arg0);
    }
    
    public void ifge(Label arg0) {
        comparisonzero(Comparison.GE, arg0);
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
        Local intLocal = localVariables.get(TypeId.INT);
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
        casting(TypeId.DOUBLE);
    }
       
    public void i2l() {
        casting(TypeId.LONG);
    }
    
    public void i2f() {
        casting(TypeId.FLOAT);
    }
    
    public void i2s() {
        casting(TypeId.SHORT);
    }
    
    public void i2c() {
        casting(TypeId.CHAR);
    }
    
    public void i2b() {
        casting(TypeId.BYTE);
    }
    
    public void l2d() {
        casting(TypeId.DOUBLE);
    }
    
    public void l2i() {
        casting(TypeId.INT);
    }
    
    public void l2f() {
        casting(TypeId.FLOAT);
    }
    
    public void f2d() {
        casting(TypeId.DOUBLE);
    }
    
    public void f2i() {
        casting(TypeId.INT);
    }
    
    public void f2l() {
        casting(TypeId.LONG);
    }
    
    public void d2f() {
        casting(TypeId.FLOAT);
    }
    
    public void d2i() {
        casting(TypeId.INT);
    }
    
    public void d2l() {
        casting(TypeId.LONG);
    }
    
    public void iinc(int variable, int amount) {
        Local intLocal = localVariables.get(TypeId.INT);
        iload(variable);
        getMethodVisitor().loadConstant(intLocal, amount);
        stack.push(intLocal);
        iadd();
        istore(variable);
    }
    
    public void monitorenter() {
        Local local = stack.pop();
        getMethodVisitor().monitorEnter(local);
    }
    
    public void monitorexit() {
        Local local = stack.pop();
        getMethodVisitor().monitorExit(local);
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
    public void casting(TypeId type) {
        Local newtype = localVariables.get(type);
        Local oldtype = stack.pop();
        getMethodVisitor().cast(newtype, oldtype);
        stack.push(newtype);
    }
    
    // Take a local from the local list, load the value into it and push onto stack
    public void pushValue(TypeId type, int value) {
        Local local = localVariables.get(type);
        getMethodVisitor().loadConstant(local, value);
        stack.push(local);
    }
    
    /**
     * Pop arguments off the stack, assign target the value at local[index]
     * then push target onto the stack
     */
    public void arrayloader(TypeId type) {
        Local target = localVariables.get(type);
        Local index = stack.pop();
        Local local = stack.pop();
        
        getMethodVisitor().aget(target, local, index);
        stack.push(target);
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
    
    public void comparison(Comparison comp, Label label) {
        Local arg1 = stack.pop();
        Local arg0 = stack.pop();
        getMethodVisitor().compare(comp, label, arg0, arg1);
    }
    
    public void comparisonzero(Comparison comp, Label label) {
        Local arg0 = stack.pop();
        Local zero = localVariables.get(TypeId.INT);
        code.loadConstant(zero, 0);
        getMethodVisitor().compare(comp, label, arg0, zero);
    }
}