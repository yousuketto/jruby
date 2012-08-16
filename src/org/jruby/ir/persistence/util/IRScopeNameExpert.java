package org.jruby.ir.persistence.util;

import org.jruby.ir.IRMetaClassBody;
import org.jruby.ir.IRScope;

public enum IRScopeNameExpert {
    INSTANCE;
    
    private static final String NAME_AND_LINE_NUMBER_SEPARATOR = ":";
    
    /**
     * Returns unique name of current IRScope.
     * Sometimes multiple scopes may have same name in single rb file (e.g modules)
     * So we need to distinguish them when we are referencing them from instructions/operands.
     * @param scope
     * @return string disambiguated name of scopes
     */
    public String getDisambiguatedScopeName(final IRScope scope) {
        
        if (scope instanceof IRMetaClassBody) {
            return scope.getName(); // already disambiguated
        }
        
        final String scopeName = scope.getName();
        final int lineNumber = scope.getLineNumber();
        
        final StringBuilder disambiguatedScopeNameBuilder = new StringBuilder();
        // FIXME? What if someone would try to put multiple scopes with single name on single line
        // appending each with <code>;</code>
        disambiguatedScopeNameBuilder.append(scopeName).append(NAME_AND_LINE_NUMBER_SEPARATOR).append(lineNumber);
        
        return disambiguatedScopeNameBuilder.toString();
        
    }
}
