package org.jruby.homepage;

import java.io.*;

import java.util.*;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.parser.*;
import org.jdom.*;
import org.jdom.input.*;

public class PageGenerator {

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

    public static void main(String[] args) {
        String templateFile = args[0];
        String pageFile = args[1];
        String menuFile = args[2];
        String outputFile = args[3];

        try {
            BufferedReader reader = new BufferedReader(new FileReader(pageFile));

            try {
                Document doc = new SAXBuilder().build(reader);

                Element root = doc.getRootElement();

                Iterator iter = root.getChildren("page").iterator();
                while (iter.hasNext()) {
                    Element pageElement = (Element) iter.next();

                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile + pageElement.getAttributeValue("id") + ".html"));
                    BufferedReader menuReader = new BufferedReader(new FileReader(menuFile));

                    new PageGenerator().generate(root, pageElement, templateFile, menuReader, writer);

                    writer.close();
                    menuReader.close();
                }
            } catch (JDOMException jdomExcptn) {
                jdomExcptn.printStackTrace();
            }

            reader.close();
        } catch (IOException ioExcptn) {
            ioExcptn.printStackTrace();
        }
    }

    public void generate(
        Element generalElement,
        Element pageElement,
        String templateFile,
        Reader menuReader,
        Writer writer) {
        try {
            Velocity.init("velocity.properties");

            VelocityContext context = new VelocityContext();

            context.put("menu", getMenuSections(menuReader, pageElement.getAttributeValue("id")));
            context.put("pageId", pageElement.getAttributeValue("id"));

            ArrayList authors = new ArrayList();
            Iterator iter = generalElement.getChildren("authors").iterator();
            while (iter.hasNext()) {
                Element author = (Element) iter.next();
                authors.add(
                    new Person(author.getAttributeValue("name"), author.getAttributeValue("email"), author.getAttributeValue("role")));
            }
            context.put("authors", authors);
            context.put("copyright", generalElement.getAttributeValue("copyright"));

            context.put("title", pageElement.getAttributeValue("title"));
            context.put("content", pageElement.getAttributeValue("id") + ".inc.html");

            Template template = null;

            try {
                template = Velocity.getTemplate(templateFile);
            } catch (ResourceNotFoundException rnfExcptn) {
                System.err.println("Cannot get template file: " + templateFile);
            } catch (ParseException pExcptn) {
                System.err.println("Syntax error in template: " + templateFile + " : " + pExcptn);
            }

            if (template != null) {
                template.merge(context, writer);
            }

        } catch (Exception excptn) {
            excptn.printStackTrace();
        }
    }
}