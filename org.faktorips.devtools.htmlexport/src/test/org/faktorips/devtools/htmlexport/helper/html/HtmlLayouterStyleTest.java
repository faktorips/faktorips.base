package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

public class HtmlLayouterStyleTest extends AbstractHtmlLayouterTest {

	public void testStyleBold() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text);
		pageElement.addStyles(Style.BOLD);
		
		assertEquals("<span class=\"BOLD\">" + text + "</span>", layout(pageElement));
	}

	public void testStyleItalic() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text);
		pageElement.addStyles(Style.ITALIC);
		
		assertEquals("<span class=\"ITALIC\">" + text + "</span>", layout(pageElement));
	}

	public void testStyleBoldAndItalic() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text);
		pageElement.addStyles(Style.BOLD);
		pageElement.addStyles(Style.ITALIC);
		
		assertEquals("<span class=\"BOLD ITALIC\">" + text + "</span>", layout(pageElement));
	}
	
	public void testStyleCenter() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK);
		pageElement.addStyles(Style.CENTER);
		
		assertEquals("<div class=\"CENTER\">" + text + "</div>", layout(pageElement));
	}
}
