package org.jruby.homepage.plugins;

import java.util.*;
import org.jruby.homepage.*;

public abstract class AbstractPlugin implements Plugin {
    private Map properties = new HashMap();
    private Plugins plugins = null;

    /*
     * @see Plugin#putProperty(String, String)
     */
    public void putProperty(String key, String value) {
        properties.put(key, value);
    }
    
    public String getProperty(String key) {
        return (String)properties.get(key);
    }

    /*
     * @see Plugin#setPlugins(Plugins)
     */
    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }
    
    protected Plugins getPlugins() {
        return plugins;
    }
}