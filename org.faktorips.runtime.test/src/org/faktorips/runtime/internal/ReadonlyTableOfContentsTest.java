/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ReadonlyTableOfContentsTest extends XmlAbstractTestCase {

    @Test
    public void testInitFromXml() {
        AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument().getDocumentElement());

        assertEquals(1, toc.getProductCmptTocEntries().size());
        assertEquals(2, toc.getModelTypeTocEntries().size());
        assertEquals(3, toc.getEnumContentTocEntries().size());
        assertEquals("org/faktorips/sample/OptionContent2.xml",
                toc.getEnumContentTocEntry("org.faktorips.sample.Option2").getXmlResourceName());
        assertEquals("org/faktorips/sample/OptionContent3.xml",
                toc.getEnumContentTocEntry("org.faktorips.sample.Option3").getXmlResourceName());
    }

}
