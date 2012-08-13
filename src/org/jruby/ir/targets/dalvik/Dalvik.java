package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.DexMaker;
import com.google.dexmaker.FieldId;
import com.google.dexmaker.TypeId;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.util.JavaNameMangler;

/**
 *
 * @author lynnewallace
 */
public class Dalvik {
    Stack<DalvikClassData> clsStack = new Stack();
    List<DalvikClassData> clsAccum = new ArrayList();
    DexMaker dexmaker;
    TypeId clstype;
    
    public Dalvik() {
    }
    
    public byte[] code() {
        return dexmaker.generate();
    }
    
    public DexMaker cls() {
        return clsData().cls;
    }

    public DalvikClassData clsData() {
        return clsStack.peek();
    }
    
    public DalvikMethodData methodData() {
        return clsData().methodData();
    }
    
    public void pushclass(String clsName) {
        clsStack.push(new DalvikClassData(clsName, new DexMaker()));
    }
    
    public void pushscript(String clsName, String filename) {
        dexmaker = new DexMaker();
        clsStack.push(new DalvikClassData(clsName, dexmaker));

        clstype = TypeId.get("L" + clsName + ";");
        cls().declare(clstype, clsName, Modifier.PUBLIC, TypeId.OBJECT);
    }
    
    public void popclass() {
        clsStack.pop();
    }
    
    public DalvikBytecodeAdapter method() {
        return clsData().method();
    }
    
    public void pushmethod(String name, int arity) {
        // Set up locals
        clsData().pushmethod(name, arity, clstype);
        
        // locals for ThreadContext and self
        methodData().local("$context", TypeId.get(ThreadContext.class));
        methodData().local("$scope", TypeId.get(StaticScope.class));
        methodData().local("$self");
        for (int i = 0; i < arity; i++) {
            // incoming arguments
            methodData().local("$argument" + i);
        }
        methodData().local("$block", TypeId.get(Block.class));

        // TODO: this should go into the PushBinding instruction
        methodData().local("$dynamicScope");
    }
    
    public void popmethod() {
        clsData().popmethod();
    }
    
    public static String scriptToClass(String name) {
        if (name.equals("-e")) {
            return "DashE";
        } else {
            return JavaNameMangler.mangledFilenameForStartupClasspath(name);
        }
    }
    
    public void declareField(String field) {
        if (!clsData().fieldSet.contains(field)) {
            FieldId fieldId = clstype.getField(TypeId.OBJECT, field);
            cls().declare(fieldId, Modifier.PROTECTED, null);
            clsData().fieldSet.add(field);
        }
    }
}
