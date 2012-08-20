package org.jruby.ir.persistence.persist.string;

import org.jruby.ir.IRClosure;
import org.jruby.ir.IREvalScript;
import org.jruby.ir.IRMetaClassBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRScopeType;
import org.jruby.ir.IRScriptBody;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.persistence.persist.string.builder.IRScopeStringBuilder;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Arity;

class IRScopeStringExtractor {
    
    private final IRScopeStringBuilder stringProducer;
    
    private IRScopeStringExtractor(final IRScopeStringBuilder stringProducer) {
        this.stringProducer = stringProducer;
    }
    
    // Static factory that is used in translator
    static IRScopeStringExtractor createToplevelInstance() {
        final IRScopeStringBuilder stringProducer = new IRScopeStringBuilder(null);
        
        return new IRScopeStringExtractor(stringProducer);
    }

    String extract(final IRScope irScope) {
        addScopeInfosRecursivelly(irScope);
        
        stringProducer.finishLine(); // there is a empty line between scope infos and instructions
        
        addInstructionsRecursively(irScope);
        
        return stringProducer.getResultString();
    }

    private void addScopeInfosRecursivelly(final IRScope irScope) {
        appendScopeInfo(irScope);
        
        for (IRScope innerScope : irScope.getLexicalScopes()) {
            addScopeInfosRecursivelly(innerScope);
        }
    }
    
    private void appendScopeInfo(final IRScope irScope) {
        stringProducer.appendScopeType(irScope);

        appendScopeSpecificParameters(irScope);
        
        stringProducer.finishLine();
    }

    private void appendScopeSpecificParameters(final IRScope irScope) {
        final IRScopeType scopeType = irScope.getScopeType();
        
        switch (scopeType) {
        case CLASS_BODY:
        case CLASS_METHOD:
        case INSTANCE_METHOD:
        case MODULE_BODY:
            appendParametersForStandartScopes(irScope);
            break;

        case METACLASS_BODY:
            appendParametersForMetaclassBody((IRMetaClassBody) irScope);
            break;

        case SCRIPT_BODY:
            appendParametersForScriptBody((IRScriptBody) irScope);
            break;

        case CLOSURE:
            appendParametersForClosure((IRClosure) irScope);
            break;

        case EVAL_SCRIPT:
            appendParametersForEvalScript((IREvalScript) irScope);
            break;

        default:
            throw new UnsupportedOperationException(scopeType.toString());
        }
    }

    private void appendParametersForStandartScopes(final IRScope irScope) {
        final String name = irScope.getName();
        final int lineNumber = irScope.getLineNumber();
        final IRScope lexicalParent = irScope.getLexicalParent();
        final StaticScope staticScope = irScope.getStaticScope();

        stringProducer.appendParameters(name, lineNumber, lexicalParent, staticScope);
    }

    private void appendParametersForMetaclassBody(final IRMetaClassBody irScope) {
        final int lineNumber = irScope.getLineNumber();
        final IRScope lexicalParent = irScope.getLexicalParent();
        final StaticScope staticScope = irScope.getStaticScope();

        stringProducer.appendParameters(lineNumber, lexicalParent, staticScope);
    }

    private void appendParametersForScriptBody(final IRScriptBody irScope) {
        final String name = irScope.getName();
        final StaticScope staticScope = irScope.getStaticScope();

        stringProducer.appendParameters(name, staticScope);
    }

    private void appendParametersForClosure(final IRClosure irClosure) {
        final int lineNumber = irClosure.getLineNumber();
        final IRScope lexicalParent = irClosure.getLexicalParent();
        final StaticScope staticScope = irClosure.getStaticScope();
        final boolean forLoopBody = irClosure.isForLoopBody();
        final Arity arity = irClosure.getArity();
        final int argumentType = irClosure.getArgumentType();

        stringProducer.appendParameters(lineNumber, lexicalParent, staticScope, forLoopBody, arity,
                argumentType);
    }

    private void appendParametersForEvalScript(final IREvalScript irScope) {
        final String fileName = irScope.getFileName();
        final int lineNumber = irScope.getLineNumber();
        final IRScope lexicalParent = irScope.getLexicalParent();
        final StaticScope staticScope = irScope.getStaticScope();

        stringProducer.appendParameters(fileName, lineNumber, lexicalParent, staticScope);

    }
    
    private void addInstructionsRecursively(final IRScope irScope) {
        appendScopeInstructionsBlock(irScope);
        stringProducer.finishLine();
        
        for (IRScope innerScope : irScope.getLexicalScopes()) {
            addInstructionsRecursively(innerScope);
        }
    }
    
    // All instruction blocks are looks like:
    // "${scopeName}"
    //  ${instr1}
    //  ${instr2}
    //  ...
    //  ${instrn}
    //
    private void appendScopeInstructionsBlock(final IRScope irScope) {
        stringProducer.appendScopeName(irScope);        
        stringProducer.finishLine();
        
        for (Instr instr : irScope.getInstrs()) {
            IRToStringTranslator.continueTranslation(stringProducer, instr);
            stringProducer.finishLine();
        }
    }

}
