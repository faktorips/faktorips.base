package org.faktorips.devtools.htmlexport.helper.html;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;

public class HtmlUtil {

	public static String createDocFrame(String title, String colDefinition, String rowsDefinition) {
		StringBuilder builder = new StringBuilder();

		builder.append(createHtmlHead(title).replaceFirst("<body>", ""));
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

	public static String createHtmlHead(String title) {
		return createHtmlHead(title, null);
	}

	public static String createHtmlHead(String title, String styles) {
		StringBuilder builder = new StringBuilder();

		builder.append("<?xml version=\"1.0\" ?>\n");
		builder
				.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>");
		builder.append(title);
		builder.append("</title>");
		builder.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />");
		if (!StringUtils.isBlank(styles)) {
			builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			builder.append(styles);
			builder.append("\">");
		}

		builder.append("</head><body>");

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
		builder.append(prepareText(text));
		builder.append(createHtmlElementCloseTag(element));
		return builder.toString();
	}

	public static String createHtmlElement(String element, HtmlAttribute... attribute) {
		StringBuilder builder = createHtmlElementOpenTagBase(element, attribute);
		builder.append("/>");
		return builder.toString();
	}

	/**
	 * 
	 * @param text
	 * @return fuer HTML aufbereiteter Text
	 */
	public static String prepareText(String text) {
		return StringUtils.replace(text, "\n", "<br/>");
	}

	public static String createHtmlElementOpenTag(String element, String classes) {
		if (StringUtils.isBlank(classes)) {
			return createHtmlElementOpenTag(element, new HtmlAttribute[] {});
		}
		HtmlAttribute classesAttr = new HtmlAttribute("class", classes);
		return createHtmlElementOpenTag(element, classesAttr);
	}

	static String createHtmlElementOpenTag(String element, HtmlAttribute... attributes) {
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

	public static String createHtmlElementCloseTag(String element) {
		StringBuilder builder = new StringBuilder();
		builder.append("</");
		builder.append(element);
		builder.append('>');
		return builder.toString();
	}

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

	public static IpsElementPathUtil createIpsElementHtmlPathUtil(IIpsElement element) {
		return PathUtilFactory.createPathUtil(element);
	}

	/**
	 * gibt den relativen Link vom <code>IIpsElement</code> ins Root-Verzeichnis
	 * 
	 * @param ipsElement
	 * @return relativer Link ins Root-Verzeichnis
	 */
	public static String getPathToRoot(IIpsElement ipsElement) {
		IpsElementPathUtil pathUtil = PathUtilFactory.createPathUtil(ipsElement);
		return pathUtil.getPathToRoot();
	}

	/**
	 * gibt den relativen Link vom Root-Verzeichnis zum <code>IIpsElement</code>
	 * 
	 * @param ipsElement
	 * @return relativer Link vom Root-Verzeichnis
	 */
	public static String getPathFromRoot(IIpsElement ipsElement, LinkedFileTypes linkedFileType) {
		IpsElementPathUtil pathUtil = PathUtilFactory.createPathUtil(ipsElement);
		return pathUtil.getPathFromRoot(linkedFileType);
	}

	public static String createImage(String src, String alt) {
		HtmlAttribute[] attribute = new HtmlAttribute[] { new HtmlAttribute("src", src), new HtmlAttribute("alt", alt) };
		return createHtmlElement("img", attribute).toString();

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
		IpsElementPathUtil pathUtil = PathUtilFactory.createPathUtil(ipsElement);
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

	public static String createHtmlFoot() {
		return createHtmlElementCloseTag("body") + createHtmlElementCloseTag("html");
	}
}
