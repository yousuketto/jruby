package org.jruby.homepage.plugins;

import java.util.*;
import org.jruby.homepage.*;

public class Section {
	private String name;
	private String id;
	private String link;
	private List childs;

    /**
     * Gets the childs.
     * @return Returns a List
     */
    public List getChilds() {
        return childs;
    }

    /**
     * Sets the childs.
     * @param childs The childs to set
     */
    public void setChilds(List childs) {
        this.childs = childs;
    }

    /**
     * Gets the link.
     * @return Returns a String
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the link.
     * @param link The link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Gets the name.
     * @return Returns a String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Gets the id.
     * @return Returns a String
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

}