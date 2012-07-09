package org.jruby.parser;

import org.jruby.Ruby;

/**
 * Allocate IR-friendly static scopes (it is also a marker
 * for constructing IR-friendly dynamic scopes.
 */
public class IRStaticScopeFactory extends StaticScopeFactory {
    public IRStaticScopeFactory(Ruby runtime) {
        super(runtime);
    }

    @Override
    public StaticScope newBlockScope(StaticScope parent) {
        return new IRStaticScope(IRStaticScopeType.BLOCK, parent);
    }

    @Override
    public StaticScope newBlockScope(StaticScope parent, String[] names) {
        return new IRStaticScope(IRStaticScopeType.BLOCK, parent, names);
    }

    @Override
    public StaticScope newEvalScope(StaticScope parent) {
        return new IRStaticScope(IRStaticScopeType.EVAL, parent);
    }

    @Override
    public StaticScope newEvalScope(StaticScope parent, String[] names) {
        return new IRStaticScope(IRStaticScopeType.EVAL, parent, names);
    }

    @Override
    public StaticScope newLocalScope(StaticScope parent) {
        return new IRStaticScope(IRStaticScopeType.LOCAL, parent);
    }

    @Override
    public StaticScope newLocalScope(StaticScope parent, String[] names) {
        return new IRStaticScope(IRStaticScopeType.LOCAL, parent, names);
    }
    
    // We only call these from inside IR impl (IR is all or nothing)
    public static StaticScope newIRBlockScope(StaticScope parent) {
        return new IRStaticScope(IRStaticScopeType.BLOCK, parent);
    }
    
    // We only call these from inside IR impl (IR is all or nothing)    
    public static StaticScope newIRLocalScope(StaticScope parent) {
        return new IRStaticScope(IRStaticScopeType.LOCAL, parent);
    }
    
    // We only call these from inside IR impl (IR is all or nothing)    
    public static StaticScope newIREvalScope(StaticScope parent) {
        return new IRStaticScope(IRStaticScopeType.EVAL, parent);
    }
    
    public static IRStaticScope newStaticScope(StaticScope parent, IRStaticScopeType type, String[] names) {
        if(names == null) {
            return new IRStaticScope(type, parent);
        } else {
            return new IRStaticScope(type, parent, names);
        }
    }    
}
