/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.search.product.conditions.table;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;

public class ElementLabelProviderTest extends AbstractIpsPluginTest {

    private ElementLabelProvider elementLabelProvider;
    private IAttribute attribute;

    @Override
    @Before
    public void setUp() throws Exception {
        elementLabelProvider = new ElementLabelProvider();

        IIpsProject ipsProject = newIpsProject("IpsProject");
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        attribute = productCmptType.newAttribute();
        attribute.setName("AttributName");
    }

    @Test
    public void testLabelOrName_Label() {
        attribute.setLabelValue(Locale.GERMAN, "DE_AttributeLabel");

        assertEquals("DE_AttributeLabel", elementLabelProvider.getLabelOrName(attribute));
    }

    @Test
    public void testLabelOrName_Name() {
        assertEquals("AttributName", elementLabelProvider.getLabelOrName(attribute));
    }

    @Test
    public void testLabelOrName_Null() {
        attribute = null;

        assertEquals("", elementLabelProvider.getLabelOrName(attribute));
    }
}
