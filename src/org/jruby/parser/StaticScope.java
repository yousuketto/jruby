package org.jruby.parser;

import java.io.Serializable;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;

public abstract class StaticScope implements Serializable {
    private static final long serialVersionUID = 4843861446986961013L;
    
    private StaticScope enclosingScope;
    
    // Our name holder (offsets are assigned as variables are added
    private String[] variableNames;
    
    protected StaticScope(StaticScope enclosingScope) {
        this.enclosingScope = enclosingScope;
    }
    
    public int addVariable(String name) {
        int slot = isDefined(name); 

        if (slot >= 0) {
            return slot;
        }
            
        // This is perhaps innefficient timewise?  Optimal spacewise
        if (variableNames == null) {
            variableNames = new String[1];
            variableNames[0] = name;
        } else {
            String[] newVariableNames = new String[variableNames.length + 1];
            System.arraycopy(variableNames, 0, newVariableNames, 0, variableNames.length);
            variableNames = newVariableNames;
            variableNames[variableNames.length - 1] = name;
        }
        
        // Returns slot of variable
        return variableNames.length - 1;
    }
    
    public String[] getVariables() {
        return variableNames;
    }
    
    public void setVariables(String[] names) {
        if (names == null) {
            return;
        }
        
        variableNames = new String[names.length];
        System.arraycopy(names, 0, variableNames, 0, names.length);
    }
    
    /**
     * Next outer most scope in list of scopes.  An enclosing scope may have no direct scoping
     * relationship to its child.  If I am in a localScope and then I enter something which
     * creates another localScope the enclosing scope will be the first scope, but there are
     * no valid scoping relationships between the two.  Methods which walk the enclosing scopes
     * are responsible for enforcing these relationships.
     * 
     * @return parent scope
     */
    public StaticScope getEnclosingScope() {
        return enclosingScope;
    }
    
    /**
     * Does the variable exist?
     * 
     * @param name of the variable to find
     * @return index of variable or -1 if it does not exist
     */
    public int exists(String name) {
        if (variableNames != null) {
            for (int i = 0; i < variableNames.length; i++) {
                if (name == variableNames[i] || name.equals(variableNames[i])) {
                    return i;
                }   
            }
        }
        
        return -1;        
    }
    
    public AssignableNode assign(ISourcePosition position, String name, Node value) {
        return assign(position, name, value, this);
    }
    
    public abstract int isDefined(String name);
    protected abstract AssignableNode assign(ISourcePosition position, String name, Node value, StaticScope topScope);
    public abstract Node declare(ISourcePosition position, String name);

    /**
     * Gets the Local Scope relative to the current Scope.  For LocalScopes this will be itself.
     * Blocks will contain the LocalScope it contains.
     * 
     * @return localScope
     */
    public abstract StaticScope getLocalScope();
}
