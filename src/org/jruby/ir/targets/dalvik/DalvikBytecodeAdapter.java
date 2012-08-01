package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.TypeId;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lynnewallace
 */
public class DalvikBytecodeAdapter {
    public DalvikBytecodeAdapter(DexMethodAdapter adapter, int arity, String... params) {
        this.adapter = adapter;
        this.arity = arity;
        this.params = params;
    }
    
    public void loadLocal(int i) {
        //adapter.aload(i, );
    }
    
    public int newLocal(String name, TypeId type) {
        int index = variableCount++;
        variableTypes.put(index, type);
        variableNames.put(index, name);
        return index;
    }
    
    public DexMethodAdapter adapter;
    private int variableCount = 0;
    private Map<Integer, TypeId> variableTypes = new HashMap<Integer, TypeId>();
    private Map<Integer, String> variableNames = new HashMap<Integer, String>();
    private int arity;
    private String[] params;
}
