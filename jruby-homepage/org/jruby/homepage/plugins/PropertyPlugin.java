package org.jruby.homepage.plugins;

import org.apache.velocity.*;
import org.jruby.homepage.*;

public class PropertyPlugin extends AbstractPlugin implements ContextPlugin {

    /*
     * @see ContextPlugin#execute(VelocityContext)
     */
    public void execute(VelocityContext context) {
        context.put(getProperty("key"), getProperty("value"));
    }
}
