package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.Label;
import com.google.dexmaker.TypeId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyEncoding;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.ir.targets.Bootstrap;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.jruby.util.JavaNameMangler;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author lynnewallace
 */
public class DalvikBytecodeAdapter {
    public DalvikBytecodeAdapter(DexMethodAdapter adapter) {
        this.adapter = adapter;
    }
    
    public void push(Long l) {
        loadLocal(0);
        DalvikCallHelper.fixnum(adapter, l);
    }
    
    public void push(ByteList bl) {
        loadLocal(0);
        DalvikCallHelper.string(adapter, bl);
    }
    
    /**
     * Push a symbol on the stack
     * @param sym the symbol's string identifier
     */
    public void push(String sym) {
        loadLocal(0);
        DalvikCallHelper.invoke(TypeId.get(IRubyObject.class), "symbol", new TypeId[]{TypeId.get(ThreadContext.class)}, Bootstrap.symbol(), sym);
    }
    
    public void pushRuntime() {
        loadLocal(0);
        adapter.getfield(TypeId.get(ThreadContext.class), TypeId.get(Ruby.class), "runtime");
    }
    
    public void loadLocal(int i) {
        adapter.aload(i, variableTypes.get(i).getClass());
    }
    
    public void loadStaticScope() {
        loadLocal(1);
    }
    
    public void storeLocal(int i) {
        adapter.astore(i, variableTypes.get(i).getClass());
    }
    
    public void invokeOther(String name, int arity) {
        TypeId[] types = params(TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), arity);
        DalvikCallHelper.invokeOther(TypeId.get(IRubyObject.class), "invoke:" + JavaNameMangler.mangleMethodName(name),
                types, Bootstrap.invoke());
    }
    
    public void invokeSelf(String name, int arity) {
        TypeId[] types = params(TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), TypeId.get(String.class), TypeId.get(IRubyObject.class), arity);
        DalvikCallHelper.invokeSelf(adapter, TypeId.get(IRubyObject.class), JavaNameMangler.mangleMethodName(name), types);
    }
    
    public void invokeSuper(String name, int arity) {
        TypeId[] types = params(TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), arity);
        DalvikCallHelper.invoke(TypeId.get(IRubyObject.class), "invokeSuper:" + JavaNameMangler.mangleMethodName(name), types, 
                new Handle(Opcodes.H_INVOKESTATIC, "dummy", "dummy", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;J)Ljava/lang/invoke/CallSite;"));
    }
    
    public void invokeHelper(TypeId returntype, String name, TypeId... params) {
        adapter.invokestatic(TypeId.get(RuntimeHelpers.class), returntype, name, params);
    }
    
    public void searchConst(String name) {
        DalvikCallHelper.invoke(TypeId.get(IRubyObject.class), "searchConst:" + name, 
                new TypeId[]{TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class)}, Bootstrap.searchConst());
    }
    
    public void inheritanceSearchConst(String name) {
        DalvikCallHelper.invoke(TypeId.get(IRubyObject.class), "inheritanceSearchConst:" + name, 
                new TypeId[]{TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class)}, Bootstrap.inheritanceSearchConst());
    }
    
    public void goTo(Label label) {
        adapter.go_to(label);
    }
    
    public void isTrue() {
        adapter.invokeinterface(TypeId.get(IRubyObject.class), TypeId.BOOLEAN, "isTrue");
    }
    
    public void isNil() {
        adapter.invokeinterface(TypeId.get(IRubyObject.class), TypeId.BOOLEAN, "isNil");
    }

    public void bfalse(Label label) {
        adapter.iffalse(label);
    }

    public void btrue(Label label) {
        adapter.iftrue(label);
    }
    
    public void poll() {
        loadLocal(0);
        adapter.invokevirtual(TypeId.get(ThreadContext.class), TypeId.VOID, "pollThreadEvents");
    }
    
    public void pushNil() {
        loadLocal(0);
        adapter.getfield(TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), "nil");
    }
    
    public void pushBoolean(boolean b) {
        loadLocal(0);
        adapter.getfield(TypeId.get(ThreadContext.class), TypeId.get(Ruby.class), "runtime");
        if (b) {
            adapter.invokevirtual(TypeId.get(Ruby.class), TypeId.get(RubyBoolean.class), "getTrue");
        } else {
            adapter.invokevirtual(TypeId.get(Ruby.class), TypeId.get(RubyBoolean.class), "getFalse");
        }
    }
    
    public void pushObjectClass() {
        loadLocal(0);
        adapter.getfield(TypeId.get(ThreadContext.class), TypeId.get(Ruby.class), "runtime");
        adapter.invokevirtual(TypeId.get(Ruby.class),TypeId.get(RubyClass.class), "getObject");
    }
    
    public void pushUndefined() {
        adapter.getstatic(TypeId.get(UndefinedValue.class), TypeId.get(UndefinedValue.class), "UNDEFINED");
    }
    
    public void pushHandle(String className, String methodName, int arity) {
        adapter.ldc(new Handle(Opcodes.H_INVOKESTATIC, className, methodName, DalvikClassData.SIGS[arity]));
    }
    
    public void mark(Label label) {
        adapter.label(label);
    }
    
    public void putField(String name) {
        DalvikCallHelper.invoke(TypeId.VOID, "ivarSet:" + JavaNameMangler.mangleMethodName(name), 
                new TypeId[]{TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class)}, Bootstrap.ivar());
    }
    
    public void getField(String name) {
        DalvikCallHelper.invoke(TypeId.get(IRubyObject.class), "ivarGet:" + JavaNameMangler.mangleMethodName(name), 
                new TypeId[]{TypeId.get(IRubyObject.class)}, Bootstrap.ivar());
    }
    
    public void returnValue() {
        adapter.areturn();
    }
    
    public void array(int length) {
        TypeId[] types = params(TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), length);
        DalvikCallHelper.invoke(TypeId.get(IRubyObject.class), "array", types, Bootstrap.array());
    }
    
    public int newLocal(String name, TypeId type) {
        int index = variableCount++;
        variableTypes.put(index, type);
        variableNames.put(index, name);
        return index;
    }
    
    public Label newLabel() {
        return new Label();
    }
    
    public static TypeId[] params(TypeId typ1, TypeId typFill, int times) {
        TypeId[] types = new TypeId[times + 1];
        Arrays.fill(types, typFill);
        types[0] = typ1;
        return types;
    }
    
    public static TypeId[] params(TypeId typ1, TypeId typ2, TypeId typFill, int times) {
        TypeId[] types = new TypeId[times + 2];
        Arrays.fill(types, typFill);
        types[0] = typ1;
        types[1] = typ2;
        return types;
    }

    public static TypeId[] params(TypeId typ1, TypeId typ2, TypeId typ3, TypeId typFill, int times) {
        TypeId[] types = new TypeId[times + 3];
        Arrays.fill(types, typFill);
        types[0] = typ1;
        types[1] = typ2;
        types[2] = typ3;
        return types;
    }
    
    public DexMethodAdapter adapter;
    private int variableCount = 0;
    private Map<Integer, TypeId> variableTypes = new HashMap<Integer, TypeId>();
    private Map<Integer, String> variableNames = new HashMap<Integer, String>();
}