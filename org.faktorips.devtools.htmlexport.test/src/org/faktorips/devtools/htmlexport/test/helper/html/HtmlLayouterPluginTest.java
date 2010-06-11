package org.faktorips.devtools.htmlexport.test.helper.html;

import java.io.UnsupportedEncodingException;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;

public class HtmlLayouterPluginTest extends AbstractIpsPluginTest {
	protected IIpsProject ipsProject;
	private HtmlLayouter layouter = new HtmlLayouter(".resource");

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		layouter.clear();
		ipsProject = newIpsProject("TestProjekt");

	}
/*
	public void testHtmlLayouterLinkPageElement() throws Exception {
		String text = "text beispiel";

		PolicyCmptType to = newPolicyCmptType(ipsProject, "base.BVB");

		String target = "classes";

		LinkPageElement pageElement = new LinkPageElement(to, target, new TextPageElement(text));

		String expected = "<a href=\"../../base/class_PolicyCmptType_BVB.html\" class=\"\" target=\"classes\">" + text
				+ "</a>";
		assertEquals(expected, layout(pageElement));
	}

	public void testHtmlLayouterLinkPageElementToRoot() throws Exception {
		String text = "text beispiel";

		PolicyCmptType to = newPolicyCmptType(ipsProject, "BVB");

		String target = "classes";

		LinkPageElement pageElement = new LinkPageElement(to, target, new TextPageElement(text));

		String expected = "<a href=\"../../class_PolicyCmptType_BVB.html\" class=\"\" target=\"classes\">" + text
				+ "</a>";
		assertEquals(expected, layout(pageElement));
	}

	public void testHtmlLayouterLinkPageElementFromRoot() throws Exception {
		String text = "text beispiel";

		PolicyCmptType to = newPolicyCmptType(ipsProject, "base.BVB");

		String target = "classes";

		LinkPageElement pageElement = new LinkPageElement(to, target, new TextPageElement(text));

		String expected = "<a href=\"base/class_PolicyCmptType_BVB.html\" class=\"\" target=\"classes\">" + text
				+ "</a>";
		assertEquals(expected, layout(pageElement));
	}

	public void testHtmlLayouterLinkPageElementFromRootToRoot() throws Exception {
		String text = "text beispiel";

		PolicyCmptType to = newPolicyCmptType(ipsProject, "BVB");

		String target = "classes";

		LinkPageElement pageElement = new LinkPageElement(to, target, new TextPageElement(text));

		String expected = "<a href=\"class_PolicyCmptType_BVB.html\" class=\"\" target=\"classes\">" + text + "</a>";
		assertEquals(expected, layout(pageElement));
	}

	public void testHtmlLayouterLinkPageElementPackage() throws Exception {
		String text = "text beispiel";

		PolicyCmptType to = newPolicyCmptType(ipsProject, "kranken.sub.BVB");

		String target = "classes";

		LinkPageElement pageElement = new LinkPageElement(to, target, new TextPageElement(text));

		String expected = "<a href=\"../kranken/sub/class_PolicyCmptType_BVB.html\" class=\"\" target=\"classes\">"
				+ text + "</a>";
		assertEquals(expected, layout(pageElement));
	}
*/
	protected void assertContains(String html, String... containments) {
		for (String string : containments) {
			assertTrue("Nicht enthalten: " + string, html.contains(string));
		}
	}

	protected String layout(LinkPageElement pageElement) throws UnsupportedEncodingException {
		pageElement.acceptLayouter(layouter);
		byte[] generate = layouter.generate();

		String html = new String(generate, "UTF-8").trim();
		System.out.println(html);
		return html;
	}
}
