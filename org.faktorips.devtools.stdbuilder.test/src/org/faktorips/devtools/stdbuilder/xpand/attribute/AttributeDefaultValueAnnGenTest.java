/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.attribute;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeDefaultValueAnnGenTest {

    @Mock
    private GeneratorModelContext modelContext;
    private AttributeDefaultValueAnnGen attributeDefaultValueAnnGen = new AttributeDefaultValueAnnGen();

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertThat(attributeDefaultValueAnnGen.isGenerateAnnotationFor(mock(XPolicyAttribute.class)), is(true));
        assertThat(attributeDefaultValueAnnGen.isGenerateAnnotationFor(mock(XProductAttribute.class)), is(false));
        assertThat(attributeDefaultValueAnnGen.isGenerateAnnotationFor(mock(XProductAssociation.class)), is(false));
    }

    @Test
    public void testCreateAnnotation() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("bar");

        JavaCodeFragment codeFragment = attributeDefaultValueAnnGen.createAnnotation(xProductAttribute);

        assertThat(codeFragment.getSourcecode(),
                is(equalTo("@IpsDefaultValue(\"bar\")" + System.getProperty("line.separator"))));
    }

    private XProductAttribute xProductAttribute(String name) {
        IProductCmptTypeAttribute productCmptTypeAttribute = mock(IProductCmptTypeAttribute.class);
        when(productCmptTypeAttribute.getName()).thenReturn(name);
        return new XProductAttribute(productCmptTypeAttribute, modelContext, null);
    }

}
