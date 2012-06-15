package org.jruby.ir;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ir.persistence.IRPersistenceException;
import org.jruby.ir.persistence.IRPersistenceFacade;

/**
 * Abstract class that contains general logic for both IR Compiler and IR
 * Interpreter
 * 
 * @param <R>
 *            type of returned object by translator
 * @param <S>
 *            type of specific for translator object
 */
public abstract class IRTranslator<R, S> {

    public R performTranslation(Ruby runtime, Node node, S specificObject) {

        IRScope producedIRScope = null;
        if (isIRPersistenceRequired()) {
            producedIRScope = produceIrScope(runtime, node, true);
            try {
                IRPersistenceFacade.persist(producedIRScope, runtime);
            } catch (IRPersistenceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        } else {
            producedIRScope = produceIrScope(runtime, node, false);
        }

        R result = translationSpecificLogic(runtime, producedIRScope, specificObject);
        return result;
    }

    protected abstract R translationSpecificLogic(Ruby runtime, IRScope producedIrScope,
            S specificObject);

    private static boolean isIRPersistenceRequired() {
        return RubyInstanceConfig.IR_PERSISTENCE;
    }

    private IRScope produceIrScope(Ruby runtime, Node node, boolean isDryRun) {
        IRManager irManager = runtime.getIRManager();
        boolean is1_9 = runtime.is1_9();
        irManager.setDryRun(isDryRun);
        IRBuilder irBuilder = IRBuilder.createIRBuilder(irManager, is1_9);

        final IRScope irScope = irBuilder.buildRoot((RootNode) node);
        return irScope;
    }

}
