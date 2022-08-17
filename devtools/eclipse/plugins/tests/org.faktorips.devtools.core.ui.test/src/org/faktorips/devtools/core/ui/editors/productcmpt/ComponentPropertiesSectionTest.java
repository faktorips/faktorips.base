/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.editors.productcmpt.ComponentPropertiesSection.ComponentPropertiesPMO;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class ComponentPropertiesSectionTest extends AbstractIpsPluginTest {
    private IIpsProject ipsProject;
    private ProductCmpt productCmpt;
    private GregorianCalendar january;
    private GregorianCalendar april;
    private GregorianCalendar june;
    private ProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        january = new GregorianCalendar(2015, 0, 1);
        april = new GregorianCalendar(2015, 0, 1);
        june = new GregorianCalendar(2015, 5, 1);

        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "productCmptType");

        productCmpt = newProductCmpt(productCmptType, "ProdCmpt");
        productCmpt.getGeneration(0).setValidFrom(june);
    }

    @Test
    public void testGetValidFrom() {
        ComponentPropertiesPMO pmo = new ComponentPropertiesSection.ComponentPropertiesPMO(productCmpt);

        // list order deliberately different from chronological order
        productCmpt.newGeneration();
        productCmpt.getGeneration(1).setValidFrom(april);

        assertEquals(april, pmo.getValidFrom());
    }

    @Test
    public void testGetValidFrom_singleGeneration() {
        ComponentPropertiesPMO pmo = new ComponentPropertiesSection.ComponentPropertiesPMO(productCmpt);

        assertEquals(june, pmo.getValidFrom());
    }

    @Test
    public void testSetValidFrom() {
        ComponentPropertiesPMO pmo = new ComponentPropertiesSection.ComponentPropertiesPMO(productCmpt);
        // list order deliberately different from chronological order
        productCmpt.newGeneration();
        productCmpt.getGeneration(1).setValidFrom(april);

        pmo.setValidFrom(january);

        assertEquals(january, pmo.getValidFrom());
        assertEquals(january, productCmpt.getGeneration(1).getValidFrom());
        assertEquals(june, productCmpt.getGeneration(0).getValidFrom());
    }

    @Test
    public void testIsValidFromEnabled_typeChangingOverTime() {
        productCmptType.setChangingOverTime(true);

        ComponentPropertiesPMO pmo = new ComponentPropertiesSection.ComponentPropertiesPMO(productCmpt);

        assertFalse(pmo.isValidFromEnabled());
    }

    @Test
    public void testIsValidFromEnabled_typeStatic() {
        productCmptType.setChangingOverTime(false);

        ComponentPropertiesPMO pmo = new ComponentPropertiesSection.ComponentPropertiesPMO(productCmpt);

        assertTrue(pmo.isValidFromEnabled());
    }

    @Test
    public void testIsValidFromEnabled_typeCanNotBeFound() {
        productCmpt = spy(productCmpt);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(null);
        ComponentPropertiesPMO pmo = new ComponentPropertiesSection.ComponentPropertiesPMO(productCmpt);

        assertFalse(pmo.isValidFromEnabled());
    }
}
