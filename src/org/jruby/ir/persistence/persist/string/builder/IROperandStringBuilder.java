package org.jruby.ir.persistence.persist.string.builder;

import org.jruby.ir.operands.Operand;

public class IROperandStringBuilder extends AbstractIRStringBuilder<Operand> {
    private static final String PARAMETER_LIST_START_MARKER = "{";
    private static final String PARAMETER_SEPARATOR = ", ";
    private static final String PARAMETER_LIST_END_MARKER = "}";

    public IROperandStringBuilder(AbstractIRStringBuilder parentBuilder) {
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

    public void appendOperandType(Operand operand) {
        appendVerbatim(operand.getOperandType());
    }
}
