package org.jruby.homepage;

import org.apache.velocity.*;

public interface ContextPlugin extends Plugin {
	public void execute(VelocityContext context);
}