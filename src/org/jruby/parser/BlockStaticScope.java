package org.jruby.parser;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;

public class BlockStaticScope extends StaticScope {
    private static final long serialVersionUID = -3882063260379968149L;

    public BlockStaticScope(StaticScope parentScope) {
        super(parentScope);
    }
    
    public StaticScope getLocalScope() {
        return getEnclosingScope().getLocalScope();
    }
    
    public int isDefined(String name) {
        int slot = exists(name); 
        if (slot >= 0) return slot;
        
        return getEnclosingScope().isDefined(name);
    }

    protected AssignableNode assign(ISourcePosition position, String name, Node value, StaticScope topScope) {
        int slot = exists(name);
        
        if (slot >= 0) {
            return new DAsgnNode(position, name, value);
        }

        return getEnclosingScope().assign(position, name, value, topScope);
    }

    public AssignableNode addAssign(ISourcePosition position, String name, Node value) {
        // TODO: This can be curried (see assignable in older code for clues)
        addVariable(name);
        return new DAsgnNode(position, name, value);
    }

    public Node declare(ISourcePosition position, String name) {
        return exists(name) >= 0 ? new DVarNode(position, name) : 
            getEnclosingScope().declare(position, name);
    }
}
