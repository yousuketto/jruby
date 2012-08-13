package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.TypeId;
import java.io.FileOutputStream;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.internal.runtime.methods.CompiledIRMethod;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.ir.IRClassBody;
import org.jruby.ir.IRMetaClassBody;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRScriptBody;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Tuple;
import org.jruby.ir.instructions.*;
import org.jruby.ir.instructions.defined.BackrefIsMatchDataInstr;
import org.jruby.ir.instructions.defined.ClassVarIsDefinedInstr;
import org.jruby.ir.instructions.defined.GetBackrefInstr;
import org.jruby.ir.instructions.defined.GetDefinedConstantOrMethodInstr;
import org.jruby.ir.instructions.defined.GetErrorInfoInstr;
import org.jruby.ir.instructions.defined.GlobalIsDefinedInstr;
import org.jruby.ir.instructions.defined.HasInstanceVarInstr;
import org.jruby.ir.instructions.defined.IsMethodBoundInstr;
import org.jruby.ir.instructions.defined.MethodDefinedInstr;
import org.jruby.ir.instructions.defined.MethodIsPublicInstr;
import org.jruby.ir.instructions.defined.RestoreErrorInfoInstr;
import org.jruby.ir.instructions.defined.SuperMethodBoundInstr;
import org.jruby.ir.instructions.ruby18.ReceiveOptArgInstr18;
import org.jruby.ir.instructions.ruby18.ReceiveRestArgInstr18;
import org.jruby.ir.instructions.ruby19.BuildLambdaInstr;
import org.jruby.ir.instructions.ruby19.GetEncodingInstr;
import org.jruby.ir.instructions.ruby19.ReceiveOptArgInstr19;
import org.jruby.ir.instructions.ruby19.ReceivePostReqdArgInstr;
import org.jruby.ir.instructions.ruby19.ReceiveRestArgInstr19;
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
import org.jruby.ir.operands.Variable;
import org.jruby.ir.operands.WrappedIRClosure;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.InstanceVariables;

/**
 * Implementation of IRCompiler for the Dalvik VM.
 * 
 * @author lynnewallace
 */
public class DalvikVisitor extends IRVisitor {
    
    public static final String DYNAMIC_SCOPE = "$dynamicScope";
    
    public DalvikVisitor() {
        this.dalvik = new Dalvik();
    }
    
    public static void compile(IRScope scope) {
        // run compiler
        DalvikVisitor target = new DalvikVisitor();
        target.codegen(scope);
        
        // write out
        try {
            FileOutputStream out = new FileOutputStream("jruby.dex");
            out.write(target.code());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public byte[] code() {
        return dalvik.code();
    }

    public void codegen(IRScope scope) {
        if (scope instanceof IRScriptBody) {
            codegen((IRScriptBody)scope);
        }
    }

    public void codegen(IRScriptBody script) {
        this.script = script;
        emit(script);
    }
    
    public String emitScope(IRScope scope, String name, int arity) {
        name = name + scope.getLineNumber();
        dalvik.pushmethod(name, arity);

        Tuple<Instr[], ?> t = scope.prepareForCompilation();
        Instr[] instrs = t.a;

        for (int i = 0; i < instrs.length; i++) {
            visit(instrs[i]);
        }

        dalvik.popmethod();

        return name;
    }
    
    public void emit(IRScriptBody script) {
        String clsName = dalvik.scriptToClass(script.getName());
        dalvik.pushscript(clsName, script.getFileName());

        emitScope(script, "__script__", 0);

        dalvik.popclass();
    }
    
    public void emit(IRMethod method) {
        String name = emitScope(method, method.getName(), method.getCallArgs().length);
        
        // push a method handle for binding purposes
        dalvik.method().pushHandle(dalvik.clsData().clsName, name, method.getStaticScope().getRequiredArgs());
    }

    public void emit(IRModuleBody method) {
        String name = method.getName();
        if (name.indexOf("DUMMY_MC") != -1) {
            name = "METACLASS";
        }

        name = emitScope(method, name, 0);
        // push a method handle for binding purposes
        dalvik.method().pushHandle(dalvik.clsData().clsName, name, method.getStaticScope().getRequiredArgs());
    }
    
    public void visit(Instr instr) {
        instr.visit(this);
    }
    
    public void visit(Operand operand) {
        if (operand.hasKnownValue()) {
            operand.visit(this);
        } else if (operand instanceof Variable) {
            emitVariable((Variable)operand);
        } else {
            operand.visit(this);
        }
    }
    
    public void emitVariable(Variable variable) {
        int index = dalvik.methodData().local(variable);
        dalvik.method().loadLocal(index);
    }
    
    @Override
    public void AliasInstr(AliasInstr aliasInstr) {
        dalvik.method().loadLocal(0);
        dalvik.method().loadLocal(dalvik.methodData().local(aliasInstr.getReceiver()));
        dalvik.method().adapter.ldc(((StringLiteral) aliasInstr.getNewName()).string);
        dalvik.method().adapter.ldc(((StringLiteral) aliasInstr.getOldName()).string);
        dalvik.method().invokeHelper(TypeId.get(IRubyObject.class), "defineAlias", TypeId.get(ThreadContext.class), 
                TypeId.get(IRubyObject.class), TypeId.OBJECT, TypeId.OBJECT);
        dalvik.method().adapter.pop();
    }

    @Override
    public void AttrAssignInstr(AttrAssignInstr attrAssignInstr) {
        dalvik.method().loadLocal(0);
        visit(attrAssignInstr.getReceiver());
        for (Operand operand : attrAssignInstr.getCallArgs()) {
            visit(operand);
        }

        dalvik.method().invokeOther(attrAssignInstr.getMethodAddr().getName(), attrAssignInstr.getCallArgs().length);
        dalvik.method().adapter.pop();
    }

    @Override
    public void BEQInstr(BEQInstr beqInstr) {
        Operand[] args = beqInstr.getOperands();
        dalvik.method().loadLocal(0);
        visit(args[0]);
        visit(args[1]);
        dalvik.method().invokeHelper(TypeId.BOOLEAN, "BEQ", TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class));
        dalvik.method().adapter.iftrue(dalvik.methodData().getLabel(beqInstr.getJumpTarget()));
    }
    
    @Override
    public void BFalseInstr(BFalseInstr bFalseInstr) {
        visit(bFalseInstr.getArg1());
        dalvik.method().isTrue();
        dalvik.method().bfalse(dalvik.methodData().getLabel(bFalseInstr.getJumpTarget()));
    }
    
    @Override
    public void BlockGivenInstr(BlockGivenInstr blockGivenInstr) {
        super.BlockGivenInstr(blockGivenInstr);
    }
    
    @Override
    public void BNEInstr(BNEInstr bneinstr) {
        Operand[] args = bneinstr.getOperands();
        dalvik.method().loadLocal(0);
        visit(args[0]);
        visit(args[1]);
        dalvik.method().invokeHelper(TypeId.BOOLEAN, "BNE", TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), TypeId.get(IRubyObject.class));
        dalvik.method().adapter.iftrue(dalvik.methodData().getLabel(bneinstr.getJumpTarget()));
    }
    
