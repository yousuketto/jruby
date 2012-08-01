package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.TypeId;
import java.util.HashMap;
import java.util.Map;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.targets.JVM;

/**
 *
 * @author lynnewallace
 */
public class DalvikMethodData {
    public DalvikMethodData(DexMethodAdapter method, int arity) {
        this.method = new DalvikBytecodeAdapter(method, arity);
    }
    
    public int local(Variable variable) {
        String newName = variable.getName().replace('%', '$');
        return local(newName, TypeId.get(variable.getClass()));
    }

    public int local(String newName) {
        return local(newName, TypeId.OBJECT);
    }

    public int local(String newName, TypeId type) {
        if (varMap.containsKey(newName)) return varMap.get(newName);

        int index = method.newLocal(newName, type);
        varMap.put(newName, index);

        return index;
    }
    
    public DalvikBytecodeAdapter method;
    public Map<String, Integer> varMap = new HashMap<String, Integer>();
    
}
