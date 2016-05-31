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

public class ProductCmptClassAnnotationGeneratorTest {

    private ProductCmptDeclClassAnnotationGenerator generator = new ProductCmptDeclClassAnnotationGenerator();

    @Test
    public void test() {
        XProductCmptClass product = mock(XProductCmptClass.class);
        when(product.addImport(IpsProductCmptType.class)).thenReturn("IpsProductCmptType");

        IProductCmptType ipsObject = mock(IProductCmptType.class);
        when(ipsObject.getQualifiedName()).thenReturn("test.ProductCmpt");
        when(product.getIpsObjectPartContainer()).thenReturn(ipsObject);

        XPolicyCmptClass policy = mock(XPolicyCmptClass.class);
        when(policy.getImplClassName()).thenReturn("PolicyCmpt");
        when(product.getPolicyCmptClass()).thenReturn(policy);

        assertEquals(
                "@IpsProductCmptType(name = \"test.ProductCmpt\", changingOverTime = false)"
                        + System.getProperty("line.separator"), generator.createAnnotation(product).getSourcecode());

        when(product.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(product.getPolicyInterfaceName()).thenReturn("PolicyInterfaceClass");
        assertEquals(
                "@IpsProductCmptType(name = \"test.ProductCmpt\", changingOverTime = false)"
                        + System.getProperty("line.separator") + "@IpsConfigures(PolicyInterfaceClass.class)"
                        + System.getProperty("line.separator"), generator.createAnnotation(product).getSourcecode());

        when(product.isConfigurationForPolicyCmptType()).thenReturn(false);
        when(product.isChangingOverTime()).thenReturn(true);
        XProductCmptGenerationClass generationClass = mock(XProductCmptGenerationClass.class);
        when(generationClass.getImplClassName()).thenReturn("ProductCmptGen");
        when(product.getProductCmptGenerationNode()).thenReturn(generationClass);

        assertEquals(
                "@IpsProductCmptType(name = \"test.ProductCmpt\", changingOverTime = true)"
                        + System.getProperty("line.separator") + "@IpsProductCmptTypeGen(ProductCmptGen.class)"
                        + System.getProperty("line.separator"), generator.createAnnotation(product).getSourcecode());

    }
}
