package org.jruby.ir.persistence.read.parser.factory;

import java.util.List;

import org.jruby.ir.IRClassBody;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IREvalScript;
import org.jruby.ir.IRManager;
import org.jruby.ir.IRMetaClassBody;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRScopeType;
import org.jruby.ir.IRScriptBody;
import org.jruby.ir.persistence.read.parser.IRParsingContext;
import org.jruby.ir.persistence.read.parser.ParametersIterator;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Arity;

public class IRScopeFactory {

    private static final String SCRIPT_BODY_PSEUDO_CLASS_NAME = "_file_";
    
    private final IRParsingContext context;
    
    public IRScopeFactory(IRParsingContext context) {
        this.context = context;
    }

    public IRScope createScope(final IRScopeType type, final List<Object> parameters) {
        final IRManager manager = context.getIRManager();        
        final ParametersIterator parametersIterator = new ParametersIterator(context, parameters); 
        
        switch (type) {
        case CLASS_BODY:
        case INSTANCE_METHOD:
        case CLASS_METHOD:
        case MODULE_BODY:
        case EVAL_SCRIPT:
            return createStandardScope(type, manager, parametersIterator);
            
        case METACLASS_BODY:            
            return createMetaclassBody(manager, parametersIterator);
            
        case SCRIPT_BODY:
            return createScriptBody(manager, parametersIterator);            

        case CLOSURE:
            return createClosure(manager, parametersIterator);
            
        default:
            throw new UnsupportedOperationException(type.toString());
        }
    }

    private IRScope createStandardScope(final IRScopeType type, final IRManager manager,
            final ParametersIterator parametersIterator) {
        final String name = parametersIterator.nextString();
        final int lineNumber = parametersIterator.nextInt();
        final IRScope lexicalParent = parametersIterator.nextScope();
        final StaticScope staticScope = parametersIterator.nextStaticScope(lexicalParent);
        
        switch (type) {
        case CLASS_BODY:            
            return new IRClassBody(manager, lexicalParent, name, lineNumber, staticScope);
            
        case INSTANCE_METHOD:
            return new IRMethod(manager, lexicalParent, name, true, lineNumber, staticScope);
            
        case CLASS_METHOD:
            return new IRMethod(manager, lexicalParent, name, false, lineNumber, staticScope);
            
        case MODULE_BODY:
            return new IRModuleBody(manager, lexicalParent, name, lineNumber, staticScope);
            
        case EVAL_SCRIPT:
            // In case of eval script, name is a file name
            return new IREvalScript(manager, lexicalParent, name, lineNumber, staticScope);
            
        default:
            throw new UnsupportedOperationException(type.toString());
        }
        
    }

    private IRMetaClassBody createMetaclassBody(final IRManager manager, final ParametersIterator parametersIterator) {
        final int lineNumber = parametersIterator.nextInt();
        final IRScope lexicalParent = parametersIterator.nextScope();
        final StaticScope staticScope = parametersIterator.nextStaticScope(lexicalParent);
        
        // metaClassName must be in special format in order to be recognized by interpreter
        // we may persist that name, but it may be generated easily
        final String metaClassName = manager.getMetaClassName();
        
        return new IRMetaClassBody(manager, lexicalParent, metaClassName, lineNumber, staticScope);
    }

    private IRScriptBody createScriptBody(final IRManager manager, final ParametersIterator parametersIterator) {
        final String name = parametersIterator.nextString();
        final StaticScope staticScope = parametersIterator.nextStaticScope(null); // no parent for script body
        
        return new IRScriptBody(manager, SCRIPT_BODY_PSEUDO_CLASS_NAME, name, staticScope);
    }
    
    private IRScope createClosure(final IRManager manager, final ParametersIterator parametersIterator) {
        final int lineNumber = parametersIterator.nextInt();
        final IRScope lexicalParent = parametersIterator.nextScope();
        final StaticScope staticScope = parametersIterator.nextStaticScope(lexicalParent);
        final boolean isForLoopBody = parametersIterator.nextBoolean();
        final Arity arity = parametersIterator.nextArity();
        final int argumentType = parametersIterator.nextInt();
        // TODO: Persist ruby version with header of .ir file 
        final boolean is1_9 = context.getRuntime().is1_9();        
        
        return new IRClosure(manager, lexicalParent, isForLoopBody, lineNumber, staticScope, arity, argumentType, is1_9);
    }
}
