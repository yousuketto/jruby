package org.jruby.homepage.plugins;

import java.io.*;

import java.util.*;

import org.apache.velocity.*;
import org.apache.velocity.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jruby.homepage.*;
import org.jruby.homepage.plugins.*;

public class MenuPlugin extends AbstractPlugin implements ContextPlugin {
    private String id;

    private boolean isPageInPath(Element section, String pageId) {
        if (section.getAttributeValue("id").equals(pageId)) {
            return true;
        }
        Iterator iter = section.getChildren("section").iterator();
        while (iter.hasNext()) {
            Element subsection = (Element) iter.next();
            if (isPageInPath(subsection, pageId)) {
                return true;
            }
        }

        return false;
    }

    private Section getMenuSectionFromXML(Element section, String pageId, boolean show) {
        Section newSection = new Section();

        newSection.setName(section.getAttributeValue("name"));
        newSection.setId(section.getAttributeValue("id"));
        if (!pageId.equals(section.getAttributeValue("id"))) {
            newSection.setLink(section.getAttributeValue("link"));
        }
        newSection.setChilds(null);

        List children = section.getChildren("section");

        if (!children.isEmpty()) {
            newSection.setChilds(new LinkedList());

            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                Element subsection = (Element) iter.next();
                if (show || section.getAttributeValue("id").equals(pageId)) {
                    newSection.getChilds().add(getMenuSectionFromXML(subsection, pageId, isPageInPath(subsection, pageId)));
                }
            }
        }

        return newSection;
    }

    public List getMenuSections(Reader reader, String pageId) {
        try {
            Document doc = new SAXBuilder().build(reader);

            Element root = doc.getRootElement();

            ArrayList menuSections = new ArrayList();

            Iterator iter = root.getChildren("section").iterator();
            while (iter.hasNext()) {
                Element section = (Element) iter.next();
                menuSections.add(getMenuSectionFromXML(section, pageId, true));
            }

            return menuSections;
        } catch (JDOMException jdomExcptn) {
            jdomExcptn.printStackTrace();
            return null;
        }
    }

    /*
     * @see ContextPlugin#execute(VelocityContext)
     */
    public void execute(VelocityContext context) {
		context.put("menu", getMenuSections(((InputPlugin)getPlugins().getPlugin(getProperty("input"))).getReader(), getProperty("pageId")));
    }
}