package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.TypeId;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jruby.Ruby;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRScriptBody;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Tuple;
import org.jruby.ir.instructions.AliasInstr;
import org.jruby.ir.instructions.CallInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Variable;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.JRubyClassLoader;

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
    
    public static void compile(Ruby ruby, IRScope scope, JRubyClassLoader jrubyClassLoader) throws IOException{
        // run compiler
        DalvikVisitor target = new DalvikVisitor();
        target.codegen(scope);
        
        // write out
        FileOutputStream out = new FileOutputStream("jruby.dex");
        try {
            out.write(target.code());
        } finally {
            out.close();
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
        emitScope(method, method.getName(), method.getCallArgs().length);
    }

    public void emit(IRModuleBody method) {
        String name = method.getName();
        if (name.indexOf("DUMMY_MC") != -1) {
            name = "METACLASS";
        }

        emitScope(method, name, 0);
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
        dalvik.method().adapter.aload(index, variable.getClass());
    }

    @Override
    public void AliasInstr(AliasInstr aliasInstr) {
        dalvik.method().adapter.aload(0, null);
        dalvik.method().adapter.aload(dalvik.methodData().local(aliasInstr.getReceiver()), null);
        dalvik.method().adapter.ldc(((StringLiteral) aliasInstr.getNewName()).string);
        dalvik.method().adapter.ldc(((StringLiteral) aliasInstr.getOldName()).string);
        dalvik.method().adapter.invokestatic(TypeId.get(RuntimeHelpers.class), TypeId.get(IRubyObject.class), "defineAlias",
                TypeId.get(ThreadContext.class), TypeId.get(IRubyObject.class), TypeId.get(Object.class), TypeId.get(Object.class));
        dalvik.method().adapter.pop();
    }
    
    @Override
    public void CallInstr(CallInstr callinstr) {
//        dalvik.method().loadLocal(0);
//        visit(callinstr.getReceiver());
//        for (Operand operand : callinstr.getCallArgs()) {
//            visit(operand);
//        }
//
//        switch (callinstr.getCallType()) {
//            case FUNCTIONAL:
//            case VARIABLE:
//                jvm.method().invokeSelf(callinstr.getMethodAddr().getName(), callinstr.getCallArgs().length);
//                break;
//            case NORMAL:
//                jvm.method().invokeOther(callinstr.getMethodAddr().getName(), callinstr.getCallArgs().length);
//                break;
//            case SUPER:
//                jvm.method().invokeSuper(callinstr.getMethodAddr().getName(), callinstr.getCallArgs().length);
//                break;
//        }
//
//        int index = jvm.methodData().local(callinstr.getResult());
//        jvm.method().storeLocal(index);
    }
    
    private final Dalvik dalvik;
    private IRScriptBody script;
}
