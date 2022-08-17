/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XTableUsage;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;

public class ProductCmptDeclClassAnnGenTest {

    private ProductCmptDeclClassAnnGen generator = new ProductCmptDeclClassAnnGen();

    @Test
    public void test() {
        XProductCmptClass product = mockProduct();

        assertEquals("@IpsProductCmptType(name = \"test.ProductCmpt\")" + System.lineSeparator(),
                generator.createAnnotation(product).getSourcecode());
    }

    @Test
    public void testChangingOverTime() {
        XProductCmptClass product = mockProduct();

        when(product.isChangingOverTime()).thenReturn(true);
        XProductCmptGenerationClass generationClass = mock(XProductCmptGenerationClass.class);
        when(generationClass.getInterfaceName()).thenReturn("IProductCmptGen");
        when(product.getProductCmptGenerationNode()).thenReturn(generationClass);

        assertEquals("@IpsProductCmptType(name = \"test.ProductCmpt\")" + System.lineSeparator()
                + "@IpsChangingOverTime(IProductCmptGen.class)" + System.lineSeparator(),
                generator
                        .createAnnotation(product).getSourcecode());
    }

    @Test
    public void testConfigures() {
        XProductCmptClass product = mockProduct();

        when(product.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(product.getPolicyInterfaceName()).thenReturn("PolicyInterfaceClass");
        assertEquals("@IpsProductCmptType(name = \"test.ProductCmpt\")" + System.lineSeparator()
                + "@IpsConfigures(PolicyInterfaceClass.class)" + System.lineSeparator(),
                generator
                        .createAnnotation(product).getSourcecode());
    }

    @Test
    public void testCreateTableUsagesAnnotation() {
        XProductCmptClass productWithTableUsages = mockProductWithTableUsages();

        JavaCodeFragment annotationCode = generator.createAnnTableUsages(productWithTableUsages);

        assertEquals("@IpsTableUsages({\"table1\", \"table2\"})" + System.lineSeparator(),
                annotationCode.getSourcecode());
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsages"));
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

    private XProductCmptClass mockProductWithTableUsages() {
        XProductCmptClass product = mockProduct();

        XTableUsage table1 = mock(XTableUsage.class);
        when(table1.getName()).thenReturn("table1");
        XTableUsage table2 = mock(XTableUsage.class);
        when(table2.getName()).thenReturn("table2");

        when(product.getAllDeclaredTables()).thenReturn(new LinkedHashSet<>(Arrays.asList(table1, table2)));
        return product;
    }
}
