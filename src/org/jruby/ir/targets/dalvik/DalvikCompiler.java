package org.jruby.ir.targets.dalvik;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jruby.ast.executable.AbstractScript;
import static org.jruby.util.CodegenUtils.ci;
import com.google.dexmaker.DexMaker;
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
    
    public void writeClass(String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);

        try {
            out.write(getClassByteArray());
        } finally {
            out.close();
        }
    }
    
    public byte[] getClassByteArray() {
        return dexmaker.generate();
    }
    
    public DexMaker getClassVisitor() {
        return dexmaker;
    }
    
    public String getClassname() {
        return classname;
    }
     
    public void startScript() {    
        TypeId<?> classtype = TypeId.get("L" + getClassname() + ";");
        TypeId<?> supertype = TypeId.get(ci(AbstractScript.class));
        getClassVisitor().declare(classtype, getClassname(), Modifier.PUBLIC, supertype);
        try {
            writeClass("jruby.dex");
        } catch (IOException ex) {
            // TODO Log something
        }
    }  
}