    @Override
    public void BNilInstr(BNilInstr bnilinstr) {
        visit(bnilinstr.getArg1());
        dalvik.method().isNil();
        dalvik.method().btrue(dalvik.methodData().getLabel(bnilinstr.getJumpTarget()));
    }
    
    @Override
    public void BreakInstr(BreakInstr breakinstr) {
        super.BreakInstr(breakinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void BTrueInstr(BTrueInstr btrueinstr) {
        visit(btrueinstr.getArg1());
        dalvik.method().isTrue();
        dalvik.method().btrue(dalvik.methodData().getLabel(btrueinstr.getJumpTarget()));
    }
    
    @Override
    public void BUndefInstr(BUndefInstr bundefinstr) {
        super.BUndefInstr(bundefinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void CallInstr(CallInstr callinstr) {
        dalvik.method().loadLocal(0);
        visit(callinstr.getReceiver());
        for (Operand operand : callinstr.getCallArgs()) {
            visit(operand);
        }

        switch (callinstr.getCallType()) {
            case FUNCTIONAL:
            case VARIABLE:
                dalvik.method().invokeSelf(callinstr.getMethodAddr().getName(), callinstr.getCallArgs().length);
                break;
            case NORMAL:
                dalvik.method().invokeOther(callinstr.getMethodAddr().getName(), callinstr.getCallArgs().length);
                break;
            case SUPER:
                dalvik.method().invokeSuper(callinstr.getMethodAddr().getName(), callinstr.getCallArgs().length);
                break;
        }

        int index = dalvik.methodData().local(callinstr.getResult());
        dalvik.method().storeLocal(index);
    }
    
    @Override
    public void CheckArgsArrayArityInstr(CheckArgsArrayArityInstr checkargsarrayarityinstr) {
        super.CheckArgsArrayArityInstr(checkargsarrayarityinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void CheckArityInstr(CheckArityInstr checkarityinstr) {
        // no-op for now
    }

    @Override
    public void ClassSuperInstr(ClassSuperInstr classsuperinstr) {
        super.ClassSuperInstr(classsuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ClosureReturnInstr(ClosureReturnInstr closurereturninstr) {
        super.ClosureReturnInstr(closurereturninstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void ConstMissingInstr(ConstMissingInstr constmissinginstr) {
        CallInstr(constmissinginstr);
    }
    
    @Override
    public void CopyInstr(CopyInstr copyinstr) {
        int index = dalvik.methodData().local(copyinstr.getResult());
        visit(copyinstr.getSource());
        dalvik.method().storeLocal(index);
    }
    
    @Override
    public void DefineClassInstr(DefineClassInstr defineclassinstr) {
        IRClassBody newIRClassBody = defineclassinstr.getNewIRClassBody();
        StaticScope scope = newIRClassBody.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = RuntimeHelpers.encodeScope(scope);

        // emit method body and get handle
        emit(newIRClassBody); // handle

        // add'l args for CompiledIRMethod constructor
        dalvik.method().adapter.ldc(newIRClassBody.getName());
        dalvik.method().adapter.ldc(newIRClassBody.getFileName());
        dalvik.method().adapter.ldc(newIRClassBody.getLineNumber());

        // construct class with RuntimeHelpers.newClassForIR
        dalvik.method().loadLocal(0); // ThreadContext
        dalvik.method().adapter.ldc(newIRClassBody.getName()); // class name
        dalvik.method().loadLocal(2); // self

        // create class
        dalvik.method().loadLocal(0);
        visit(defineclassinstr.getContainer());
        dalvik.method().invokeHelper(TypeId.get(RubyModule.class), "checkIsRubyModule", TypeId.get(ThreadContext.class),TypeId.OBJECT);

        // superclass
        if (defineclassinstr.getSuperClass() instanceof Nil) {
            dalvik.method().adapter.aconst_null();
        } else {
            visit(defineclassinstr.getSuperClass());
        }

        // is meta?
        dalvik.method().adapter.ldc(newIRClassBody instanceof IRMetaClassBody);

        dalvik.method().invokeHelper(TypeId.get(RubyClass.class), "newClassForIR", TypeId.get(ThreadContext.class), 
                TypeId.STRING, TypeId.get(IRubyObject.class), TypeId.get(RubyModule.class), TypeId.OBJECT, TypeId.BOOLEAN);

        //// static scope
        dalvik.method().loadLocal(0);
        dalvik.method().loadLocal(1);
        dalvik.method().adapter.ldc(scopeString);
        dalvik.method().adapter.invokestatic(TypeId.get(RuntimeHelpers.class), TypeId.get(StaticScope.class), "decodeLocalScope", TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class),TypeId.STRING);
        dalvik.method().adapter.swap();

        // set into StaticScope
        dalvik.method().adapter.dup2();
        dalvik.method().adapter.invokevirtual(TypeId.get(StaticScope.class), TypeId.VOID, "setModule", TypeId.get(RubyModule.class));

        dalvik.method().adapter.getstatic(TypeId.get(Visibility.class), TypeId.get(Visibility.class), "PUBLIC");
        dalvik.method().adapter.swap();

        // new CompiledIRMethod
        dalvik.method().adapter.newobj(CompiledIRMethod.class, 7);
        
        // store
        dalvik.method().storeLocal(dalvik.methodData().local(defineclassinstr.getResult()));
    }
    
    @Override
    public void DefineClassMethodInstr(DefineClassMethodInstr defineclassmethodinstr) {
        super.DefineClassMethodInstr(defineclassmethodinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void DefineInstanceMethodInstr(DefineInstanceMethodInstr defineinstancemethodinstr) {
        IRMethod method = defineinstancemethodinstr.getMethod();
        StaticScope scope = method.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = RuntimeHelpers.encodeScope(scope);

        // preamble for addMethod below
        dalvik.method().loadLocal(0);
        dalvik.method().adapter.invokevirtual(TypeId.get(ThreadContext.class), TypeId.get(RubyModule.class), "getRubyClass");
        dalvik.method().adapter.ldc(method.getName());      

        // emit method body and get handle
        emit(method); // handle

        // args for CompiledIRMethod constructor
        dalvik.method().adapter.ldc(method.getName());
        dalvik.method().adapter.ldc(method.getFileName());
        dalvik.method().adapter.ldc(method.getLineNumber());

        dalvik.method().loadLocal(0);
        dalvik.method().loadLocal(1);
        dalvik.method().adapter.ldc(scopeString);
        dalvik.method().adapter.invokestatic(TypeId.get(RuntimeHelpers.class), TypeId.get(StaticScope.class), "decodeLocalScope", TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class),TypeId.STRING);

        dalvik.method().loadLocal(0);
        dalvik.method().adapter.invokevirtual(TypeId.get(ThreadContext.class), TypeId.get(Visibility.class), "getCurrentVisibility");
        dalvik.method().loadLocal(0);
        dalvik.method().adapter.invokevirtual(TypeId.get(ThreadContext.class), TypeId.get(RubyModule.class), "getRubyClass");

        // new CompiledIRMethod
        dalvik.method().adapter.newobj(CompiledIRMethod.class, 7);

        // add method
        dalvik.method().adapter.invokevirtual(TypeId.get(RubyModule.class), TypeId.VOID, "addMethod", TypeId.STRING, TypeId.get(DynamicMethod.class));
    }
    
    @Override
    public void DefineMetaClassInstr(DefineMetaClassInstr definemetaclassinstr) {
        IRModuleBody metaClassBody = definemetaclassinstr.getMetaClassBody();
        StaticScope scope = metaClassBody.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = RuntimeHelpers.encodeScope(scope);

        // emit method body and get handle
        emit(metaClassBody); // handle

        // add'l args for CompiledIRMethod constructor
        dalvik.method().adapter.ldc(metaClassBody.getName());
        dalvik.method().adapter.ldc(metaClassBody.getFileName());
        dalvik.method().adapter.ldc(metaClassBody.getLineNumber());

        //// static scope
        dalvik.method().loadLocal(0);
        dalvik.method().loadLocal(1);
        dalvik.method().adapter.ldc(scopeString);
        dalvik.method().adapter.invokestatic(TypeId.get(RuntimeHelpers.class), TypeId.get(StaticScope.class), "decodeLocalScope", TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class),TypeId.STRING);

        // get singleton class
        dalvik.method().pushRuntime();
        visit(definemetaclassinstr.getObject());
        dalvik.method().invokeHelper(TypeId.get(RubyClass.class), "getSingletonClass", TypeId.get(Ruby.class), TypeId.get(IRubyObject.class));

        // set into StaticScope
        dalvik.method().adapter.dup2();
        dalvik.method().adapter.invokevirtual(TypeId.get(StaticScope.class), TypeId.VOID, "setModule", TypeId.get(RubyModule.class));

        dalvik.method().adapter.getstatic(TypeId.get(Visibility.class), TypeId.get(Visibility.class), "PUBLIC");
        dalvik.method().adapter.swap();

        // new CompiledIRMethod
        dalvik.method().adapter.newobj(CompiledIRMethod.class, 7);

        // store
        dalvik.method().storeLocal(dalvik.methodData().local(definemetaclassinstr.getResult()));
    }
    
    @Override
    public void DefineModuleInstr(DefineModuleInstr definemoduleinstr) {
        IRModuleBody newIRModuleBody = definemoduleinstr.getNewIRModuleBody();
        StaticScope scope = newIRModuleBody.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = RuntimeHelpers.encodeScope(scope);

        // emit method body and get handle
        emit(newIRModuleBody); // handle

        // add'l args for CompiledIRMethod constructor
        dalvik.method().adapter.ldc(newIRModuleBody.getName());
        dalvik.method().adapter.ldc(newIRModuleBody.getFileName());
        dalvik.method().adapter.ldc(newIRModuleBody.getLineNumber());

        dalvik.method().loadLocal(0);
        dalvik.method().loadLocal(1);
        dalvik.method().adapter.ldc(scopeString);
        dalvik.method().adapter.invokestatic(TypeId.get(RuntimeHelpers.class), TypeId.get(StaticScope.class), "decodeLocalScope", TypeId.get(ThreadContext.class), TypeId.get(StaticScope.class),TypeId.STRING);

        // create module
        dalvik.method().loadLocal(0);
        visit(definemoduleinstr.getContainer());
        dalvik.method().invokeHelper(TypeId.get(RubyModule.class), "checkIsRubyModule", TypeId.get(ThreadContext.class), TypeId.OBJECT);
        dalvik.method().adapter.ldc(newIRModuleBody.getName());
        dalvik.method().adapter.invokevirtual(TypeId.get(RubyModule.class), TypeId.get(RubyModule.class), "defineOrGetModuleUnder", TypeId.STRING);

        // set into StaticScope
        dalvik.method().adapter.dup2();
        dalvik.method().adapter.invokevirtual(TypeId.get(StaticScope.class), TypeId.VOID, "setModule", TypeId.get(RubyModule.class));

        dalvik.method().adapter.getstatic(TypeId.get(Visibility.class), TypeId.get(Visibility.class), "PUBLIC");
        dalvik.method().adapter.swap();

        // new CompiledIRMethod
        dalvik.method().adapter.newobj(CompiledIRMethod.class, 7);

        // store
        dalvik.method().storeLocal(dalvik.methodData().local(definemoduleinstr.getResult()));
    }
    
    @Override
    public void EnsureRubyArrayInstr(EnsureRubyArrayInstr ensurerubyarrayinstr) {
        super.EnsureRubyArrayInstr(ensurerubyarrayinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void EQQInstr(EQQInstr eqqinstr) {
        super.EQQInstr(eqqinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ExceptionRegionEndMarkerInstr(ExceptionRegionEndMarkerInstr exceptionregionendmarkerinstr) {
        super.ExceptionRegionEndMarkerInstr(exceptionregionendmarkerinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ExceptionRegionStartMarkerInstr(ExceptionRegionStartMarkerInstr exceptionregionstartmarkerinstr) {
        super.ExceptionRegionStartMarkerInstr(exceptionregionstartmarkerinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetClassVarContainerModuleInstr(GetClassVarContainerModuleInstr getclassvarcontainermoduleinstr) {
        super.GetClassVarContainerModuleInstr(getclassvarcontainermoduleinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetClassVariableInstr(GetClassVariableInstr getclassvariableinstr) {
        super.GetClassVariableInstr(getclassvariableinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void GetFieldInstr(GetFieldInstr getfieldinstr) {
        String field = getfieldinstr.getRef();
        visit(getfieldinstr.getSource());
        dalvik.method().getField(field);
        dalvik.method().storeLocal(dalvik.methodData().local(getfieldinstr.getResult()));
    }
    
    @Override
    public void GetGlobalVariableInstr(GetGlobalVariableInstr getglobalvariableinstr) {
        super.GetGlobalVariableInstr(getglobalvariableinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GVarAliasInstr(GVarAliasInstr gvaraliasinstr) {
        super.GVarAliasInstr(gvaraliasinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void InheritanceSearchConstInstr(InheritanceSearchConstInstr inheritancesearchconstinstr) {
        dalvik.method().loadLocal(0);
        visit(inheritancesearchconstinstr.getCurrentModule());

        // TODO: private consts
        dalvik.method().inheritanceSearchConst(inheritancesearchconstinstr.getConstName());
        dalvik.method().storeLocal(dalvik.methodData().local(inheritancesearchconstinstr.getResult()));
    }
    
    @Override
    public void InstanceOfInstr(InstanceOfInstr instanceofinstr) {
        super.InstanceOfInstr(instanceofinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void InstanceSuperInstr(InstanceSuperInstr instancesuperinstr) {
        super.InstanceSuperInstr(instancesuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void JumpIndirectInstr(JumpIndirectInstr jumpindirectinstr) {
        super.JumpIndirectInstr(jumpindirectinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void JumpInstr(JumpInstr jumpinstr) {
        dalvik.method().goTo(dalvik.methodData().getLabel(jumpinstr.getJumpTarget()));
    }

    @Override
    public void LabelInstr(LabelInstr labelinstr) {
        dalvik.method().mark(dalvik.methodData().getLabel(labelinstr.getLabel()));
    }

    @Override
    public void LexicalSearchConstInstr(LexicalSearchConstInstr lexicalsearchconstinstr) {
        super.LexicalSearchConstInstr(lexicalsearchconstinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void LineNumberInstr(LineNumberInstr linenumberinstr) {
        // No need for line numbers
    }
    
    @Override
    public void LoadLocalVarInstr(LoadLocalVarInstr loadlocalvarinstr) {
        dalvik.method().loadLocal(dalvik.methodData().local(DYNAMIC_SCOPE));
        int depth = loadlocalvarinstr.getLocalVar().getScopeDepth();
        // TODO should not have to subtract 1
        int location = loadlocalvarinstr.getLocalVar().getLocation() - 1;
        // TODO if we can avoid loading nil unnecessarily, it could be a big win
        switch (depth) {
            case 0:
                switch (location) {
                    case 0:
                        dalvik.method().pushNil();
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), 
                                "getValueZeroDepthZeroOrNil", TypeId.get(IRubyObject.class));
                        return;
                    case 1:
                        dalvik.method().pushNil();
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), 
                                "getValueOneDepthZeroOrNil", TypeId.get(IRubyObject.class));
                        return;
                    case 2:
                        dalvik.method().pushNil();
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), 
                                "getValueTwoDepthZeroOrNil", TypeId.get(IRubyObject.class));
                        return;
                    case 3:
                        dalvik.method().pushNil();
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), 
                                "getValueThreeDepthZeroOrNil", TypeId.get(IRubyObject.class));
                        return;
                    default:
                        dalvik.method().adapter.pushInt(location);
                        dalvik.method().pushNil();
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), 
                                "getValueDepthZeroOrNil", TypeId.INT, TypeId.get(IRubyObject.class));
                        return;
                }
            default:
                dalvik.method().adapter.pushInt(location);
                dalvik.method().adapter.pushInt(depth);
                dalvik.method().pushNil();
                dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), "getValueOrNil", 
                        TypeId.INT, TypeId.INT, TypeId.get(IRubyObject.class));
        }
    }
    
    @Override
    public void Match2Instr(Match2Instr match2instr) {
        super.Match2Instr(match2instr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Match3Instr(Match3Instr match3instr) {
        super.Match3Instr(match3instr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MatchInstr(MatchInstr matchinstr) {
        super.MatchInstr(matchinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodLookupInstr(MethodLookupInstr methodlookupinstr) {
        super.MethodLookupInstr(methodlookupinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ModuleVersionGuardInstr(ModuleVersionGuardInstr moduleversionguardinstr) {
        super.ModuleVersionGuardInstr(moduleversionguardinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void NopInstr(NopInstr nopinstr) {
        // do nothing
    }
    
    @Override
    public void NoResultCallInstr(NoResultCallInstr noResultCallInstr) {
        dalvik.method().loadLocal(0);
        visit(noResultCallInstr.getReceiver());
        for (Operand operand : noResultCallInstr.getCallArgs()) {
            visit(operand);
        }

        switch (noResultCallInstr.getCallType()) {
            case FUNCTIONAL:
            case VARIABLE:
                dalvik.method().invokeSelf(noResultCallInstr.getMethodAddr().getName(), noResultCallInstr.getCallArgs().length);
                break;
            case NORMAL:
                dalvik.method().invokeOther(noResultCallInstr.getMethodAddr().getName(), noResultCallInstr.getCallArgs().length);
                break;
            case SUPER:
                dalvik.method().invokeSuper(noResultCallInstr.getMethodAddr().getName(), noResultCallInstr.getCallArgs().length);
                break;
        }

        dalvik.method().adapter.pop();
    }
    
    @Override
    public void NotInstr(NotInstr notinstr) {
        super.NotInstr(notinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void OptArgMultipleAsgnInstr(OptArgMultipleAsgnInstr optargmultipleasgninstr) {
        super.OptArgMultipleAsgnInstr(optargmultipleasgninstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void PopBindingInstr(PopBindingInstr popbindinginstr) {
        // TODO pop
    }
    
    @Override
    public void ProcessModuleBodyInstr(ProcessModuleBodyInstr processmodulebodyinstr) {
        dalvik.method().loadLocal(0);
        visit(processmodulebodyinstr.getModuleBody());
        dalvik.method().invokeHelper(TypeId.get(IRubyObject.class), "invokeModuleBody", TypeId.get(ThreadContext.class), TypeId.get(CompiledIRMethod.class));
        dalvik.method().storeLocal(dalvik.methodData().local(processmodulebodyinstr.getResult()));
    }
    
    @Override
    public void PushBindingInstr(PushBindingInstr pushbindinginstr) {
        dalvik.method().loadStaticScope();
        dalvik.method().adapter.invokestatic(TypeId.get(DynamicScope.class), TypeId.get(DynamicScope.class), "newDynamicScope", TypeId.get(StaticScope.class));
        dalvik.method().storeLocal(dalvik.methodData().local(DYNAMIC_SCOPE));

        // TODO push
    }
    
    @Override
    public void PutConstInstr(PutConstInstr putconstinstr) {
        visit(putconstinstr.getTarget());
        dalvik.method().adapter.checkcast(RubyModule.class);
        dalvik.method().adapter.ldc(putconstinstr.getRef());
        visit(putconstinstr.getValue());
        dalvik.method().adapter.invokevirtual(TypeId.get(RubyModule.class), TypeId.get(IRubyObject.class), "setConstant", TypeId.get(String.class), TypeId.get(IRubyObject.class));
        dalvik.method().adapter.pop();
    }
    
    @Override
    public void PutFieldInstr(PutFieldInstr putfieldinstr) {
        String field = putfieldinstr.getRef();
        visit(putfieldinstr.getTarget());
        visit(putfieldinstr.getValue());
        dalvik.method().putField(field);
    }
    
    @Override
    public void PutClassVariableInstr(PutClassVariableInstr putclassvariableinstr) {
        super.PutClassVariableInstr(putclassvariableinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void PutGlobalVarInstr(PutGlobalVarInstr putglobalvarinstr) {
        super.PutGlobalVarInstr(putglobalvarinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void RaiseArgumentErrorInstr(RaiseArgumentErrorInstr raiseargumenterrorinstr) {
        super.RaiseArgumentErrorInstr(raiseargumenterrorinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void ReceiveClosureInstr(ReceiveClosureInstr receiveclosureinstr) {
        dalvik.method().loadLocal(dalvik.methodData().local("$block"));
        dalvik.method().storeLocal(dalvik.methodData().local(receiveclosureinstr.getResult()));
    }
    
    @Override
    public void ReceiveExceptionInstr(ReceiveExceptionInstr receiveexceptioninstr) {
        // TODO implement
    }
    
    @Override
    public void ReceivePreReqdArgInstr(ReceivePreReqdArgInstr receiveprereqdarginstr) {
        int index = dalvik.methodData().local(receiveprereqdarginstr.getResult());
        dalvik.method().loadLocal(3 + receiveprereqdarginstr.getArgIndex());
        dalvik.method().storeLocal(index);
    }
    
    @Override
    public void ReceiveSelfInstr(ReceiveSelfInstr receiveselfinstr) {
        int $selfIndex = dalvik.methodData().local(receiveselfinstr.getResult());
        dalvik.method().loadLocal(2);
        dalvik.method().storeLocal($selfIndex);
    }
    
    @Override
    public void RecordEndBlockInstr(RecordEndBlockInstr recordendblockinstr) {
        super.RecordEndBlockInstr(recordendblockinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReqdArgMultipleAsgnInstr(ReqdArgMultipleAsgnInstr reqdargmultipleasgninstr) {
        super.ReqdArgMultipleAsgnInstr(reqdargmultipleasgninstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void RescueEQQInstr(RescueEQQInstr rescueeqqinstr) {
        super.RescueEQQInstr(rescueeqqinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void RestArgMultipleAsgnInstr(RestArgMultipleAsgnInstr restargmultipleasgninstr) {
        super.RestArgMultipleAsgnInstr(restargmultipleasgninstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void ReturnInstr(ReturnInstr returninstr) {
        visit(returninstr.getReturnValue());
        dalvik.method().returnValue();
    }
    
    @Override
    public void SearchConstInstr(SearchConstInstr searchconstinstr) {
        // TODO: private consts
        dalvik.method().loadLocal(0);
        visit(searchconstinstr.getStartingScope());
        dalvik.method().searchConst(searchconstinstr.getConstName());
        dalvik.method().storeLocal(dalvik.methodData().local(searchconstinstr.getResult()));
    }
    
    @Override
    public void SetReturnAddressInstr(SetReturnAddressInstr setreturnaddressinstr) {
        super.SetReturnAddressInstr(setreturnaddressinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void StoreLocalVarInstr(StoreLocalVarInstr storelocalvarinstr) {
        dalvik.method().loadLocal(dalvik.methodData().local(DYNAMIC_SCOPE));
        int depth = storelocalvarinstr.getLocalVar().getScopeDepth();
        // TODO should not have to subtract 1
        int location = storelocalvarinstr.getLocalVar().getLocation() - 1;
        switch (depth) {
            case 0:
                switch (location) {
                    case 0:
                        storelocalvarinstr.getValue().visit(this);
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), "setValueZeroDepthZero", TypeId.get(IRubyObject.class));
                        dalvik.method().adapter.pop();
                        return;
                    case 1:
                        storelocalvarinstr.getValue().visit(this);
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), "setValueOneDepthZero", TypeId.get(IRubyObject.class));
                        dalvik.method().adapter.pop();
                        return;
                    case 2:
                        storelocalvarinstr.getValue().visit(this);
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), "setValueTwoDepthZero", TypeId.get(IRubyObject.class));
                        dalvik.method().adapter.pop();
                        return;
                    case 3:
                        storelocalvarinstr.getValue().visit(this);
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), "setValueThreeDepthZero", TypeId.get(IRubyObject.class));
                        dalvik.method().adapter.pop();
                        return;
                    default:
                        storelocalvarinstr.getValue().visit(this);
                        dalvik.method().adapter.pushInt(location);
                        dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class),
                                "setValueDepthZero", TypeId.get(IRubyObject.class), TypeId.INT);
                        dalvik.method().adapter.pop();
                        return;
                }
            default:
                dalvik.method().adapter.pushInt(depth);
                storelocalvarinstr.getValue().visit(this);
                dalvik.method().adapter.pushInt(location);
                dalvik.method().adapter.invokevirtual(TypeId.get(DynamicScope.class), TypeId.get(IRubyObject.class), "setValue", 
                        TypeId.INT, TypeId.get(IRubyObject.class), TypeId.INT);
                dalvik.method().adapter.pop();
        }
    }
    
    @Override
    public void ThreadPollInstr(ThreadPollInstr threadpollinstr) {
        dalvik.method().poll();
    }
    
    @Override
    public void ThrowExceptionInstr(ThrowExceptionInstr throwexceptioninstr) {
        // TODO implement
    }

    @Override
    public void ToAryInstr(ToAryInstr toaryinstr) {
        super.ToAryInstr(toaryinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void UndefMethodInstr(UndefMethodInstr undefmethodinstr) {
        super.UndefMethodInstr(undefmethodinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void UnresolvedSuperInstr(UnresolvedSuperInstr unresolvedsuperinstr) {
        super.UnresolvedSuperInstr(unresolvedsuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void YieldInstr(YieldInstr yieldinstr) {
        visit(yieldinstr.getBlockArg());

        // TODO: proc, nil block logic

        dalvik.method().loadLocal(0);
        if (yieldinstr.getYieldArg() == UndefinedValue.UNDEFINED) {
            dalvik.method().adapter.invokevirtual(TypeId.get(Block.class), TypeId.get(IRubyObject.class), "yieldSpecific", TypeId.get(ThreadContext.class));
        } else {
            visit(yieldinstr.getYieldArg());

            // TODO: if yielding array, call yieldArray

            dalvik.method().adapter.invokevirtual(TypeId.get(Block.class), TypeId.get(IRubyObject.class), "yield", TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class));
        }

        dalvik.method().storeLocal(dalvik.methodData().local(yieldinstr.getResult()));
    }
    
    @Override
    public void ZSuperInstr(ZSuperInstr zsuperinstr) {
        super.ZSuperInstr(zsuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    // "defined" instructions
    @Override
    public void BackrefIsMatchDataInstr(BackrefIsMatchDataInstr backrefismatchdatainstr) {
        super.BackrefIsMatchDataInstr(backrefismatchdatainstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ClassVarIsDefinedInstr(ClassVarIsDefinedInstr classvarisdefinedinstr) {
        super.ClassVarIsDefinedInstr(classvarisdefinedinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetBackrefInstr(GetBackrefInstr getbackrefinstr) {
        super.GetBackrefInstr(getbackrefinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetDefinedConstantOrMethodInstr(GetDefinedConstantOrMethodInstr getdefinedconstantormethodinstr) {
        super.GetDefinedConstantOrMethodInstr(getdefinedconstantormethodinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetErrorInfoInstr(GetErrorInfoInstr geterrorinfoinstr) {
        super.GetErrorInfoInstr(geterrorinfoinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GlobalIsDefinedInstr(GlobalIsDefinedInstr globalisdefinedinstr) {
        super.GlobalIsDefinedInstr(globalisdefinedinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
     @Override
    public void HasInstanceVarInstr(HasInstanceVarInstr hasinstancevarinstr) {
        // TODO: This is suboptimal, not caching ivar offset at all
        dalvik.method().pushRuntime();
        visit(hasinstancevarinstr.getObject());
        dalvik.method().adapter.invokeinterface(TypeId.get(IRubyObject.class), TypeId.get(InstanceVariables.class), "getInstanceVariables");
        dalvik.method().adapter.ldc(hasinstancevarinstr.getName().string);
        dalvik.method().adapter.invokeinterface(TypeId.get(InstanceVariables.class), TypeId.BOOLEAN, "hasInstanceVariable", TypeId.STRING);
        dalvik.method().adapter.invokevirtual(TypeId.get(Ruby.class), TypeId.get(RubyBoolean.class), "newBoolean", TypeId.BOOLEAN);
        dalvik.method().storeLocal(dalvik.methodData().local(hasinstancevarinstr.getResult()));
    }
    
    @Override
    public void IsMethodBoundInstr(IsMethodBoundInstr ismethodboundinstr) {
        super.IsMethodBoundInstr(ismethodboundinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodDefinedInstr(MethodDefinedInstr methoddefinedinstr) {
        super.MethodDefinedInstr(methoddefinedinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodIsPublicInstr(MethodIsPublicInstr methodispublicinstr) {
        super.MethodIsPublicInstr(methodispublicinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void RestoreErrorInfoInstr(RestoreErrorInfoInstr restoreerrorinfoinstr) {
        super.RestoreErrorInfoInstr(restoreerrorinfoinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void SuperMethodBoundInstr(SuperMethodBoundInstr supermethodboundinstr) {
        super.SuperMethodBoundInstr(supermethodboundinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    // ruby 1.8 specific
    @Override
    public void ReceiveOptArgInstr18(ReceiveOptArgInstr18 receiveoptarginstr) {
        super.ReceiveOptArgInstr18(receiveoptarginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReceiveRestArgInstr18(ReceiveRestArgInstr18 receiverestarginstr) {
        super.ReceiveRestArgInstr18(receiverestarginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    // ruby 1.9 specific
    @Override
    public void BuildLambdaInstr(BuildLambdaInstr buildlambdainstr) {
        super.BuildLambdaInstr(buildlambdainstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetEncodingInstr(GetEncodingInstr getencodinginstr) {
        super.GetEncodingInstr(getencodinginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReceiveOptArgInstr19(ReceiveOptArgInstr19 receiveoptarginstr) {
        super.ReceiveOptArgInstr19(receiveoptarginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReceivePostReqdArgInstr(ReceivePostReqdArgInstr receivepostreqdarginstr) {
        super.ReceivePostReqdArgInstr(receivepostreqdarginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReceiveRestArgInstr19(ReceiveRestArgInstr19 receiverestarginstr) {
        super.ReceiveRestArgInstr19(receiverestarginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    // operands
    @Override
    public void Array(Array array) {
        dalvik.method().loadLocal(0);

        for (Operand operand : array.getElts()) {
            visit(operand);
        }

        dalvik.method().array(array.getElts().length);
    }
    
    @Override
    public void AsString(AsString asstring) {
        super.AsString(asstring);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Backref(Backref backref) {
        super.Backref(backref);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void BacktickString(BacktickString backtickstring) {
        super.BacktickString(backtickstring);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Bignum(Bignum bignum) {
        super.Bignum(bignum);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void BooleanLiteral(BooleanLiteral booleanliteral) {
        dalvik.method().pushBoolean(booleanliteral.isTrue());
    }
    
    @Override
    public void ClosureLocalVariable(ClosureLocalVariable closurelocalvariable) {
        super.ClosureLocalVariable(closurelocalvariable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void CompoundArray(CompoundArray compoundarray) {
        super.CompoundArray(compoundarray);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void CompoundString(CompoundString compoundstring) {
        super.CompoundString(compoundstring);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void CurrentScope(CurrentScope currentscope) {
        dalvik.method().loadLocal(1);
    }
    
    @Override
    public void DynamicSymbol(DynamicSymbol dynamicsymbol) {
        super.DynamicSymbol(dynamicsymbol);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void Fixnum(Fixnum fixnum) {
        dalvik.method().push(fixnum.getValue());
    }
    
    @Override
    public void Float(org.jruby.ir.operands.Float flote) {
        super.Float(flote);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GlobalVariable(GlobalVariable globalvariable) {
        super.GlobalVariable(globalvariable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Hash(Hash hash) {
        super.Hash(hash);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void IRException(IRException irexception) {
        super.IRException(irexception);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Label(Label label) {
        super.Label(label);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void LocalVariable(LocalVariable localvariable) {
        super.LocalVariable(localvariable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethAddr(MethAddr methaddr) {
        super.MethAddr(methaddr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodHandle(MethodHandle methodhandle) {
        super.MethodHandle(methodhandle);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Nil(Nil nil) {
        dalvik.method().pushNil();
    }
    
    @Override
    public void NthRef(NthRef nthref) {
        super.NthRef(nthref);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void ObjectClass(ObjectClass objectclass) {
        dalvik.method().pushObjectClass();
    }
    
    @Override
    public void Range(Range range) {
        super.Range(range);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Regexp(Regexp regexp) {
        super.Regexp(regexp);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void ScopeModule(ScopeModule scopemodule) {
        dalvik.method().loadLocal(1);
        dalvik.method().adapter.invokevirtual(TypeId.get(StaticScope.class), TypeId.get(RubyModule.class), "getModule");
    }
    
    @Override
    public void Self(Self self) {
        super.Self(self);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Splat(Splat splat) {
        super.Splat(splat);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void StandardError(StandardError standarderror) {
        super.StandardError(standarderror);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void StringLiteral(StringLiteral stringliteral) {
        dalvik.method().push(stringliteral.getByteList());
    }
    
    @Override
    public void SValue(SValue svalue) {
        super.SValue(svalue);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void Symbol(Symbol symbol) {
        dalvik.method().push(symbol.getName());
    }
    
    @Override
    public void TemporaryClosureVariable(TemporaryClosureVariable temporaryclosurevariable) {
        super.TemporaryClosureVariable(temporaryclosurevariable);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public void TemporaryVariable(TemporaryVariable temporaryvariable) {
        dalvik.method().loadLocal(dalvik.methodData().local(temporaryvariable));
    }
    
    @Override
    public void UndefinedValue(UndefinedValue undefinedvalue) {
        dalvik.method().pushUndefined();
    }
    
    @Override
    public void UnexecutableNil(UnexecutableNil unexecutablenil) {
        super.UnexecutableNil(unexecutablenil);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void WrappedIRClosure(WrappedIRClosure wrappedirclosure) {
        super.WrappedIRClosure(wrappedirclosure);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    private final Dalvik dalvik;
    private IRScriptBody script;
}
