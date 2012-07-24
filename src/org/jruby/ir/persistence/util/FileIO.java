package org.jruby.ir.persistence.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import jline.internal.InputStreamReader;

public enum FileIO {
    INSTANCE;

    public static final Charset CHARSET = Charset.forName("UTF-8");

    public String readFile(File file) throws FileNotFoundException, IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return readFromStream(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public String readFromJar(String fileName) throws IOException {
        String before = fileName.substring("file:".length(), fileName.indexOf("!/"));
        String after = fileName.substring(fileName.indexOf("!/") + 2);

        JarFile jFile = new JarFile(before);
        JarEntry entry = jFile.getJarEntry(after);

        if (entry != null && !entry.isDirectory()) {
            InputStream is = null;
            try {
                is = jFile.getInputStream(entry);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, CHARSET));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                }                
                return builder.toString();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } else {
            throw new IOException();
        }
    }

    public String readFromStream(FileInputStream fis) throws IOException {
        FileChannel fc = fis.getChannel();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        return CHARSET.decode(bb).toString();
    }

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
