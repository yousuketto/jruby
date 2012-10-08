package org.jruby.ir.persistence.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {
    public static final FileIO INSTANCE = new FileIO();;

    private FileIO() { }

    public void writeToFile(final File file, final String content) throws IOException {
        writeToFileCommon(content, new BufferedWriter(new FileWriter(file)));
    }

    public void writeToFile(final String fileName, final String content) throws IOException {
        writeToFile(new File(fileName), content);
    }

    private void writeToFileCommon(final String content, final BufferedWriter writer) throws IOException {
        try {
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
