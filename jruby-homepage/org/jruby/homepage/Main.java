package org.jruby.homepage;

import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jruby.homepage.plugins.*;

public class Main {
    public static void main(String[] args) throws JDOMException {
        Plugins plugins = new Plugins();
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(args[0]);
        
        Element root = doc.getRootElement();
        
        if (!root.getName().equals("homepage")) {
            System.err.println("root element is not \"homepage\"");
        }

        Iterator iter = root.getChildren("plugin").iterator();
        while (iter.hasNext()) {
            Element pluginElement = (Element) iter.next();
            
            plugins.loadPlugin(pluginElement);
        }

        iter = root.getChildren("page").iterator();
        while (iter.hasNext()) {
            Element pageElement = (Element) iter.next();
            
            PagePlugin plugin = (PagePlugin)plugins.loadPlugin(pageElement);
            plugin.createPage(pageElement);
        }
    }
}