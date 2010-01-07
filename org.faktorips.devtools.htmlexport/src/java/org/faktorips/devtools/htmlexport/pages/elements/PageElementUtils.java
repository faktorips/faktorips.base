package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.List;
import java.util.Set;

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
}
