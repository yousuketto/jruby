package org.jruby.homepage.plugins;

import java.io.*;
import java.util.*;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.jdom.*;
import org.jruby.homepage.*;

public class DefaultPage extends AbstractPlugin implements PagePlugin {

    /*
     * @see PagePlugin#createPage(Element)
     */
    public void createPage(Element element) {
        Iterator iter = element.getChildren("transform").iterator();
        while (iter.hasNext()) {
            VelocityContext context = new VelocityContext();

            Element transform = (Element) iter.next();
            Iterator contextIter = transform.getChildren("context").iterator();
            while (contextIter.hasNext()) {
                ContextPlugin contextPlugin = (ContextPlugin) getPlugins().loadPlugin((Element) contextIter.next());

                contextPlugin.execute(context);
            }

            InputPlugin inputPlugin = (InputPlugin) getPlugins().loadPlugin(transform.getChild("input"));
            OutputPlugin outputPlugin = (OutputPlugin) getPlugins().loadPlugin(transform.getChild("output"));

                try {
                    Velocity.init();
                    
                    Reader reader = inputPlugin.getReader();
                    Writer writer = outputPlugin.getWriter();
                    
                    Velocity.evaluate(context, writer, "DefaultPage", reader);
                    
                    writer.close();
                    reader.close();
                } catch (Exception excptn) {
                    excptn.printStackTrace();
                }
        }
    }
}