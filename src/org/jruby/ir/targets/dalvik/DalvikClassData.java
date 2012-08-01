package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.DexMaker;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

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
    
    public void pushmethod(String name, int arity, TypeId clstype) {
        switch (arity) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                throw new RuntimeException("Unsupported arity " + arity + " for " + name);
        }
        
        MethodId methodId = clstype.getMethod(TypeId.VOID, "main");
        methodStack.push(new DalvikMethodData(new DexMethodAdapter(cls, Modifier.PUBLIC | Modifier.STATIC, methodId, null), arity));
    }
    
    public void popmethod() {
        methodStack.pop();
    }
        
    public DexMaker cls;
    public String clsName;
    Stack<DalvikMethodData> methodStack = new Stack();
    public Set<String> fieldSet = new HashSet<String>();

}
