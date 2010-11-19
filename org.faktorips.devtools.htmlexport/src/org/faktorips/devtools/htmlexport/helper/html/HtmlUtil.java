/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;

/**
 * Utility for generating html
 * 
 * @author dicker
 * 
 */
public class HtmlUtil {
    private static final DateFormat META_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

    /**
     * returns complete html with a frameset-definition with two columns and in the left column two
     * rows like a javadoc-site.
     * 
     */
    public static String createDocFrame(String title, String colDefinition, String rowsDefinition) {
        StringBuilder builder = new StringBuilder();

        builder.append(createHtmlHead(title, null).replaceFirst("<body>", "")); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("<frameset cols=\""); //$NON-NLS-1$
        builder.append(colDefinition);
        builder.append("\"><frameset rows=\""); //$NON-NLS-1$
        builder.append(rowsDefinition);
        builder.append("\"><frame src=\"overview.html\" name=\"overview\"><frame src=\"classes.html\" name=\"classes\"></frameset><frame src=\"summary.html\" name=\"content\">"); //$NON-NLS-1$
        builder.append("<noframes><h2>Frame Alert</h2><p>This document is designed to be viewed only with the frames feature.</p></noframes>"); //$NON-NLS-1$
        builder.append("</frameset>"); //$NON-NLS-1$
        builder.append(createHtmlElementCloseTag("html")); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * returns the head including the opening body-tag
     * 
     * @param title title of the page
     * @param stylePath relative path to the css-definitions
     */
    public static String createHtmlHead(String title, String stylePath) {
        StringBuilder builder = new StringBuilder();

        builder.append("<?xml version=\"1.0\" ?>\n"); //$NON-NLS-1$
        builder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"); //$NON-NLS-1$
        builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>"); //$NON-NLS-1$
        builder.append(title);
        builder.append("</title>"); //$NON-NLS-1$
        builder.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />"); //$NON-NLS-1$
        builder.append("<meta name=\"date\" content=\"" + META_DATE_FORMAT.format(new Date()) + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!StringUtils.isBlank(stylePath)) {
            builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""); //$NON-NLS-1$
            builder.append(stylePath);
            builder.append("\" />"); //$NON-NLS-1$
        }

        builder.append("</head><body>"); //$NON-NLS-1$

        return builder.toString();
    }

    /**
     * creates complete html-element with text
     * 
     */
    public static String createHtmlElement(String element, String text, String classes) {
        StringBuilder builder = new StringBuilder();
        builder.append(createHtmlElementOpenTag(element, classes));
        builder.append(getHtmlText(text));
        builder.append(createHtmlElementCloseTag(element));
        return builder.toString();
    }

    /**
     * creates complete html-element without text
     * 
     */
    public static String createHtmlElement(String element, HtmlAttribute... attribute) {
        StringBuilder builder = createHtmlElementOpenTagBase(element, attribute);
        builder.append("/>"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * returns the text adapted for html
     */
    public static String getHtmlText(String text) {
        String adaptedText = text;
        adaptedText = StringUtils.replace(adaptedText, "<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        adaptedText = StringUtils.replace(adaptedText, ">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        return StringUtils.replace(adaptedText, "\n", "\n<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * creates opening tag
     * 
     */
    public static String createHtmlElementOpenTag(String element, String classes) {
        if (StringUtils.isBlank(classes)) {
            return createHtmlElementOpenTag(element, new HtmlAttribute[] {});
        }
        HtmlAttribute classesAttr = new HtmlAttribute("class", classes); //$NON-NLS-1$
        return createHtmlElementOpenTag(element, classesAttr);
    }

    /**
     * creates opening tag with given html-attributes
     * 
     */
    public static String createHtmlElementOpenTag(String element, HtmlAttribute... attributes) {
        StringBuilder builder = createHtmlElementOpenTagBase(element, attributes);
        builder.append('>');
        return builder.toString();
    }

    private static StringBuilder createHtmlElementOpenTagBase(String element, HtmlAttribute... attributes) {
        StringBuilder builder = new StringBuilder();
        builder.append('\n');
        builder.append('<');
        builder.append(element);
        for (HtmlAttribute attribute : attributes) {
            if (attribute == null) {
                continue;
            }
            builder.append(' ');
            builder.append(attribute.getName());
            builder.append("=\""); //$NON-NLS-1$
            builder.append(attribute.getValue());
            builder.append("\""); //$NON-NLS-1$
        }
        return builder;
    }

    /**
     * creates closing tag
     */
    public static String createHtmlElementCloseTag(String element) {
        StringBuilder builder = new StringBuilder();
        builder.append("</"); //$NON-NLS-1$
        builder.append(element);
        builder.append('>');
        return builder.toString();
    }

    /**
     * creates opening link-tag
     * 
     */
    public static String createLinkOpenTag(String href, String target, String classes) {
        HtmlAttribute hrefAttr = new HtmlAttribute("href", href); //$NON-NLS-1$
        HtmlAttribute targetAttr = (target == null ? null : new HtmlAttribute("target", target)); //$NON-NLS-1$

        StringBuilder builder = new StringBuilder();

        if (StringUtils.isBlank(classes)) {
            builder.append(createHtmlElementOpenTag("a", hrefAttr, targetAttr)); //$NON-NLS-1$
            return builder.toString();
        }

        HtmlAttribute classAttr = new HtmlAttribute("class", classes); //$NON-NLS-1$
        builder.append(createHtmlElementOpenTag("a", hrefAttr, classAttr, targetAttr)); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * returns relative link from root to the page for the <code>IIpsElement</code>
     * 
     */
    public static String getPathFromRoot(IIpsElement ipsElement, LinkedFileType linkedFileType) {
        IpsElementPathUtil pathUtil = PathUtilFactory.createPathUtil(ipsElement);
        return pathUtil.getPathFromRoot(linkedFileType) + ".html"; //$NON-NLS-1$
    }

    /**
     * creates tag for an image
     * 
     */
    public static String createImage(String src, String alt) {
        HtmlAttribute[] attribute = new HtmlAttribute[] { new HtmlAttribute("src", src), new HtmlAttribute("alt", alt) }; //$NON-NLS-1$ //$NON-NLS-2$
        return createHtmlElement("img", attribute).toString(); //$NON-NLS-1$

    }

    /**
     * creates foot of the html-page beginning with the closing body-tag
     * 
     */
    public static String createHtmlFoot() {
        return createHtmlElementCloseTag("body") + createHtmlElementCloseTag("html"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
