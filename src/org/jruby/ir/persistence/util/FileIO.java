package org.jruby.ir.persistence.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public enum FileIO {
    INSTANCE;

    public void writeToFile(File file, String containment) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        writeToFileCommon(containment, fileOutputStream);
    }

    public void writeToFile(String fileName, String containment) throws IOException {
        File file = new File(fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        writeToFileCommon(containment, fileOutputStream);
    }

    private void writeToFileCommon(String containment, FileOutputStream fos) throws IOException {
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
