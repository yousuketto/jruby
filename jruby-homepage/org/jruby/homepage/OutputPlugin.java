package org.jruby.homepage;

import java.io.*;

public interface OutputPlugin extends Plugin {
	public Writer getWriter();
}