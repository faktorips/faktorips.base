/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.attribute;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AttributeDefaultValueSetterAnnGenTest {

    @Mock
    private GeneratorModelContext modelContext;
    private AttributeDefaultValueSetterAnnGen attributeDefaultValueSetterAnnGen = new AttributeDefaultValueSetterAnnGen();

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertThat(attributeDefaultValueSetterAnnGen.isGenerateAnnotationFor(mock(XPolicyAttribute.class)), is(true));
        assertThat(attributeDefaultValueSetterAnnGen.isGenerateAnnotationFor(mock(XProductAttribute.class)), is(false));
        assertThat(attributeDefaultValueSetterAnnGen.isGenerateAnnotationFor(mock(XProductAssociation.class)),
                is(false));
    }

    @Test
    public void testCreateAnnotation() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("bar");

        JavaCodeFragment codeFragment = attributeDefaultValueSetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(codeFragment.getSourcecode(),
                is(equalTo("@IpsDefaultValueSetter(\"bar\")" + System.lineSeparator())));
    }

    private XProductAttribute xProductAttribute(String name) {
        IProductCmptTypeAttribute productCmptTypeAttribute = mock(IProductCmptTypeAttribute.class);
        when(productCmptTypeAttribute.getName()).thenReturn(name);
        return new XProductAttribute(productCmptTypeAttribute, modelContext, null);
    }

}
