package org.jruby.ir.persistence.persist.string;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jcodings.Encoding;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.operands.Array;
import org.jruby.ir.operands.AsString;
import org.jruby.ir.operands.Backref;
import org.jruby.ir.operands.BacktickString;
import org.jruby.ir.operands.Bignum;
import org.jruby.ir.operands.BooleanLiteral;
import org.jruby.ir.operands.ClosureLocalVariable;
import org.jruby.ir.operands.CompoundArray;
import org.jruby.ir.operands.CompoundString;
import org.jruby.ir.operands.CurrentScope;
import org.jruby.ir.operands.DynamicSymbol;
import org.jruby.ir.operands.Fixnum;
import org.jruby.ir.operands.GlobalVariable;
import org.jruby.ir.operands.Hash;
import org.jruby.ir.operands.IRException;
import org.jruby.ir.operands.KeyValuePair;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.LocalVariable;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.MethodHandle;
import org.jruby.ir.operands.Nil;
import org.jruby.ir.operands.NthRef;
import org.jruby.ir.operands.ObjectClass;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Range;
import org.jruby.ir.operands.Regexp;
import org.jruby.ir.operands.SValue;
import org.jruby.ir.operands.ScopeModule;
import org.jruby.ir.operands.Self;
import org.jruby.ir.operands.Splat;
import org.jruby.ir.operands.StandardError;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Symbol;
import org.jruby.ir.operands.TemporaryClosureVariable;
import org.jruby.ir.operands.TemporaryVariable;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.ir.operands.UnexecutableNil;
import org.jruby.ir.operands.WrappedIRClosure;
import org.jruby.ir.persistence.persist.string.builder.AbstractIRStringBuilder;
import org.jruby.ir.persistence.persist.string.builder.IROperandStringBuilder;
import org.jruby.util.RegexpOptions;

class IROperandStringExtractor extends IRVisitor {
    
    private final IROperandStringBuilder stringProducer;
    
    private IROperandStringExtractor(IROperandStringBuilder stringProducer) {
        this.stringProducer = stringProducer;
    }
    
    // Static factory that is used in translator
    static IROperandStringExtractor createToplevelInstance() {
        IROperandStringBuilder stringProducer = new IROperandStringBuilder(null);
        return new IROperandStringExtractor(stringProducer);
    }
    static IROperandStringExtractor createInstance(AbstractIRStringBuilder builder) {
        IROperandStringBuilder stringProducer = new IROperandStringBuilder(builder);
        return new IROperandStringExtractor(stringProducer);
    }
    
    public String extract(final Operand operand) {
        produceString(operand);
        
        return stringProducer.getResultString();
    }
    
    public void produceString(final Operand operand) {
        stringProducer.appendOperandType(operand);

        operand.visit(this);
    }
    
 // Operands

    // Operands without parameters
    public void Nil(final Nil nil) {}
    public void ObjectClass(final ObjectClass objectclass) {}
    public void Self(final Self self) {}
    public void StandardError(final StandardError standarderror) {}
    public void UndefinedValue(final UndefinedValue undefinedvalue) {}
    public void UnexecutableNil(final UnexecutableNil unexecutablenil) {}
    
    // Operands that have arrays as parameters
    
    // If we simply pass array directly to appendParameters
    //  than it will be unwrapped
    //  we want to pass single parameters which type is Operand[]
    public void Array(final Array array) {
        final Operand[] elts = array.getElts();        
        
        stringProducer.appendParameters(new Object[] { elts });
    }
    
    public void BacktickString(final BacktickString backtickstring) {
        final List<Operand> pieces = backtickstring.pieces;
        
        stringProducer.appendParameters(new Object[] { pieces.toArray() });
    }

    public void CompoundString(final CompoundString compoundstring) {
        final List<Operand> pieces = compoundstring.getPieces();
        final Encoding encoding = compoundstring.getEncoding();
        
        // No need to wrap pieces array here,
        // 2 parameters are passed,
        // so appendParameters is able to figure out that there are 2 parameters
        stringProducer.appendParameters(pieces.toArray(), encoding);
    }

    public void Hash(final Hash hash) {
        List<Operand[]> keyValuePairArrays = Collections.emptyList();
        if (!hash.isBlank()) {
            final List<KeyValuePair> pairs = hash.pairs;
            keyValuePairArrays = new ArrayList<Operand[]>(pairs.size());
            for (KeyValuePair keyValuePair : pairs) {
                final Operand[] keyValuePairArray = { keyValuePair.getKey(), keyValuePair.getValue() };
                keyValuePairArrays.add(keyValuePairArray);
            }
        }        
        stringProducer.appendParameters(new Object[] { keyValuePairArrays.toArray() });
    }

    // Operands that takes another operands as parameters    
    
    public void AsString(final AsString asstring) {
        final Operand source = asstring.getSource();
        
        stringProducer.appendParameters(source);
    }
    
    public void CompoundArray(final CompoundArray compoundarray) {
        final Operand a1 = compoundarray.getA1();
        final Operand a2 = compoundarray.getA2();
        final boolean argsPush = compoundarray.isArgsPush();
        
        stringProducer.appendParameters(a1, a2, argsPush);
    }

