package org.faktorips.devtools.htmlexport.helper.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;

/**
 * Utility for generating html
 * @author dicker
 *
 */
public class HtmlUtil {
	private static final DateFormat META_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	
	
	/**
	 * returns complete html with a frameset-definition with two columns and in the left column two rows like a javadoc-site.
	 * @param title
	 * @param colDefinition
	 * @param rowsDefinition
	 * @return
	 */
	public static String createDocFrame(String title, String colDefinition, String rowsDefinition) {
		StringBuilder builder = new StringBuilder();

		builder.append(createHtmlHead(title, null).replaceFirst("<body>", ""));
		builder.append("<frameset cols=\"");
		builder.append(colDefinition);
		builder.append("\"><frameset rows=\"");
		builder.append(rowsDefinition);
		builder
				.append("\"><frame src=\"overview.html\" name=\"overview\"><frame src=\"classes.html\" name=\"classes\"></frameset><frame src=\"summary.html\" name=\"content\">");
		builder
				.append("<noframes><h2>Frame Alert</h2><p>This document is designed to be viewed only with the frames feature.</p></noframes>");
		builder.append("</frameset>");
		builder.append(createHtmlElementCloseTag("html"));
		return builder.toString();
	}

	/**
	 * returns the head including the opening body-tag
	 * @param title
	 * @param stylePath relative path to the css-definitions
	 * @return
	 */
	public static String createHtmlHead(String title, String stylePath) {
		StringBuilder builder = new StringBuilder();

		builder.append("<?xml version=\"1.0\" ?>\n");
		builder
				.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>");
		builder.append(title);
		builder.append("</title>");
		builder.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />");
		builder.append("<meta name=\"date\" content=\"" + META_DATE_FORMAT.format(new Date()) + "\" />");
		if (!StringUtils.isBlank(stylePath)) {
			builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			builder.append(stylePath);
			builder.append("\" />");
		}

		builder.append("</head><body>");

		return builder.toString();
	}

	/**
	 * creates an list
	 * @param items
	 * @param listClasses
	 * @param itemClasses
	 * @return
	 */
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

	/**
	 * creates complete html-element with text
	 * @param element
	 * @param text
	 * @param classes
	 * @return
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
	 * @param element
	 * @param attribute
	 * @return
	 */
	public static String createHtmlElement(String element, HtmlAttribute... attribute) {
		StringBuilder builder = createHtmlElementOpenTagBase(element, attribute);
		builder.append("/>");
		return builder.toString();
	}

	/**
	 * returns the text adapted for html
	 * @param text
	 * @return 
	 */
	public static String getHtmlText(String text) {
		return StringUtils.replace(text, "\n", "\n<br/>");
	}

	/**
	 * creates opening tag
	 * @param element
	 * @param classes
	 * @return
	 */
	public static String createHtmlElementOpenTag(String element, String classes) {
		if (StringUtils.isBlank(classes)) {
			return createHtmlElementOpenTag(element, new HtmlAttribute[] {});
		}
		HtmlAttribute classesAttr = new HtmlAttribute("class", classes);
		return createHtmlElementOpenTag(element, classesAttr);
	}

	/**
	 * creates opening tag with given html-attributes
	 * @param element
	 * @param attributes
	 * @return
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
			if (attribute == null)
				continue;
			builder.append(' ');
			builder.append(attribute.getName());
			builder.append("=\"");
			builder.append(attribute.getValue());
			builder.append("\"");
		}
		return builder;
	}

	/**
	 * creates closing tag
	 * @param element
	 * @return
	 */
	public static String createHtmlElementCloseTag(String element) {
		StringBuilder builder = new StringBuilder();
		builder.append("</");
		builder.append(element);
		builder.append('>');
		return builder.toString();
	}

	/**
	 * creates opening link-tag
	 * @param href
	 * @param target
	 * @param classes
	 * @return
	 */
	public static String createLinkOpenTag(String href, String target, String classes) {
		HtmlAttribute hrefAttr = new HtmlAttribute("href", href);
		HtmlAttribute targetAttr = (target == null ? null : new HtmlAttribute("target", target));

		StringBuilder builder = new StringBuilder();

		if (StringUtils.isBlank(classes)) {
			builder.append(createHtmlElementOpenTag("a", hrefAttr, targetAttr));
			return builder.toString();
		}

		HtmlAttribute classAttr = new HtmlAttribute("class", classes);
		builder.append(createHtmlElementOpenTag("a", hrefAttr, classAttr, targetAttr));
		return builder.toString();
	}

	/**
	 * returns relative link from root to the page for the <code>IIpsElement</code>
	 * 
	 * @param ipsElement
	 * @return 
	 */
	public static String getPathFromRoot(IIpsElement ipsElement, LinkedFileType linkedFileType) {
		IpsElementPathUtil pathUtil = PathUtilFactory.createPathUtil(ipsElement);
		return pathUtil.getPathFromRoot(linkedFileType) + ".html";
	}

	/**
	 * creates tag for an image
	 * @param src
	 * @param alt
	 * @return
	 */
	public static String createImage(String src, String alt) {
		HtmlAttribute[] attribute = new HtmlAttribute[] { new HtmlAttribute("src", src), new HtmlAttribute("alt", alt) };
		return createHtmlElement("img", attribute).toString();

	}

	/**
	 * returns name of the {@link IIpsElement} for a link
	 * 
	 * @param ipsElement
	 * @param withImage
	 *            true: link includes a small image which represents the type of the {@link IpsElement}
	 * @return 
	 */
	public static String getLinkName(IIpsElement ipsElement, boolean withImage) {
		IpsElementPathUtil pathUtil = PathUtilFactory.createPathUtil(ipsElement);
		return pathUtil.getLinkText(withImage);
	}

	
	/**
	 * creates complete html-table
	 * @param cells
	 * @param tableClasses
	 * @param cellClasses
	 * @return
	 */
	public static String createHtmlTable(String[][] cells, String tableClasses, String cellClasses) {
		HtmlTable table = new HtmlTable(cells, tableClasses, cellClasses);

		return table.generate();
	}

	/**
	 * creates foot of the html-page beginning with the closing body-tag
	 * @return
	 */
	public static String createHtmlFoot() {
		return createHtmlElementCloseTag("body") + createHtmlElementCloseTag("html");
	}
}
