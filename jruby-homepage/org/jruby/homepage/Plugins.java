package org.jruby.homepage;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.*;

public class Plugins {
    private Map plugins = new HashMap();

    public void putPlugin(String id, Plugin plugin) {
        plugins.put(id, plugin);
    }

    public Plugin getPlugin(String id) {
        return (Plugin)plugins.get(id);
    }

    public Plugin loadPlugin(String name) {
        // Plugin plugin = loadPluginFromClassName(name);
        // if (plugin == null) {
            Plugin plugin = loadPluginFromClassName("org.jruby.homepage.plugins." + name);
        // }
        return plugin;
    }

    public Plugin loadPlugin(Element element) {
        if (element.getAttributeValue("plugin") != null) {
            Plugin plugin = loadPlugin(element.getAttributeValue("plugin"));
            if (plugin != null) {
                Iterator iter = element.getAttributes().iterator();
                while (iter.hasNext()) {
                    Attribute attribute = (Attribute) iter.next();
                    if (!attribute.getName().equals("plugin") && !attribute.getName().equals("id")) {
                        plugin.putProperty(attribute.getName(), attribute.getValue());
                    } else if (attribute.getName().equals("id")) {
                        putPlugin(attribute.getValue(), plugin);
                    }
                }
	            return plugin;
            }
        } else if (element.getAttributeValue("id") != null) {
            Plugin plugin = getPlugin(element.getAttributeValue("id"));
            if (plugin != null) {
                Iterator iter = element.getAttributes().iterator();
                while (iter.hasNext()) {
                    Attribute attribute = (Attribute) iter.next();
                    if (!attribute.getName().equals("plugin") && !attribute.getName().equals("id")) {
                        plugin.putProperty(attribute.getName(), attribute.getValue());
                    } else if (attribute.getName().equals("id")) {
                        putPlugin(attribute.getValue(), plugin);
                    }
                }
	            return plugin;
            }
        }
        return null;
    }

    private Plugin loadPluginFromClassName(String name) {
        try {
            Class pluginClass = Class.forName(name);
            Plugin plugin = (Plugin) pluginClass.newInstance();
            plugin.setPlugins(this);
            return plugin;
        } catch (ClassNotFoundException cnfExcptn) {
            cnfExcptn.printStackTrace();
        } catch (IllegalAccessException iaExcptn) {
            iaExcptn.printStackTrace();
        } catch (InstantiationException iExcptn) {
            iExcptn.printStackTrace();
        }
        return null;
    }
}