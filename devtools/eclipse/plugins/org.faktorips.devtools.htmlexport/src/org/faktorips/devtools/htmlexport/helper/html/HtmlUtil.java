/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.IHtmlPath;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Utility for generating html
 * 
 * @author dicker
 * 
 */
public class HtmlUtil {
    private final DateFormat metaDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

    /**
     * returns complete html with a frameset-definition with two columns and in the left column two
     * rows like a javadoc-site.
     * 
     */
    public String createDocFrame(String title, String colDefinition, String rowsDefinition) {
        StringBuilder builder = new StringBuilder();

        builder.append(createHtmlHead(title, null, false).replaceFirst("<body>", "")); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("<frameset cols=\""); //$NON-NLS-1$
        builder.append(colDefinition);
        builder.append("\"><frameset rows=\""); //$NON-NLS-1$
        builder.append(rowsDefinition);
        builder.append(
                "\"><frame src=\"overview.html\" name=\"overview\" /><frame src=\"classes.html\" name=\"classes\" /></frameset><frame src=\"summary.html\" name=\"content\" />"); //$NON-NLS-1$
        builder.append(
                "<noframes><h2>Frame Alert</h2><p>This document is designed to be viewed only with the frames feature.</p></noframes>"); //$NON-NLS-1$
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
    public String createHtmlHead(String title, String stylePath, boolean moveTitle) {
        StringBuilder builder = new StringBuilder();

        builder.append("<?xml version=\"1.0\" ?>\n"); //$NON-NLS-1$
        builder.append(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"); //$NON-NLS-1$
        builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>"); //$NON-NLS-1$
        builder.append(title);
        builder.append("</title>"); //$NON-NLS-1$
        builder.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />"); //$NON-NLS-1$
        builder.append("<meta name=\"date\" content=\"" + metaDateFormat.format(new Date()) + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!IpsStringUtils.isBlank(stylePath)) {
            builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""); //$NON-NLS-1$
            builder.append(stylePath);
            builder.append("\" />"); //$NON-NLS-1$
        }

        if (moveTitle) {
            builder.append("\n<script type=\"text/javascript\">"); //$NON-NLS-1$
            builder.append("\nfunction setWindowTitle() {"); //$NON-NLS-1$
            builder.append("\n    parent.document.title=\""); //$NON-NLS-1$
            builder.append(title);
            builder.append("\";\n}\n</script>\n"); //$NON-NLS-1$
        }
        builder.append("</head><body"); //$NON-NLS-1$
        if (moveTitle) {
            builder.append(" onload=\"setWindowTitle();\""); //$NON-NLS-1$
        }
        builder.append(">"); //$NON-NLS-1$

        return builder.toString();
    }

    /**
     * creates complete html-element with text
     * 
     */
    public String createHtmlElement(String element, String text, String classes) {
        return createHtmlElement(element, null, text, classes);
    }

    private String createHtmlElement(String element, String id, String text, String classes) {
        StringBuilder builder = new StringBuilder();
        builder.append(createHtmlElementOpenTag(element, id, classes));
        builder.append(getHtmlText(text));
        builder.append(createHtmlElementCloseTag(element));
        return builder.toString();
    }

    /**
     * creates complete html-element without text
     * 
     */
    public String createHtmlElement(String element, List<HtmlAttribute> attribute) {
        StringBuilder builder = createHtmlElementOpenTagBase(element, attribute);
        builder.append("/>"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * returns the text adapted for html
     */
    public String getHtmlText(String text) {
        String adaptedText = text;
        adaptedText = StringUtils.replace(adaptedText, "<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        adaptedText = StringUtils.replace(adaptedText, ">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        return StringUtils.replace(adaptedText, "\n", "\n<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * creates opening tag with classes
     * 
     */
    public String createHtmlElementOpenTag(String element, String id, String classes) {
        List<HtmlAttribute> attributes = new ArrayList<>();

        if (IpsStringUtils.isNotBlank(classes)) {
            attributes.add(new HtmlAttribute("class", classes)); //$NON-NLS-1$
        }

        if (IpsStringUtils.isNotBlank(id)) {
            attributes.add(new HtmlAttribute("id", id)); //$NON-NLS-1$
        }

        return createHtmlElementOpenTag(element, attributes);
    }

    public String createHtmlElementOpenTag(String element) {
        StringBuilder builder = createHtmlElementOpenTagBase(element, Collections.<HtmlAttribute> emptyList());
        builder.append('>');
        return builder.toString();
    }

    /**
     * creates opening tag with given html-attributes
     * 
     */
    public String createHtmlElementOpenTag(String element, List<HtmlAttribute> attributes) {
        StringBuilder builder = createHtmlElementOpenTagBase(element, attributes);
        builder.append('>');
        return builder.toString();
    }

    private StringBuilder createHtmlElementOpenTagBase(String element, List<HtmlAttribute> attributes) {
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
    public String createHtmlElementCloseTag(String element) {
        StringBuilder builder = new StringBuilder();
        builder.append("</"); //$NON-NLS-1$
        builder.append(element);
        builder.append('>');
        return builder.toString();
    }

    public String createLinkOpenTag(String href, String anchor, String target, String classes, String title) {
        List<HtmlAttribute> attributes = new ArrayList<>();

        if (IpsStringUtils.isBlank(anchor)) {
            attributes.add(new HtmlAttribute("href", href)); //$NON-NLS-1$
        } else {
            attributes.add(new HtmlAttribute("href", href + "#" + anchor)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (IpsStringUtils.isNotBlank(target)) {
            attributes.add(new HtmlAttribute("target", target)); //$NON-NLS-1$
        }

        if (IpsStringUtils.isNotBlank(title)) {
            attributes.add(new HtmlAttribute("title", title)); //$NON-NLS-1$
        }

        if (IpsStringUtils.isNotBlank(classes)) {
            attributes.add(new HtmlAttribute("class", classes)); //$NON-NLS-1$
        }

        return createHtmlElementOpenTag("a", attributes); //$NON-NLS-1$
    }

    /**
     * returns relative link from root to the page for the <code>IIpsElement</code>
     * 
     */
    public String getPathFromRoot(IIpsElement ipsElement, LinkedFileType linkedFileType) {
        IHtmlPath pathUtil = HtmlPathFactory.createPathUtil(ipsElement);
        return pathUtil.getPathFromRoot(linkedFileType) + ".html"; //$NON-NLS-1$
    }

    /**
     * returns relative link from root to the page for the <code>IpsObjectType</code>
     * 
     */
    public String getPathFromRoot(IpsObjectType ipsObjectType, LinkedFileType linkedFileType) {
        IHtmlPath pathUtil = HtmlPathFactory.createPathUtil(ipsObjectType);
        return pathUtil.getPathFromRoot(linkedFileType) + ".html"; //$NON-NLS-1$
    }

    /**
     * creates tag for an image
     * 
     */
    public String createImage(String src, String alt) {
        List<HtmlAttribute> attributes = new ArrayList<>();
        attributes.add(new HtmlAttribute("src", src)); //$NON-NLS-1$
        attributes.add(new HtmlAttribute("alt", alt)); //$NON-NLS-1$
        return createHtmlElement("img", attributes); //$NON-NLS-1$

    }

    /**
     * creates foot of the html-page beginning with the closing body-tag
     * 
     */
    public String createHtmlFoot() {
        return createHtmlElementCloseTag("body") + createHtmlElementCloseTag("html"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * creates an anchor
     */
    public String createAnchor(String id) {
        return "<a id=\"" + id + "\"/>"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
