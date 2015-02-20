/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class ProductCmptToTypeDeltaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "MyProductCmptType");
        productCmpt = newProductCmpt(productCmptType, "MyProductCmpt");
    }

    @Test
    public void testDelta_empty() throws Exception {

        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    @Test
    public void testDelta_unusedGenerations_onlyOneGeneration() throws Exception {
        productCmptType.setChangingOverTime(false);

        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    @Test
    public void testDelta_unusedGenerations_needToFix() throws Exception {
        productCmpt.newGeneration(new GregorianCalendar(2001, 0, 1));
        productCmptType.setChangingOverTime(false);

        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertFalse(productCmptToTypeDelta.isEmpty());
        assertEquals(1, productCmptToTypeDelta.getEntries(DeltaType.INVALID_GENERATIONS).length);
    }

}
