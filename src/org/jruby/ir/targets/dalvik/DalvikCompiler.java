package org.jruby.ir.targets.dalvik;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;

import org.jruby.ast.executable.AbstractScript;
import org.jruby.runtime.ThreadContext;
import static org.jruby.util.CodegenUtils.*;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;

/**
 *
 * @author lynnewallace
 */
public class DalvikCompiler {
    
    private String classname;
    private String sourcename;

    private DexMaker dexmaker = new DexMaker();

    public DalvikCompiler(String classname, String sourcename) {
        this.classname = classname;
        this.sourcename = sourcename;
    }
    
    public byte[] getClassByteArray() {
        return dexmaker.generate();
    }
    
    public void writeClass(String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);

        try {
            out.write(getClassByteArray());
        } finally {
            out.close();
        }
    }
    
    public String getClassname() {
        return classname;
    }
    
    public DexMaker getClassVisitor() {
        return dexmaker;
    }
     
    public void startScript() {    
        // declare class
        TypeId<?> classtype = TypeId.get("L" + getClassname() + ";");
        TypeId<?> supertype = TypeId.get(ci(AbstractScript.class));
        getClassVisitor().declare(classtype, getClassname(), Modifier.PUBLIC, supertype);
        
        // declare method
        MethodId methodId = classtype.getMethod(TypeId.VOID, "setPosition", TypeId.get(ThreadContext.class), TypeId.INT);
        DexMethodAdapter method = new DexMethodAdapter(getClassVisitor(), Modifier.PRIVATE | Modifier.STATIC, methodId, null);
        method.voidreturn();
        
        try {
            writeClass("jruby.dex");
        } catch (IOException ex) {
            // TODO Log something
        }
    }  
}