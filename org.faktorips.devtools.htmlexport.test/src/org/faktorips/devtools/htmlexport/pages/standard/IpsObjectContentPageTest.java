/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.io.IOException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractHtmlExportTest;
import org.xml.sax.SAXException;

public class IpsObjectContentPageTest extends AbstractHtmlExportTest {
    private class HtmlExportXmlUnitTest extends XMLTestCase {
        public void test(String xPath, String xml) throws XpathException, IOException, SAXException {
            String xmlWithoutDoctypeDeclaration = prepareXml(xml);

            assertXpathExists(xPath, xmlWithoutDoctypeDeclaration);
        }

        private String prepareXml(String xml) {
            return xml.replaceFirst("<html .+\n", "<html>\n").replaceFirst("<!DOCTYPE .+\n", "").trim();
        }
    }

    private ILayouter layouter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        layouter = new HtmlLayouter("");
    }

    public void testPolicyCmptType() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        String expectedHeadline = policy.getIpsObjectType().getDisplayName() + " " + policy.getName();

        String xPath = "//h1[. = '" + expectedHeadline + "']";

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                config);
        assertXPath(objectContentPage, xPath);

    }

    private void assertXPath(AbstractPageElement pageElement, final String xPath) throws Exception {
        pageElement.build();

        layouter.clear();
        pageElement.acceptLayouter(layouter);

        String xml = new String(layouter.generate(), IGenerator.CHARSET);

        new HtmlExportXmlUnitTest().test(xPath, xml);

    }
}