    public void DynamicSymbol(final DynamicSymbol dynamicsymbol) {
        final CompoundString symbolName = dynamicsymbol.getSymbolName();
        
        stringProducer.appendParameters(symbolName);
    }

    public void MethodHandle(final MethodHandle methodhandle) {
        final Operand receiver = methodhandle.getReceiver();
        final Operand methodNameOperand = methodhandle.getMethodNameOperand();
        
        stringProducer.appendParameters(receiver, methodNameOperand);
    }

    public void Range(final Range range) {
        final Operand begin = range.getBegin();
        final Operand end = range.getEnd();
        final boolean exclusive = range.isExclusive();
        
        stringProducer.appendParameters(begin, end, exclusive);
    }

    public void Regexp(final Regexp regexp) {
        final Operand regexpOperand = regexp.getRegexp();
        final RegexpOptions options = regexp.options;        
        
        stringProducer.appendParameters(regexpOperand, options);
    }

    public void Splat(final Splat splat) {
        final Operand array = splat.getArray();
        
        stringProducer.appendParameters(array);
    }

    public void SValue(final SValue svalue) {
        final Operand array = svalue.getArray();
        
        stringProducer.appendParameters(array);
    }
    
    // Operands that takes IRScope as parameter
    //  actually, all we need to persist is name of scope, by IRPersisterHelper will deal with this
    public void CurrentScope(final CurrentScope currentscope) {
        final IRScope scope = currentscope.getScope();
        
        stringProducer.appendParameters(scope);
    }

    public void ScopeModule(final ScopeModule scopemodule) {
        final IRScope scope = scopemodule.getScope();
        
        stringProducer.appendParameters(scope);
    }

    public void WrappedIRClosure(final WrappedIRClosure wrappedirclosure) {
        final IRClosure closure = wrappedirclosure.getClosure();
        
        stringProducer.appendParameters(closure);
    }
    
    // Parameters that takes string(or char) as parameters
    public void Backref(final Backref backref) {
        final char type = backref.type;
        
        stringProducer.appendParameters(type);
    }

    public void StringLiteral(final StringLiteral stringliteral) {
        final String string = stringliteral.string;
        
        stringProducer.appendParameters(string);
    }

    public void Symbol(final Symbol symbol) {
        final String name = symbol.getName();
        
        stringProducer.appendParameters(name);
    }

    public void GlobalVariable(final GlobalVariable globalvariable) {        
        final String name = globalvariable.getName();
        
        stringProducer.appendParameters(name);
    }

    public void IRException(final IRException irexception) {
        String type = null;
        
        if (irexception == IRException.NEXT_LocalJumpError) type = "NEXT";
        else if (irexception == IRException.BREAK_LocalJumpError) type = "BREAK";
        else if (irexception == IRException.RETURN_LocalJumpError) type = "RETURN";
        else if (irexception == IRException.REDO_LocalJumpError) type = "REDO";
        else if (irexception == IRException.RETRY_LocalJumpError) type = "RETRY";
        else {
            throw new UnsupportedOperationException(irexception.toString());
        }
        
        stringProducer.appendParameters(type);
    }

    public void Label(final Label label) {
        final String labelValue = label.label;
        
        stringProducer.appendParameters(labelValue);
    }

    public void MethAddr(final MethAddr methaddr) {
        final String name = methaddr.getName();
        
        stringProducer.appendParameters(name);
    }

    // Operands that takes java objects from standard library(or primitive types) as parameters
    //  exception for string types 
    public void Bignum(final Bignum bignum) {
        final BigInteger value = bignum.value;
        
        stringProducer.appendParameters(value);
    }

    public void BooleanLiteral(final BooleanLiteral booleanliteral) {
        final boolean bool = booleanliteral.isTrue();
        
        stringProducer.appendParameters(bool);
    }

    public void ClosureLocalVariable(final ClosureLocalVariable closurelocalvariable) {
        commonForLocalVariables(closurelocalvariable);
    }

    public void LocalVariable(final LocalVariable localvariable) {
        commonForLocalVariables(localvariable);
    }
    
    private void commonForLocalVariables(final LocalVariable localVariable) {
        final String name = localVariable.getName();
        final int scopeDepth = localVariable.getScopeDepth();
        
        stringProducer.appendParameters(name, scopeDepth);
    }

    public void Fixnum(final Fixnum fixnum) {
        final Long value = fixnum.value;
        
        stringProducer.appendParameters(value);
    }

    public void Float(final org.jruby.ir.operands.Float flote) {
        final Double value = flote.value;
        
        stringProducer.appendParameters(value);
    }   

    public void NthRef(final NthRef nthref) {
        final int matchNumber = nthref.matchNumber;
        
        stringProducer.appendParameters(matchNumber);
    }

    public void TemporaryVariable(final TemporaryVariable temporaryvariable) {
        commonForTemproraryVariable(temporaryvariable);
    }

    public void TemporaryClosureVariable(final TemporaryClosureVariable temporaryclosurevariable) {
        commonForTemproraryVariable(temporaryclosurevariable);
    }

    private void commonForTemproraryVariable(final TemporaryVariable temporaryVariable) {
        final String name = temporaryVariable.getName();
        
        stringProducer.appendParameters(name);
    }

}
