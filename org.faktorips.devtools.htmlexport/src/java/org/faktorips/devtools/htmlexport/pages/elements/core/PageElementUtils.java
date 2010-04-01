package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * Utility for {@link PageElement}s
 * @author dicker
 *
 */
public class PageElementUtils {

	/**
	 * creates {@link PageElement}s from the given {@link String}s with {@link Style}s and {@link TextType}s
	 * @param texts
	 * @param styles
	 * @param type
	 * @return array of {@link PageElement}s
	 */
	public static PageElement[] createTextPageElements(List<String> texts, Set<Style> styles, TextType type) {
		PageElement[] textPageElements = new PageElement[texts.size()];

		for (int i = 0; i < textPageElements.length; i++) {
			textPageElements[i] = new TextPageElement(texts.get(i), styles, type);
		}

		return textPageElements;
	}

	/**
	 * creates {@link PageElement}s from the given {@link String}s
	 * @param texts
	 * @return array of {@link PageElement}s
	 */
	public static PageElement[] createTextPageElements(List<String> texts) {
		return createTextPageElements(texts, null, TextType.WITHOUT_TYPE);
	}

	/**
	 * creates a {@link List} with link to the given {@link IIpsObject}s with the given target and {@link Style}s
	 * @param objects
	 * @param target
	 * @param styles
	 * @return {@link List} of {@link LinkPageElement}s
	 */
	public static List<LinkPageElement> createLinkPageElements(List<? extends IIpsObject> objects, String target, Set<Style> styles) {
		List<LinkPageElement> liste = new ArrayList<LinkPageElement>();

		for (IIpsObject object : objects) {
			LinkPageElement linkPageElement = new LinkPageElement(object, target, object.getName(), true);
			linkPageElement.addStyles(styles.toArray(new Style[styles.size()]));
			liste.add(linkPageElement);
		}
		
		return liste;
	}
}
