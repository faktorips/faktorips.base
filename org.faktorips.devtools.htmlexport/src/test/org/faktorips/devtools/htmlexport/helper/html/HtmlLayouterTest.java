package org.faktorips.devtools.htmlexport.helper.html;

import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Test fuer das HTML-Layout
 * 
 * <div>Achtung: Für die Generierung der Links sind momentan noch IPS-Objekte
 * notwendig und werden daher im Plugin-Test-Projekt getestet</div>
 */
public class HtmlLayouterTest extends AbstractHtmlLayouterTest {
	public void testHtmlLayouterRootPageElement() throws Exception {
		AbstractRootPageElement pageElement = new AbstractRootPageElement() {

			@Override
			public String getPathToRoot() {
				return "";
			}
			
		};
		pageElement.setTitle("Test");

		String[] containments = { "<html", "</html>", "<head>", "</head>",
				"<title>" + pageElement.getTitle() + "</title>", "<body>", "</body>" };
		assertContains(layout(pageElement), containments);
	}

	public void testHtmlLayouterTextPageElementEinfach() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text);

		assertEquals(text, layout(pageElement));
	}

	public void testHtmlLayouterTextPageElementInline() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text, TextType.INLINE);

		assertEquals("<span>" + text + "</span>", layout(pageElement));
	}

	public void testHtmlLayouterTextPageElementBlock() {
		String text = "text beispiel";
		TextPageElement pageElement = new TextPageElement(text, TextType.BLOCK);

		assertEquals("<div>" + text + "</div>", layout(pageElement));
	}

	/*
	 * da die Links ipsobjekte brauchen, muessen sie als plugin test getestet
	 * werden
	 */
	/*
	 * public void testHtmlLayouterLinkPageElement() throws Exception { String
	 * text = "text beispiel";
	 * 
	 * LinkPageElement pageElement = new LinkPageElement();
	 * 
	 * assertEquals("<h3>" + text + "</h3>", layout(pageElement)); }
	 */

	public void testHtmlLayouterListPageElement() {

		String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" };
		List<PageElement> elementListe = createPageElementListe(texte);

		ListPageElement pageElement = new ListPageElement(elementListe);

		String html = layout(pageElement);
		assertContains(html, texte);

		String[] tags = { "<ul>", "<li>", "</li>", "</ul>" };
		assertContains(html, tags);
	}

	public void testHtmlLayouterListPageElementUngeordnet() {

		String[] texte = { "Item 1", "Punkt 2", "blablabla", "letzter Punkt" };
		List<PageElement> elementListe = createPageElementListe(texte);

		ListPageElement pageElement = new ListPageElement(elementListe);
		pageElement.setOrdered(false);

		String html = layout(pageElement);
		assertContains(html, texte);

		String[] tags = { "<ol>", "<li>", "</li>", "</ol>" };
		assertContains(html, tags);
	}

	public void testHtmlLayouterTablePageElement() {
		int rows = 3;
		int cols = 4;

		String[][] texte = new String[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				texte[i][j] = i + "-" + j;
			}
		}

		TablePageElement table = new TablePageElement();

		for (String[] zeile : texte) {
			table
					.addPageElements(new TableRowPageElement(createPageElementListe(zeile).toArray(
							new TextPageElement[0])));

		}

		String html = layout(table);
		for (int i = 0; i < texte.length; i++) {
			assertContains(html, texte[i]);
		}

		String[] tags = { "<table", "<tr>", "<td>", "</td>", "</tr>", "</table>" };
		assertContains(html, tags);
	}

}
