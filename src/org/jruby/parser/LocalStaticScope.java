package org.jruby.parser;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.VCallNode;
import org.jruby.lexer.yacc.ISourcePosition;

public class LocalStaticScope extends StaticScope {
    private static final long serialVersionUID = 2204064248888411628L;

    protected LocalStaticScope(StaticScope enclosingScope) {
        super(enclosingScope);
        
        addVariable("$~");
        addVariable("$_");
    }

    public StaticScope getLocalScope() {
        return this;
    }

    public int isDefined(String name) {
        return exists(name); 
    }

    public AssignableNode assign(ISourcePosition position, String name, Node value, StaticScope topScope) {
        int slot = exists(name);
        
        // We can assign if we already have variable of that name here or we are the only
        // scope in the chain (which Local scopes always are).
        if (slot >= 0) {
            return new LocalAsgnNode(position, name, slot, value);
        } else if (topScope == this) {
            return new LocalAsgnNode(position, name, addVariable(name), value);
        }
        
        // We know this is a block scope because a local scope cannot be within a local scope
        // If topScope was itself it would have created a LocalAsgnNode above.
        return ((BlockStaticScope) topScope).addAssign(position, name, value);
    }

    public Node declare(ISourcePosition position, String name) {
        int slot = exists(name);
        
        if (slot >= 0) {
            return new LocalVarNode(position, slot, name);
        }
        
        return new VCallNode(position, name);
    }

}
