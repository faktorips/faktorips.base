/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.attribute;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeSetterAnnGenTest {

    @Mock
    private GeneratorModelContext modelContext;

    AttributeSetterAnnGen attributeSetterAnnGen = new AttributeSetterAnnGen();

    @Test
    public void testCreateAnnotation_product() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo");

        JavaCodeFragment codeFragment = attributeSetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(codeFragment.getSourcecode(),
                is(equalTo("@IpsAttributeSetter(\"foo\")" + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar");

        JavaCodeFragment codeFragment = attributeSetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(codeFragment.getSourcecode(),
                is(equalTo("@IpsAttributeSetter(\"bar\")" + System.lineSeparator())));
    }

    private XPolicyAttribute xPolicyAttribute(String name) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = mock(IPolicyCmptTypeAttribute.class);
        when(policyCmptTypeAttribute.getName()).thenReturn(name);
        return new XPolicyAttribute(policyCmptTypeAttribute, modelContext, null);
    }

    private XProductAttribute xProductAttribute(String name) {
        IProductCmptTypeAttribute productCmptTypeAttribute = mock(IProductCmptTypeAttribute.class);
        when(productCmptTypeAttribute.getName()).thenReturn(name);
        return new XProductAttribute(productCmptTypeAttribute, modelContext, null);
    }

}
