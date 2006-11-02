package org.jruby.runtime;

import org.jruby.parser.BlockStaticScope;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Represents the the dynamic portion of scoping information.  The variableValues are the
 * values of assigned local or block variables.  The staticScope identifies which sort of
 * scope this is (block or local).
 * 
 * Properties of Dynamic Scopes:
 * 1. static and dynamic scopes have the same number of names to values
 * 2. size of variables (and thus names) is determined during parsing.  So those structured do
 *    not need to change
 *
 * FIXME: When creating dynamic scopes we sometimes accidentally pass in extra parents.  This
 * is harmless (other than wasting memory), but we should not do that.  We can fix this in two
 * ways:
 * 1. Fix all callers
 * 2. Check parent that is passed in and make if new instance is local, then its parent is not local
 */
public class DynamicScope {
    // Our values holder (name of variables are kept in staticScope)
    private IRubyObject[] variableValues;
    
    // Static scoping information for this scope
    private StaticScope staticScope;
    
    // Captured dyanmic scopes
    private DynamicScope parent;

    public DynamicScope(StaticScope staticScope, DynamicScope parent) {
        this.staticScope = staticScope;
        this.parent = parent;
        
        int size = staticScope.getNumberOfVariables();
        if (size > 0) variableValues = new IRubyObject[size];
    }
    
    public DynamicScope cloneScope() {
        return new DynamicScope(staticScope, parent);
    }
    
    /**
     * Get all variable names captured (visible) by this scope (sans $~ and $_).
     * 
     * @return a list of variable names
     */
    public String[] getAllNamesInScope() {
        return staticScope.getAllNamesInScope();
    }
    
    /**
     * Get value from current scope or one of its captured scopes.
     * 
     * FIXME: block variables are not getting primed to nil so we need to null check those
     *  until we prime them properly.  Also add assert back in.
     * 
     * @param offset zero-indexed value that represents where variable lives
     * @param depth how many captured scopes down this variable should be set
     * @return the value here
     */
    public IRubyObject getValue(int offset, int depth) {
        if (depth > 0) {
            return parent.getValue(offset, depth - 1);
        }
        assert variableValues != null : "No variables in getValue for Off: " + offset + ", Dep: " + depth;
        assert offset < variableValues.length : "Index to big for getValue Off: " + offset + ", Dep: " + depth + ", O: " + this;
        // &foo are not getting set from somewhere...I want the following assert to be true though
        //assert variableValues[offset] != null : "Getting unassigned: " + staticScope.getVariables()[offset];
        return variableValues[offset];
    }

    /**
     * Set value in current dynamic scope or one of its captured scopes.
     * 
     * @param offset zero-indexed value that represents where variable lives
     * @param value to set
     * @param depth how many captured scopes down this variable should be set
     */
    public void setValue(int offset, IRubyObject value, int depth) {
        if (depth > 0) {
            assert parent != null : "If depth > 0, then parent should not ever be null";
            
            parent.setValue(offset, value, depth - 1);
        } else {
            assert offset < variableValues.length : "Setting " + offset + " to " + value + ", O: " + this; 

            if (getStaticScope().exists("_erbout") >= 0) {
                System.out.println("Setting value: " + value.inspect() + ","+value.getRuntime().getNil().inspect());
            }
            variableValues[offset] = value;
        }
    }

    /**
     * Set all values which represent 'normal' parameters in a call list to this dynamic
     * scope.  Function calls bind to local scopes by assuming that the indexes or the
     * arg list correspond to that of the local scope (plus 2 since $_ and $~ always take
     * the first two slots).  We pass in a second argument because we sometimes get more
     * values than we are expecting.  The rest get compacted by original caller into 
     * rest args.
     * 
     * @param values up to size specified to be mapped as ordinary parm values
     * @param size is the number of values to assign as ordinary parm values
     */
    public void setArgValues(IRubyObject[] values, int size) {
        for (int i = 0; i < size; i++) {
            setValue(i + 2, values[i], 0);
        }
    }


    // FIXME: Depending on profiling we may want to cache information on location and depth of
    // both $_ and/or $~ since in some situations they may happen a lot.  isDefined should be
    // fairly cheap, but you never know...
    
    public void setLastLine(IRubyObject value) {
        int location = staticScope.isDefined("$_");
        
        setValue(location & 0xffff, value, location >> 16);
    }
    
    public IRubyObject getLastLine() {
        int location = staticScope.isDefined("$_");

        return getValue(location & 0xffff, location >> 16);
    }

    public void setBackRef(IRubyObject value) {
        int location = staticScope.isDefined("$~");
        
        setValue(location & 0xffff, value, location >> 16);
    }
    
    public IRubyObject getBackRef() {
        int location = staticScope.isDefined("$~");

        return getValue(location & 0xffff, location >> 16);
    }

    /**
     * Get the static scope associated with this DynamicScope.
     * 
     * @return static complement to this scope
     */
    public StaticScope getStaticScope() {
        return staticScope;
    }
    
    public String toString() {
        return toString(new StringBuffer(), "");
    }

    // Helper function to give a good view of current dynamic scope with captured scopes
    private String toString(StringBuffer buf, String indent) {
        buf.append(indent).append("Static Type[" + hashCode() + "]: " + 
                (staticScope instanceof BlockStaticScope ? "block" : "local")+" [");
        int size = staticScope.getNumberOfVariables();
        
        if (size != 0) {
            String names[] = staticScope.getVariables();
            for (int i = 0; i < size-1; i++) {
                buf.append(names[i]).append("=");

                if (variableValues[i] == null) {
                    buf.append("null");
                } else {
                    buf.append(variableValues[i]);
                }
                
                buf.append(",");
            }
            buf.append(names[size-1]).append("=");
            
            assert variableValues.length == names.length : "V: " + variableValues.length + 
                " != N: " + names.length + " for " + buf;
            
            if (variableValues[size-1] == null) {
                buf.append("null");
            } else {
                buf.append(variableValues[size-1]);
            }
            
        }
        
        buf.append("]");
        if (parent != null) {
            buf.append("\n");
            parent.toString(buf, indent + "  ");
        }
        
        return buf.toString();
    }
}
