/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html.elements;

import org.faktorips.devtools.htmlexport.AbstractHtmlExportXmlUnitLayouterTest;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.junit.Before;

public abstract class AbstractHtmlPageElementLayouterTest extends AbstractHtmlExportXmlUnitLayouterTest {

    protected HtmlLayouter layouter = new HtmlLayouter(new TestUtil().createMockDocumentationContext(), "",
            new TestUtil().createMockIoHandler());

    public AbstractHtmlPageElementLayouterTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        layouter.clear();
    }

    protected DocumentationContext getContext() {
        return layouter.getContext();
    }

}
