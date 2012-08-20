package org.jruby.ir.persistence.read.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jcodings.Encoding;
import org.jruby.RubyLocalJumpError.Reason;
import org.jruby.ir.IRClassBody;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.instructions.SuperInstrType;
import org.jruby.ir.instructions.specialized.SpecializedInstType;
import org.jruby.ir.operands.BooleanLiteral;
import org.jruby.ir.operands.CompoundString;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.LocalVariable;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.MethodHandle;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.persistence.read.parser.factory.NonIRObjectFactory;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.parser.IRStaticScope;
import org.jruby.parser.IRStaticScopeFactory;
import org.jruby.parser.IRStaticScopeType;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Arity;
import org.jruby.runtime.CallType;
import org.jruby.util.KCode;
import org.jruby.util.RegexpOptions;

public class ParametersIterator {
    
    private final IRFileParsingContext context;
    private final Iterator<Object> parametersIterator;
    
    public ParametersIterator(final IRFileParsingContext context, final List<Object> parameters) {
        this.context = context;
        this.parametersIterator = parameters.iterator();
    }
    
    public ParametersIterator(final IRFileParsingContext context, final Object parameter) {
        final List<Object> listWithSingleParam = new ArrayList<Object>(1);
        listWithSingleParam.add(parameter);
        
        this.parametersIterator = listWithSingleParam.iterator();
        this.context = context;
    }
    
    public Object next() {
        return parametersIterator.next();
    }
    
    public boolean hasNext() {
        return parametersIterator.hasNext();
    }
    
  // Collections and arrays
    public List<Operand> nextOperandList() {
        @SuppressWarnings("unchecked")
        final List<Operand> operands = (List<Operand>) (List<?>) nextList();
        return operands;
    }
    
    public Operand[] nextOperandArray() {
        final List<Operand> argsList = nextOperandList();
        
        Operand[] args = null;
        if (argsList != null) {
            args = new Operand[argsList.size()];
            argsList.toArray(args);
        } else {
            args = Operand.EMPTY_ARRAY;
        }
        return args;
    }
    
    public List<Object> nextList() {
        @SuppressWarnings("unchecked")
        final List<Object> parameters = (List<Object>) parametersIterator.next();        
        return parameters;
    }
    
    private String[] nextObjectArray() {
        final List<Object> namesList = nextList();
        
        String[] names = new String[namesList.size()];
        namesList.toArray(names);
        return names;
    }
    
  // Java objects  
    public String nextString() {
        return (String) next();
    }
    
    public char nextChar() {
        // Chars are persisted as strings
        final String string = nextString();
        
        return string.charAt(0);
    }
    
    public Long nextLong() {
        final String valueString = nextString();
        
        return Long.valueOf(valueString);
    }
    
    public Double nextDouble() {
        final String valueString = nextString();
        
        return Double.valueOf(valueString);
    }
    
    public BigInteger nextBigInteger() {
        final String bignumString = nextString();
        
        return new BigInteger(bignumString);
    }

    public boolean nextBoolean() {
        final String booleanString = nextString();
        
        return Boolean.parseBoolean(booleanString);
    }
    
    public int nextInt() {
        final String integerString = nextString();
        
        return Integer.parseInt(integerString); 
    }
    
  // JRuby objects
    public ISourcePosition nextISourcePossition() {
        final String fileName = nextString();
        final int line = nextInt();
        
        final ISourcePosition possition = NonIRObjectFactory.INSTANCE.createSourcePosition(fileName, line);
        
        return possition;
    }

    public Arity nextArity() {
        int value = nextInt();
        
        return Arity.createArity(value);
    }

    public RegexpOptions nextRegexpOptions() {
        final String kcodeName = nextString();
        final boolean isKCodeDefault = nextBoolean();
        final KCode kcode = NonIRObjectFactory.INSTANCE.createKcode(kcodeName);
        
        return new RegexpOptions(kcode, isKCodeDefault);
    }
    
    public IRStaticScope nextStaticScope(IRScope lexicalParent) {
        final String typeString = nextString();
        final IRStaticScopeType type = NonIRObjectFactory.INSTANCE.createStaticScopeType(typeString);
        
        final String[] names = nextObjectArray();    
        final int requiredArgs = nextInt();
        
        StaticScope parent = null;
        if(lexicalParent != null) {
            parent = lexicalParent.getStaticScope();
        }
        
        final IRStaticScope staticScope = IRStaticScopeFactory.newStaticScope(parent, type, names);
        // requiredArg are needed to be set when interpretation runs
        // some code relies on value of requiredArg (e.g. Arity check)
        staticScope.setRequiredArgs(requiredArgs);        
        
        return staticScope;
    }

  // Enums
    public Encoding nextEncoding() {
        final String encodingName = nextString();
        
        return NonIRObjectFactory.INSTANCE.createEncoding(encodingName);
    }
    
    public Reason nextReason() {
        final String reasonString = nextString();
        
        return NonIRObjectFactory.INSTANCE.createReason(reasonString); 
    }
    
    public SpecializedInstType nextSpecializedInstType() {
        final String specializedInstName = nextString();
        
        return NonIRObjectFactory.INSTANCE.createSpecilizedInstrType(specializedInstName);
    }

    public CallType nextCallType() {
        final String callTypeString = nextString();
        
        return NonIRObjectFactory.INSTANCE.createCallType(callTypeString);
    }

    public SuperInstrType nextSuperInstrType() {
        final String superInstrTypeString = nextString();
        
        return NonIRObjectFactory.INSTANCE.createSuperInstrType(superInstrTypeString);
    }
    
 // IRScopes
    public IRScope nextScope() {
        final String scopeName = nextString(); 
        
        // Check if it is reference to special IRScope
        if(scopeName == null) { 
            // Scope sometimes may be null for some instructions, that's ok
            return null;
        } else if(context.isContainsScope(scopeName)) {
            return context.getScopeByName(scopeName);
        } else if(scopeName.startsWith(IRClassBody.OBJECT_CLASS_NAME)) { // It's reference to object
            // We need to look into context first
            // because there is a possibility that class 'Object' was defined in file 
            return context.getIRManager().getObject();
        } else {
            throw new RuntimeException("Scope '" + scopeName + "' was not found");
        }
    }
    
    public IRClosure nextIRClosure() {
        return (IRClosure) nextScope();
    }

    public IRMethod nextIRMethod() {
        return (IRMethod) nextScope();
    }

    public IRClassBody nextIRClassBody() {
        return (IRClassBody) nextScope();
    }

    public IRModuleBody nextIRModuleBody() {
        return (IRModuleBody) nextScope();
    }
    
  // Operands
    public Operand nextOperand() {
        return (Operand) next();
    }

    public CompoundString nextCompoundString() {
        return (CompoundString) next();
    }

    public Label nextLabel() {
        return (Label) next();
    }

    public LocalVariable nextLocalVariable() {
        return (LocalVariable) next();
    }

    public Variable nextVariable() {
        return (Variable) next();
    }

    public MethAddr nextMethAddr() {
        return (MethAddr) next();
    }
    
    public StringLiteral nextStringLiteral() {
        return (StringLiteral) next();
    }

    public MethodHandle nextMethodHandle() {
        return (MethodHandle) next();
    }

    public BooleanLiteral nextBooleanLiteral() {
        return (BooleanLiteral) next();
    }

}
