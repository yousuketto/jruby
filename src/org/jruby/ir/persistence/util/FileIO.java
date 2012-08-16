package org.jruby.ir.persistence.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public enum FileIO {
    INSTANCE;

    public void writeToFile(final File file, final String containment) throws IOException {
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        
        writeToFileCommon(containment, fileOutputStream);
    }

    public void writeToFile(final String fileName, final String containment) throws IOException {
        final File file = new File(fileName);
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        
        writeToFileCommon(containment, fileOutputStream);
    }

    private void writeToFileCommon(final String containment, final FileOutputStream fos) throws IOException {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(fos);
            outputStreamWriter.write(containment);
        } finally {
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        }
    }

}
