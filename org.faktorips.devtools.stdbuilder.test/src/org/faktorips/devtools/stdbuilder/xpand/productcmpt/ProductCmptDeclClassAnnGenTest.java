/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;

public class ProductCmptDeclClassAnnGenTest {

    private ProductCmptDeclClassAnnGen generator = new ProductCmptDeclClassAnnGen();

    @Test
    public void test() {
        XProductCmptClass product = mockProduct();

        assertEquals("@IpsProductCmptType(name = \"test.ProductCmpt\")" + System.getProperty("line.separator"),
                generator.createAnnotation(product).getSourcecode());
    }

    @Test
    public void testChangingOverTime() {
        XProductCmptClass product = mockProduct();

        when(product.isChangingOverTime()).thenReturn(true);
        XProductCmptGenerationClass generationClass = mock(XProductCmptGenerationClass.class);
        when(generationClass.getInterfaceName()).thenReturn("IProductCmptGen");
        when(product.getProductCmptGenerationNode()).thenReturn(generationClass);

        assertEquals("@IpsProductCmptType(name = \"test.ProductCmpt\")" + System.getProperty("line.separator")
                + "@IpsChangingOverTime(IProductCmptGen.class)" + System.getProperty("line.separator"), generator
                .createAnnotation(product).getSourcecode());
    }

    @Test
    public void testConfigures() {
        XProductCmptClass product = mockProduct();

        when(product.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(product.getPolicyInterfaceName()).thenReturn("PolicyInterfaceClass");
        assertEquals("@IpsProductCmptType(name = \"test.ProductCmpt\")" + System.getProperty("line.separator")
                + "@IpsConfigures(PolicyInterfaceClass.class)" + System.getProperty("line.separator"), generator
                .createAnnotation(product).getSourcecode());
    }

    private XProductCmptClass mockProduct() {
        XProductCmptClass product = mock(XProductCmptClass.class);
        when(product.addImport(IpsProductCmptType.class)).thenReturn("IpsProductCmptType");

        IProductCmptType ipsObject = mock(IProductCmptType.class);
        when(ipsObject.getQualifiedName()).thenReturn("test.ProductCmpt");
        when(product.getIpsObjectPartContainer()).thenReturn(ipsObject);

        XPolicyCmptClass policy = mock(XPolicyCmptClass.class);
        when(policy.getImplClassName()).thenReturn("PolicyCmpt");
        when(product.getPolicyCmptClass()).thenReturn(policy);
        return product;
    }
}
