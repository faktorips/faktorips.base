package org.faktorips.devtools.htmlexport.helper.html;

import java.util.Collection;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.helper.html.path.HtmlPathUtilFactory;
import org.faktorips.devtools.htmlexport.helper.html.path.IpsElementHtmlPathUtil;
import org.faktorips.devtools.htmlexport.helper.html.path.LinkedFileTypes;

public class HtmlUtil {

    public static String createDocFrame(String title, String colDefinition, String rowsDefinition) {
        StringBuilder builder = new StringBuilder();

        builder.append(createHtmlHead(title));
        builder.append("<frameset cols=\"");
        builder.append(colDefinition);
        builder.append("\"><frameset rows=\"");
        builder.append(rowsDefinition);
        builder
                .append("\"><frame src=\"overview.html\" name=\"overview\"><frame src=\"classes.html\" name=\"classes\"></frameset><frame src=\"summary.html\" name=\"content\">");
        builder.append("<noframes><h2>Frame Alert</h2><p>This document is designed to be viewed only with the frames feature.</p></noframes>");
        builder.append("</frameset>");
        builder.append(createHtmlElementCloseTag("html"));
        return builder.toString();
    }

    public static String createHtmlHead(String title) {
        StringBuilder builder = new StringBuilder();

        builder
                .append("<?xml version=\"1.0\" ?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>");
        builder.append(title);
        builder.append("</title></head>");

        return builder.toString();
    }

    public static String createList(Collection<String> items, String listClasses, String itemClasses) {
        if (items.size() == 0)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append(createHtmlElementOpenTag("ul", listClasses));

        for (String string : items) {
            builder.append(createHtmlElement("li", string, itemClasses));
        }

        builder.append(createHtmlElementCloseTag("ul"));

        return builder.toString();
    }

    public static String createHtmlElement(String node, String text) {
        return createHtmlElement(node, text, null);
    }

    public static String createHtmlElement(String element, String text, String classes) {
        StringBuilder builder = new StringBuilder();
        builder.append(createHtmlElementOpenTag(element, classes));
        builder.append(text);
        builder.append(createHtmlElementCloseTag(element));
        return builder.toString();
    }

    public static String createHtmlElementOpenTag(String element, String classes) {
        if (classes == null || classes.trim().equals("")) {
            return createHtmlElementOpenTag(element, new HtmlAttribute[] {});
        }
        HtmlAttribute classesAttr = new HtmlAttribute("class", classes);
        return createHtmlElementOpenTag(element, classesAttr);
    }

    static String createHtmlElementOpenTag(String element, HtmlAttribute... attributes) {
        StringBuilder builder = new StringBuilder();
        builder.append('\n');
        builder.append('<');
        builder.append(element);
        for (HtmlAttribute attribute : attributes) {
            builder.append(' ');
            builder.append(attribute.getName());
            builder.append("=\"");
            builder.append(attribute.getValue());
            builder.append("\"");
        }
        builder.append('>');
        return builder.toString();
    }

    public static String createHtmlElementCloseTag(String element) {
        StringBuilder builder = new StringBuilder();
        builder.append("</");
        builder.append(element);
        builder.append('>');
        return builder.toString();
    }

    public static String createLink(String href, String text, String alt, String target, String classes) {
        HtmlAttribute hrefAttr = new HtmlAttribute("href", href);
        HtmlAttribute altAttr = new HtmlAttribute("alt", alt);
        HtmlAttribute classAttr = new HtmlAttribute("class", classes);
        HtmlAttribute targetAttr = new HtmlAttribute("target", target);

        StringBuilder builder = new StringBuilder();
        builder.append(createHtmlElementOpenTag("a", hrefAttr, altAttr, classAttr, targetAttr));
        builder.append(text);
        builder.append(createHtmlElementCloseTag("a"));

        return builder.toString();
    }

    public static IpsElementHtmlPathUtil createIpsElementHtmlPathUtil(IIpsElement element) {
        return HtmlPathUtilFactory.createPathUtil(element);
    }

    /**
     * gibt den relativen Link vom <code>IIpsElement</code> ins Root-Verzeichnis
     * 
     * @param ipsElement
     * @return relativer Link ins Root-Verzeichnis
     */
    public static String getPathToRoot(IIpsElement ipsElement) {
        IpsElementHtmlPathUtil pathUtil = HtmlPathUtilFactory.createPathUtil(ipsElement);
        return pathUtil.getPathToRoot();
    }

    /**
     * gibt den relativen Link vom Root-Verzeichnis zum <code>IIpsElement</code>
     * 
     * @param ipsElement
     * @return relativer Link vom Root-Verzeichnis
     */
    public static String getPathFromRoot(IIpsElement ipsElement, LinkedFileTypes linkedFileType) {
        IpsElementHtmlPathUtil pathUtil = HtmlPathUtilFactory.createPathUtil(ipsElement);
        return pathUtil.getPathFromRoot(linkedFileType);
    }

    /**
     * Name des IIPSElement im Link
     * 
     * @param ipsElement
     * @param withImage
     *            true: Darstellung mit Thumb
     * @return Html-Link-Text
     */
    public static String getLinkName(IIpsElement ipsElement, boolean withImage) {
        IpsElementHtmlPathUtil pathUtil = HtmlPathUtilFactory.createPathUtil(ipsElement);
        return pathUtil.getLinkText(withImage);
    }

    public static String createLinkBase(IIpsElement from, IIpsElement to, LinkedFileTypes linkedFileType) {
        return getPathToRoot(from) + getPathFromRoot(to, linkedFileType);
    }
    
    public static String createHtmlTable(String[][] cells, String tableClasses, String cellClasses) {
        HtmlTable table = new HtmlTable(cells, tableClasses, cellClasses);
        
        return table.generate();
    }
    
    public static String getHtmlText(String text) {
        return text.replaceAll("\n", "\n<br/>");
    }
}
