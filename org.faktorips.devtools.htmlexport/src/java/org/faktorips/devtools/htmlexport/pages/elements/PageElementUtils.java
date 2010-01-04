package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.List;
import java.util.Set;

public class PageElementUtils {
	
	public static TextPageElement[] createTextPageElements(Set<Style> styles, TextType type, List<String> texts) {
		TextPageElement[] textPageElements = new TextPageElement[texts.size()];
		
		for (int i = 0; i < textPageElements.length; i++) {
			textPageElements[i] = new TextPageElement(texts.get(i), styles, type);
		}
		
		return textPageElements;
	}
}
