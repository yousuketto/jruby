package org.jruby.ir.persistence.persist.string.builder;

import org.jruby.ir.IRScope;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.persistence.persist.string.IRToStringTranslator;
import org.jruby.ir.persistence.util.IRScopeNameExpert;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.parser.IRStaticScope;
import org.jruby.parser.IRStaticScopeType;
import org.jruby.runtime.Arity;
import org.jruby.util.KCode;
import org.jruby.util.RegexpOptions;

/**
 * Constructs string
 * That class contains code that is common for all ...IRStringBuilder's
 */
public abstract class AbstractIRStringBuilder<T> {
    
    private static final String DOUBLE_QUOTES = "\"";
    private static final String ESCAPED_DOUBLE_QUOTES = "\\\\\"";
    
    private static final String ARRAY_START_MARKER = "[";
    private static final String ARRAY_END_MARKER = "]";
    
    private final String PARAMETER_LIST_START_MARKER = getParameterListStartMarker();
    private final String PARAMETER_SEPARATOR = getParameterSeparator();
    private final String PARAMETER_LIST_END_MARKER = getParameterListEndMarker();

    private final StringBuilder builder;

    // Take StringBuilder from parent or create it if there is no parent
    AbstractIRStringBuilder(AbstractIRStringBuilder parent) {
        if (parent == null) {
            builder = new StringBuilder();
        } else {
            builder = parent.builder;
        }
    }

    abstract String getParameterListStartMarker();

    abstract String getParameterSeparator();

    abstract String getParameterListEndMarker();

    public void appendParameters(final Object... parameters) {
        builder.append(PARAMETER_LIST_START_MARKER);

        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                builder.append(PARAMETER_SEPARATOR);
            }

            final Object parameter = parameters[i];
            appendParameter(parameter);
        }

        builder.append(PARAMETER_LIST_END_MARKER);
    }

    private void appendParameter(final Object parameter) {
        
        // We need these ugly instanceof's because the choice of
        // which overloading to invoke is made at compile time
        // so we can't simply write appendParameter(String string) etc.
        // TODO: Maybe we have to get rid of that varargs method
        // and use real builder approach (e.g. builder.appendOperand(op).appendString(str)... ) 
        if (parameter instanceof Operand) {
            appendOperandParameter((Operand) parameter);

        } else if (parameter instanceof String) {
            appendEscapedString((String) parameter);

        } else if (parameter instanceof Number) {
            appendVerbatim(parameter);
        } else if (parameter instanceof Boolean) {
            appendVerbatim(parameter);
        } else if (parameter instanceof Enum) {
            appendEnum((Enum) parameter);
            
        } else if (parameter instanceof Object[]) {
            appendArrayParameter((Object[]) parameter);

        } else if (parameter instanceof IRScope) {
            appendIRScopeParameter((IRScope) parameter);

        } else if (parameter instanceof IRStaticScope) {
            appendStaticScopeParameter((IRStaticScope) parameter);
            
        } else if (parameter == null) {
            builder.append(parameter);
            
        } else if (parameter instanceof ISourcePosition) {
            appendISourcePossition((ISourcePosition) parameter);
            
        } else if (parameter instanceof Arity) {
            appendArity((Arity) parameter);
            
        } else if (parameter instanceof RegexpOptions) {
            appendRegexpOptions((RegexpOptions) parameter);
            
        } else {
            appendOtherParameter(parameter);

        }
    }

    private void appendEnum(Enum enumInstance) {
        String name = enumInstance.name();
        
        appendEscapedString(name);        
    }

    private void appendRegexpOptions(RegexpOptions options) {
        final KCode kCode = options.getKCode();
        final boolean kcodeDefault = options.isKcodeDefault();
        
        appendOtherParameter(kCode);
        builder.append(PARAMETER_SEPARATOR);
        appendVerbatim(kcodeDefault);
    }

    private void appendOperandParameter(final Operand operand) {
        IRToStringTranslator.continueTranslation(this, operand);
    }

    void appendEscapedString(final String string) {
        final String escapedStringValue = string.replaceAll(DOUBLE_QUOTES, ESCAPED_DOUBLE_QUOTES);
        
        builder.append(DOUBLE_QUOTES).append(escapedStringValue).append(DOUBLE_QUOTES);
    }
    
    void appendVerbatim(final Object value) {
        builder.append(value);
    }
    
    private void appendArrayParameter(final Object[] array) {
        builder.append(ARRAY_START_MARKER);
        
        for (int i = 0; i < array.length; i++) {
            if(i != 0) {
                builder.append(PARAMETER_SEPARATOR);
            }
            
            final Object parameter = array[i];
            appendParameter(parameter);
        }
        
        builder.append(ARRAY_END_MARKER);
    }

    private void appendIRScopeParameter(final IRScope scope) {
        final String disambiguatedScopeName = IRScopeNameExpert.INSTANCE.getDisambiguatedScopeName(scope);
        
        appendEscapedString(disambiguatedScopeName);
    }
    
    private void appendStaticScopeParameter(final IRStaticScope staticScope) {
        final IRStaticScopeType type = staticScope.getType();
        final String[] variables = staticScope.getVariables();
        final int requiredArgs = staticScope.getRequiredArgs();
        
        appendOtherParameter(type);
        builder.append(PARAMETER_SEPARATOR);
        appendArrayParameter(variables);
        builder.append(PARAMETER_SEPARATOR);
        appendVerbatim(requiredArgs);
    }

    private void appendISourcePossition(final ISourcePosition position) {
        final String file = position.getFile();
        final int line = position.getLine();
        
        appendEscapedString(file);
        appendVerbatim(line);
    }

    private void appendArity(Arity parameter) {
        int value = parameter.getValue();
        
        appendVerbatim(value);
    }

    private void appendOtherParameter(final Object parameter) {
        appendEscapedString(parameter.toString());
    }
    
    public String getResultString() {
        return builder.toString();
    }
}
