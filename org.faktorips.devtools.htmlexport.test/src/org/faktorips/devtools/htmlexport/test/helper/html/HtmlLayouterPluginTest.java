package org.faktorips.devtools.htmlexport.test.helper.html;

import java.io.UnsupportedEncodingException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public class HtmlLayouterPluginTest extends AbstractIpsPluginTest {
    protected IIpsProject ipsProject;
    private HtmlLayouter layouter = new HtmlLayouter(".resource");

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        layouter.clear();
        layouter.setPathToRoot("../../");
        ipsProject = newIpsProject("TestProjekt");

    }

    public void testHtmlLayouterLinkPageElement() throws Exception {
        String linkText = "text beispiel";

        PolicyCmptType to = newPolicyCmptType(ipsProject, "base.BVB");

        String target = "classes";

        LinkPageElement pageElement = PageElementUtils.createLinkPageElementToIpsElement(to, target,
                new TextPageElement(linkText));

        assertLinkCorrect(linkText, to, target, pageElement);
    }

    public void testHtmlLayouterLinkPageElementToRoot() throws Exception {
        String linkText = "text beispiel";

        PolicyCmptType to = newPolicyCmptType(ipsProject, "BVB");

        String target = "classes";

        LinkPageElement pageElement = PageElementUtils.createLinkPageElementToIpsElement(to, target,
                new TextPageElement(linkText));

        assertLinkCorrect(linkText, to, target, pageElement);
    }

    public void testHtmlLayouterLinkPageElementFromRoot() throws Exception {
        String linkText = "text beispiel";

        PolicyCmptType to = newPolicyCmptType(ipsProject, "base.BVB");

        String target = "classes";

        LinkPageElement pageElement = PageElementUtils.createLinkPageElementToIpsElement(to, target,
                new TextPageElement(linkText));

        assertLinkCorrect(linkText, to, target, pageElement);
    }

    public void testHtmlLayouterLinkPageElementFromRootToRoot() throws Exception {
        String linkText = "text beispiel";

        PolicyCmptType to = newPolicyCmptType(ipsProject, "BVB");

        String target = "classes";

        LinkPageElement pageElement = PageElementUtils.createLinkPageElementToIpsElement(to, target,
                new TextPageElement(linkText));

        assertLinkCorrect(linkText, to, target, pageElement);
    }

    public void testHtmlLayouterLinkPageElementPackage() throws Exception {
        String linkText = "text beispiel";

        PolicyCmptType to = newPolicyCmptType(ipsProject, "kranken.sub.BVB");

        String target = "classes";

        LinkPageElement pageElement = PageElementUtils.createLinkPageElementToIpsElement(to, target,
                new TextPageElement(linkText));

        assertLinkCorrect(linkText, to, target, pageElement);
    }

    protected void assertContains(String html, String... containments) {
        for (String string : containments) {
            assertTrue("Nicht enthalten: " + string, html.contains(string));
        }
    }

    protected String layout(LinkPageElement pageElement) throws UnsupportedEncodingException {
        pageElement.acceptLayouter(layouter);
        byte[] generate = layouter.generate();

        String html = new String(generate, "UTF-8").trim();
        return html;
    }

    private void assertLinkCorrect(String linkText, PolicyCmptType to, String target, LinkPageElement pageElement)
            throws UnsupportedEncodingException {
        StringBuilder expected = new StringBuilder();
        expected.append("<a href=\"");
        expected.append(layouter.getPathToRoot());
        expected.append(to.getQualifiedName().replace('.', '/'));
        expected.append('.');
        expected.append(to.getIpsObjectType().getFileExtension());
        expected.append(".html");
        expected.append("\" target=\"");
        expected.append(target);
        expected.append("\">");
        expected.append(linkText);
        expected.append("</a>");

        assertEquals(expected.toString(), layout(pageElement));
    }
}
