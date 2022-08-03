/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
