package org.jruby.ir.persistence.read.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jruby.ir.IRScope;
import org.jruby.ir.IRScopeType;
import org.jruby.ir.Operation;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.persistence.read.parser.factory.IRInstructionFactory;
import org.jruby.ir.persistence.read.parser.factory.IROperandFactory;
import org.jruby.ir.persistence.read.parser.factory.IRScopeFactory;
import org.jruby.ir.persistence.read.parser.factory.NonIRObjectFactory;

import beaver.Symbol;


public class PersistedIRParserLogic {
    
    private final IRScopeFactory scopeBuilder;
    private final IRInstructionFactory instrFactory;
    private final IROperandFactory operandFactory;
    
    private static final NonIRObjectFactory NON_IR_OBJECT_FACTORY = NonIRObjectFactory.INSTANCE;
    
    private final IRParsingContext context;

    PersistedIRParserLogic(final IRParsingContext context) {
        this.context = context;
        
        scopeBuilder = new IRScopeFactory(context);
        instrFactory = new IRInstructionFactory(context);
        operandFactory = new IROperandFactory(context);
    }
    
    Symbol getToplevelScope() {
        final IRScope scope = context.getToplevelScope();
        
        return new Symbol(scope);
    }
    
    Symbol createScope(final String typeString, final List<Object> parameters) {
        final IRScopeType type = NON_IR_OBJECT_FACTORY.createScopeType(typeString);
        final IRScope irScope = scopeBuilder.createScope(type, parameters);
        
        context.addToScopes(irScope);
        
        return new Symbol(irScope);
    }
    
    Symbol addToScope(final IRScope scope, final List<Instr> instrs) {
        // we need to iterate throw scopes
        // because IRScope#addInst has obvious side effect and for that reason
        // so we can't just use IRScope#getInsts and assign it to instrs 
        for (Instr instr : instrs) {
            scope.addInstr(instr);
        }
        
        return new Symbol(scope);
    }
    
    Symbol enterScope(final String name) {
        final IRScope scope = context.getScopeByName(name);
        context.setCurrentScope(scope);
        
        return new Symbol(scope);
    }
    
    Symbol addFirstInstruction(final Instr i) {
        final List<Object> lst = new ArrayList<Object>();
        lst.add(i);
        
        return new Symbol(lst);
    }
    
    Symbol addFollowingInstructions(final List<Instr> lst, final Instr i, final Symbol _symbol_lst) {
        lst.add(i);
        
        return _symbol_lst;
    }
    
    Symbol markAsDeadIfNeeded(final Symbol instrSymbol, final Symbol marker) {
        if(marker.value != null) {
            final Instr currentInstr =  (Instr) instrSymbol.value;
            currentInstr.markDead();
        }
        
        return instrSymbol;
    }
    
    Symbol createInstrWithoutParams(final String operationName) {
        final Instr instr = instrFactory.createInstrWithoutParams(operationName);
        
        return new Symbol(instr);
    }
    
    Symbol createInstrWithParams(final String id, final List<Object> parameters) {
        final Operation operation = NON_IR_OBJECT_FACTORY.createOperation(id);
        final ParametersIterator parametersIterator = new ParametersIterator(context, parameters);
        
        final Instr instr = instrFactory.createInstrWithParams(operation, parametersIterator);
        
        return new Symbol(instr);
    }
    
    Symbol markHasUnusedResultIfNeeded(final Symbol instrSymbol, final Symbol marker) {
        if(marker.value != null) {
            final Instr currentInstr =  (Instr) instrSymbol.value;
            currentInstr.markUnusedResult();
        }
        
        return instrSymbol;
    }
    
    Symbol createReturnInstrWithNoParams(final Operand result, final String operationName) {
        final Instr instr = instrFactory.createReturnInstrWithNoParams((Variable) result, operationName);
        
        return new Symbol(instr);
    }
    
    Symbol createReturnInstrWithParams(final Operand result, final String id, final List<Object> parameters) {
        final Operation operation = NON_IR_OBJECT_FACTORY.createOperation(id);
        final ParametersIterator parametersIterator = new ParametersIterator(context, parameters);
        
        final Instr instr = instrFactory.createReturnInstrWithParams((Variable) result, operation, parametersIterator);
        
        return new Symbol(instr);
    }
    
    Symbol createNull() {
        return new Symbol(null);
    }
    
    Symbol createList(List<Object> params) {        
        if(params == null) {
            params = Collections.emptyList();
        }
        
        return new Symbol(params);
    }
    
    Symbol createOperandWithoutParameters(final String operandName) {
        final Operand operand = operandFactory.createOperandWithoutParameters(operandName);
        
        return new Symbol(operand);
    }
    
    Symbol createOperandWithParameters(final String operandName, final List<Object> params) {
        final Operand operand = operandFactory.createOperandWithParameters(operandName, params);
        
        return new Symbol(operand);
    }
}
