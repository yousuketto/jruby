package org.jruby.homepage;

import org.jdom.*;

public interface PagePlugin extends Plugin {
	public void createPage(Element element);
}

