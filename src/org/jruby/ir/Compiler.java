package org.jruby.ir;

import java.lang.reflect.InvocationTargetException;

import org.jruby.Ruby;
import org.jruby.ast.executable.AbstractScript;
import org.jruby.ast.executable.Script;
import org.jruby.exceptions.JumpException;
import org.jruby.ir.targets.JVMVisitor;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.JRubyClassLoader;

public class Compiler extends IRTranslator<Script, JRubyClassLoader> {

    // Compiler is singleton
    private Compiler() {
    }

    private static class CompilerHolder {
        public static final Compiler instance = new Compiler();
    }

    public static Compiler getInstance() {
        return CompilerHolder.instance;
    }
    
    @Override
    protected Script translationSpecificLogic(final Ruby runtime, IRScope scope, JRubyClassLoader classLoader) {
        final Class<?> compiled = JVMVisitor.compile(runtime, scope, classLoader);
        final StaticScope staticScope = scope.getStaticScope();
        final IRubyObject runtimeTopSelf = runtime.getTopSelf();
        staticScope.setModule(runtimeTopSelf.getMetaClass());
        return new AbstractScript() {
            public IRubyObject __file__(ThreadContext context, IRubyObject self,
                    IRubyObject[] args, Block block) {
                try {
                    return (IRubyObject) compiled.getMethod("__script__0", ThreadContext.class,
                            StaticScope.class, IRubyObject.class, Block.class).invoke(null,
                            runtime.getCurrentContext(), staticScope, runtimeTopSelf,
                            block);
                } catch (InvocationTargetException ite) {
                    if (ite.getCause() instanceof JumpException) {
                        throw (JumpException) ite.getCause();
                    } else {
                        throw new RuntimeException(ite);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public IRubyObject load(ThreadContext context, IRubyObject self, boolean wrap) {
                try {
                    RuntimeHelpers.preLoadCommon(context, staticScope, false);
                    return __file__(context, self, IRubyObject.NULL_ARRAY, Block.NULL_BLOCK);
                } finally {
                    RuntimeHelpers.postLoad(context);
                }
            }
        };
    }

}
