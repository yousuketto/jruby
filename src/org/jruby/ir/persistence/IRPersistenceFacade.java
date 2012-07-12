package org.jruby.ir.persistence;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ir.IRScope;
import org.jruby.ir.persistence.lexer.PersistedIRScanner;
import org.jruby.ir.persistence.parser.IRParsingContext;
import org.jruby.ir.persistence.parser.PersistedIRParser;
import org.jruby.ir.persistence.util.FileIO;
import org.jruby.ir.persistence.util.IRFileExpert;

// This class currently contains code that will be decoupled later on
public class IRPersistenceFacade {

    public static void persist(IRScope irScopeToPersist, Ruby runtime)
            throws IRPersistenceException {
        try {
            RubyInstanceConfig config = runtime.getInstanceConfig();
            String rbFileName = IRReadingContext.INSTANCE.getFileName();
            File irFile = IRFileExpert.INSTANCE.getIRFileInIntendedPlace(config, rbFileName);

            StringBuilder instructions = new StringBuilder();
            getIstructionsFromThisAndDescendantScopes(irScopeToPersist, instructions);
            FileIO.INSTANCE.writeToFile(irFile, instructions.toString());
        } catch (Exception e) {
            throw new IRPersistenceException(e);
        }

    }

    private static void getIstructionsFromThisAndDescendantScopes(IRScope irScopeToPersist,
            StringBuilder instructions) {
        instructions.append(irScopeToPersist.toPersistableString());
        instructions.append("\n\n");
        for (IRScope irScope : irScopeToPersist.getLexicalScopes()) {
            getIstructionsFromThisAndDescendantScopes(irScope, instructions);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<IRScope> read(Ruby runtime) throws IRPersistenceException {
        IRParsingContext.INSTANCE.setRuntime(runtime);
        RubyInstanceConfig config = runtime.getInstanceConfig();
        String irFileName = IRReadingContext.INSTANCE.getFileName();
        File irFile = IRFileExpert.INSTANCE.getIRFileInIntendedPlace(config, irFileName);
        try {
            String fileContent = FileIO.INSTANCE.readFile(irFile);
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(fileContent.getBytes(FileIO.CHARSET));
                PersistedIRScanner input = new PersistedIRScanner(is);
                return (List<IRScope>) new PersistedIRParser().parse(input);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (FileNotFoundException e) {
            throw new IRPersistenceException(e);
        } catch (IOException e) {
            throw new IRPersistenceException(e);
        } catch (beaver.Parser.Exception e) {
            throw new IRPersistenceException(e);
        }
    }
}
