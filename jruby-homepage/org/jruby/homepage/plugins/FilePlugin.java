package org.jruby.homepage.plugins;

import java.io.*;

import java.io.*;
import org.jruby.homepage.*;
import org.jruby.homepage.plugins.*;

public class FilePlugin extends AbstractPlugin implements InputPlugin, OutputPlugin {
    /*
     * @see InputPlugin#getReader()
     */
    public Reader getReader() {
        try {
        	return new BufferedReader(new FileReader(getProperty("file")));
        } catch (IOException ioExcptn) {
            ioExcptn.printStackTrace();
            return null;
        }
    }
    /*
     * @see OutputPlugin#getWriter()
     */
    public Writer getWriter() {
        try {
        	return new BufferedWriter(new FileWriter(getProperty("file")));
        } catch (IOException ioExcptn) {
            ioExcptn.printStackTrace();
            return null;
        }
    }
}