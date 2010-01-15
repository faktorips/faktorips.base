package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

public class PageElementUtils {

	public static TextPageElement[] createTextPageElements(List<String> texts, Set<Style> styles, TextType type) {
		TextPageElement[] textPageElements = new TextPageElement[texts.size()];

		for (int i = 0; i < textPageElements.length; i++) {
			textPageElements[i] = new TextPageElement(texts.get(i), styles, type);
		}

		return textPageElements;
	}

	public static TextPageElement[] createTextPageElements(List<String> texts) {
		return createTextPageElements(texts, null, TextType.WITHOUT_TYPE);
	}

	public static List<LinkPageElement> createLinkPageElements(IIpsObject srcObject, List<? extends IIpsObject> objects, String target, Set<Style> styles) {
		List<LinkPageElement> liste = new ArrayList<LinkPageElement>();

		for (IIpsObject object : objects) {
			LinkPageElement linkPageElement = new LinkPageElement(srcObject, object, target, new TextPageElement(object.getName()));
			linkPageElement.addStyles(styles.toArray(new Style[styles.size()]));
			liste.add(linkPageElement);
		}
		
		return liste;
	}
}
