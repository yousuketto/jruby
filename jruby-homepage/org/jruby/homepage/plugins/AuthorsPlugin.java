package org.jruby.homepage.plugins;

import java.io.*;
import java.util.*;

import org.apache.velocity.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jruby.homepage.*;
import org.jruby.homepage.plugins.util.*;

public class AuthorsPlugin extends AbstractPlugin implements ContextPlugin {

	public List getAuthors() throws Exception {
	    List authors = new ArrayList();
	    
	    Document doc = new SAXBuilder().build(new BufferedReader(new FileReader(getProperty("file"))));
	    Iterator iter = doc.getRootElement().getChildren("author").iterator();
	    while (iter.hasNext()) {
	        Element author = (Element)iter.next();
	        
	        authors.add(new Author(author.getAttributeValue("name"), author.getAttributeValue("email")));
	    }
	    
	    return authors;
	}


    /*
     * @see ContextPlugin#execute(VelocityContext)
     */
    public void execute(VelocityContext context) {
        try {
            context.put("authors", getAuthors());
        } catch (Exception excptn) {
            excptn.printStackTrace();
        }
    }
}