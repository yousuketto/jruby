package org.jruby.homepage;

public interface Plugin {
	public void putProperty(String key, String value);
	public void setPlugins(Plugins plugins);
}

