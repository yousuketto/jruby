package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.TypeId;
import java.util.HashMap;
import java.util.Map;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.Variable;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author lynnewallace
 */
public class DalvikMethodData {
    public DalvikMethodData(DexMethodAdapter method) {
        this.method = new DalvikBytecodeAdapter(method);
    }
    
    public int local(Variable variable) {
        String newName = variable.getName().replace('%', '$');
        return local(newName, TypeId.get(variable.getClass()));
    }

    public int local(String newName) {
        return local(newName, TypeId.get(IRubyObject.class));
    }

    public int local(String newName, TypeId type) {
        if (varMap.containsKey(newName)) return varMap.get(newName);

        int index = method.newLocal(newName, type);
        varMap.put(newName, index);

        return index;
    }
    
    public com.google.dexmaker.Label getLabel(Label label) {
        com.google.dexmaker.Label dexLabel = labelMap.get(label);
        if (dexLabel == null) {
            dexLabel = method.newLabel();
            labelMap.put(label, dexLabel);
        }
        return dexLabel;
    }
    
    public DalvikBytecodeAdapter method;
    public Map<String, Integer> varMap = new HashMap<String, Integer>();
    public Map<Label, com.google.dexmaker.Label> labelMap = new HashMap<Label, com.google.dexmaker.Label>();   
}
