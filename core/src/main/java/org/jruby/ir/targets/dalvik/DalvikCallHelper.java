package org.jruby.ir.targets.dalvik;

import com.google.dexmaker.TypeId;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jruby.RubyFixnum;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.objectweb.asm.Handle;

/**
 *
 * @author lynnewallace
 */
public class DalvikCallHelper {
    // TODO: These are all using slow, uncached paths to retrieve literals
    // and call methods; they will need improvement to cache properly.
    
    public static void invokeSelf(DexMethodAdapter adapter, TypeId returnval, String name, TypeId[] params) {
        adapter.ldc(name);

        adapter.invokestatic(TypeId.get(RuntimeHelpers.class), returnval, "invoke", params);
    }

    // compile-time
    public static void fixnum(DexMethodAdapter adapter, long value) {
        adapter.ldc(value);

        adapter.invokestatic(TypeId.get(DalvikCallHelper.class), TypeId.get(IRubyObject.class), "fixnum", TypeId.get(ThreadContext.class), TypeId.get(long.class));

    }

    // run-time
    public static IRubyObject fixnum(ThreadContext context, long value) {
        return context.runtime.newFixnum(value);
    }

    // compile-time
    public static void string(DexMethodAdapter adapter, ByteList value) {
        String asString = RuntimeHelpers.rawBytesToString(value.bytes());
        String encName = new String(value.getEncoding().getName());

        adapter.invokestatic(TypeId.get(DalvikCallHelper.class), TypeId.get(IRubyObject.class), "string", TypeId.get(ThreadContext.class), TypeId.get(String.class), TypeId.get(String.class));

    }

    // run-time
    public static IRubyObject string(ThreadContext context, String value, String encoding) {
        byte[] rawBytes = RuntimeHelpers.stringToRawBytes(value);
        ByteList bl = new ByteList(rawBytes, Encoding.load(encoding));

        return context.runtime.newString(bl);
    }

    // compile-time
    public static void symbol(DexMethodAdapter adapter, String value) {
        adapter.ldc(value);

        adapter.invokestatic(TypeId.get(DalvikCallHelper.class), TypeId.get(IRubyObject.class), "symbol", TypeId.get(ThreadContext.class), TypeId.get(String.class));

    }

    // run-time
    public static IRubyObject symbol(ThreadContext context, String value) {
        return context.runtime.newSymbol(value);
    }
}
