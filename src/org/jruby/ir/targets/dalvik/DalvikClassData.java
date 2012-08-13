package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.DexMaker;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.jruby.ir.targets.JVM;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.CodegenUtils;

/**
 *
 * @author lynnewallace
 */
class DalvikClassData {
        
    public DalvikClassData(String clsName, DexMaker cls) {
        this.clsName = clsName;
        this.cls = cls;
    }
    
    public DalvikBytecodeAdapter method() {
        return methodData().method;
    }
    
    public DalvikMethodData methodData() {
        return methodStack.peek();
    }
    
    public static final TypeId[][] ARGS = new TypeId[][] {
            new TypeId[]{TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class), 
                TypeId.get(IRubyObject.class), TypeId.get(Block.class)},
            new TypeId[]{TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class), 
                TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), TypeId.get(Block.class)},
            new TypeId[]{TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class), 
                TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), 
                TypeId.get(Block.class)},
            new TypeId[]{TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class), 
                TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), 
                TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class), TypeId.get(Block.class)}
    };
    
    public static final String[] SIGS = new String[] {
            CodegenUtils.sig(JVM.OBJECT, JVM.THREADCONTEXT, JVM.STATICSCOPE, JVM.OBJECT, JVM.BLOCK),
            CodegenUtils.sig(JVM.OBJECT, JVM.THREADCONTEXT, JVM.STATICSCOPE, JVM.OBJECT, JVM.OBJECT, JVM.BLOCK),
            CodegenUtils.sig(JVM.OBJECT, JVM.THREADCONTEXT, JVM.STATICSCOPE, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT, JVM.BLOCK),
            CodegenUtils.sig(JVM.OBJECT, JVM.THREADCONTEXT, JVM.STATICSCOPE, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT, JVM.BLOCK)
    };
    
    public void pushmethod(String name, int arity, TypeId clstype) {
        TypeId[] args;
        switch (arity) {
            case 0:
                args = ARGS[0];
                break;
            case 1:
                args = ARGS[1];
                break;
            case 2:
                args = ARGS[2];
                break;
            case 3:
                args = ARGS[3];
                break;
            default:
                throw new RuntimeException("Unsupported arity " + arity + " for " + name);
        }
        
        MethodId methodId = clstype.getMethod(TypeId.get(IRubyObject.class), name, args);
        methodStack.push(new DalvikMethodData(new DexMethodAdapter(cls, Modifier.PUBLIC | Modifier.STATIC, methodId, null)));
    }
    
    public void popmethod() {
        methodStack.pop();
    }
        
    public DexMaker cls;
    public String clsName;
    Stack<DalvikMethodData> methodStack = new Stack();
    public Set<String> fieldSet = new HashSet<String>();
}