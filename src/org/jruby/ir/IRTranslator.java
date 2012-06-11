package org.jruby.ir;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ir.persistence.IRPersistenceException;
import org.jruby.ir.persistence.IRPersistenceFacade;

/**
 * Abstract class that contains general logic for both IR Compiler and IR Interpreter
 *
 * @param <R> type of returned object by translator
 * @param <S> type of specific for translator object
 */
public abstract class IRTranslator<R, S> {
    
    public R performTranslation(Ruby runtime, Node node, S specificObject) {        
        IRScope producedIRScope = produceIrScope(runtime, node);
        
        if(isIRPersistenceRequired()) {
            try {
                IRPersistenceFacade.persist(producedIRScope, runtime);
            } catch (IRPersistenceException e) {
                // FIXME: Log error, but do not interrupt translation 
                // Or should we throw runtime exception?
                e.printStackTrace();
            }
        }
        
        R result = translationSpecificLogic(runtime, producedIRScope, specificObject);
        return result;
    }
    
    protected abstract R translationSpecificLogic(Ruby runtime, IRScope producedIrScope, S specificObject);

    private static boolean isIRPersistenceRequired() {
        return RubyInstanceConfig.IR_PERSISTENCE;
    }

    private IRScope produceIrScope(Ruby runtime, Node node) {
        IRManager irManager = runtime.getIRManager();
        boolean is1_9 = runtime.is1_9();
        IRBuilder irBuilder = IRBuilder.createIRBuilder(irManager, is1_9);
        
        final IRScope irScope = irBuilder.buildRoot((RootNode) node);
        return irScope;
    }    

}
