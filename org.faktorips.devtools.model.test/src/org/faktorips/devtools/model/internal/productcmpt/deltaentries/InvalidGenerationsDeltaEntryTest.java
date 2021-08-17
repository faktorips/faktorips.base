/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class InvalidGenerationsDeltaEntryTest extends AbstractIpsPluginTest {

    @Test
    public void testFix() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        ProductCmpt productCmpt = newProductCmpt(ipsProject, "MyProductCmpt");
        productCmpt.newGeneration(new GregorianCalendar(2000, 0, 1));
        productCmpt.newGeneration(new GregorianCalendar(2005, 0, 1));
        GregorianCalendar validFrom = new GregorianCalendar(1999, 0, 1);
        productCmpt.newGeneration(validFrom);
        InvalidGenerationsDeltaEntry unusedGenerationsDeltaEntry = new InvalidGenerationsDeltaEntry(productCmpt);

        unusedGenerationsDeltaEntry.fix();

        assertEquals(1, productCmpt.getNumOfGenerations());
        assertEquals(validFrom, productCmpt.getLatestGeneration().getValidFrom());
    }

}
