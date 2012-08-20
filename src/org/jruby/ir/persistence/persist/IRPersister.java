package org.jruby.ir.persistence.persist;

import java.io.File;

import org.jruby.ir.IRScope;
import org.jruby.ir.persistence.IRPersistenceException;
import org.jruby.ir.persistence.persist.string.IRToStringTranslator;
import org.jruby.ir.persistence.util.FileIO;
import org.jruby.ir.persistence.util.IRFileExpert;

/**
 * Persists IRScope in intended place
 */
public class IRPersister {

    public static void persist(final IRScope irScopeToPersist)
            throws IRPersistenceException {
        try {
            final String stringRepresentationOfIR = IRToStringTranslator.translate(irScopeToPersist);
            final String rbFileName = irScopeToPersist.getFileName();
            
            // Persist ir only if its not up to date with rb file
            final boolean needToPersistIr = !IRFileExpert.INSTANCE.persistedIrIsUpToDateForRbFile(rbFileName);
            if (needToPersistIr) {
                File irFile = IRFileExpert.INSTANCE.getIrFileByRbFileForPersistence(rbFileName);
                FileIO.INSTANCE.writeToFile(irFile, stringRepresentationOfIR);
                // Set IR file to be up to date with rb file
                IRFileExpert.INSTANCE.rememberModificationTimeForIr(rbFileName);
            }
        } catch (Exception e) { // We do not want to brake current run, so catch even unchecked exceptions
            throw new IRPersistenceException(e);
        }

    }
}
