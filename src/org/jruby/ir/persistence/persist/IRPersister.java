package org.jruby.ir.persistence.persist;

import java.io.File;

import org.jruby.ir.IRScope;
import org.jruby.ir.persistence.IRPersistenceException;
import org.jruby.ir.persistence.persist.string.IRToStringTranslator;
import org.jruby.ir.persistence.util.FileIO;
import org.jruby.ir.persistence.util.IRFileExpert;


public class IRPersister {

    public static void persist(final IRScope irScopeToPersist)
            throws IRPersistenceException {
        try {
            final String stringRepresentationOfIR = IRToStringTranslator.translate(irScopeToPersist);
            final String rbFileName = irScopeToPersist.getFileName();
            
            final boolean needToPersistIr = !IRFileExpert.INSTANCE.persistedIrIsUpToDateForRbFile(rbFileName);
            if (needToPersistIr) {
                File irFile = IRFileExpert.INSTANCE.getIrFileByRbFileForPersistence(rbFileName);
                FileIO.INSTANCE.writeToFile(irFile, stringRepresentationOfIR);
                IRFileExpert.INSTANCE.rememberModificationTimeForIr(rbFileName);
            }
        } catch (Exception e) { // We do not want to brake current run, so catch even unchecked exceptions
            throw new IRPersistenceException(e);
        }

    }
}
