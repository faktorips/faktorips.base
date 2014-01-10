/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    }

}
