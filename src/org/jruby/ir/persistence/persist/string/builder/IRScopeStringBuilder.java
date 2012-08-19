package org.jruby.ir.persistence.persist.string.builder;

import org.jruby.ir.IRScope;
import org.jruby.ir.IRScopeType;
import org.jruby.ir.persistence.util.IRScopeNameExpert;

public class IRScopeStringBuilder extends AbstractIRStringBuilder<IRScope> {
    private static final String LINE_TERMINATOR = "\n";
    private static final String PARAMETER_LIST_START_MARKER = "<";
    private static final String PARAMETER_SEPARATOR = ", ";
    private static final String PARAMETER_LIST_END_MARKER = ">";

    public IRScopeStringBuilder(AbstractIRStringBuilder parentBuilder) {
        super(parentBuilder);
    }

    @Override
    String getParameterListStartMarker() {
        return PARAMETER_LIST_START_MARKER;
    }

    @Override
    String getParameterSeparator() {
        return PARAMETER_SEPARATOR;
    }

    @Override
    String getParameterListEndMarker() {
        return PARAMETER_LIST_END_MARKER;
    }
    
    public void appendScopeType(final IRScope irScope) {
        final IRScopeType scopeType = irScope.getScopeType();
        
        appendVerbatim(scopeType);
    }
    
    public void appendScopeName(final IRScope irScope) {
        final String disambiguatedScopeName = IRScopeNameExpert.INSTANCE
                .getDisambiguatedScopeName(irScope);

        appendEscapedString(disambiguatedScopeName);
    }

    public void finishLine() {
        appendVerbatim(LINE_TERMINATOR);
    }
}
