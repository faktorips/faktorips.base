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

import java.io.UnsupportedEncodingException;

import org.faktorips.devtools.htmlexport.AbstractHtmlExportXmlUnitLayouterTest;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.junit.Before;

public abstract class AbstractXmlUnitHtmlExportTest extends AbstractHtmlExportPluginTest {

    private ILayouter layouter;

    public AbstractXmlUnitHtmlExportTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        layouter = new HtmlLayouter(new TestUtil().createMockDocumentationContext(), "");
    }

    protected void assertXPathExists(IPageElement pageElement, String xPath) throws Exception {
        new AbstractHtmlExportXmlUnitLayouterTest() {
        }.assertXpathExists(createXml(pageElement), xPath);
    }

    protected void assertXPathNotExists(IPageElement pageElement, String xPath) throws Exception {
        new AbstractHtmlExportXmlUnitLayouterTest() {
        }.assertXpathNotExists(createXml(pageElement), xPath);
    }

    private String createXml(IPageElement pageElement) throws UnsupportedEncodingException {
        pageElement.build();

        layouter.clear();
        pageElement.acceptLayouter(layouter);

        return new String(layouter.generate(), IGenerator.CHARSET);
    }

}