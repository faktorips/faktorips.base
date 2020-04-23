/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Test;
import org.mockito.Mock;

public class AttributeAllowedValuesSetterAnnGenTest {
    @Mock
    private GeneratorModelContext modelContext;
    private AttributeAllowedValuesSetterAnnGen attributeAllowedValuesSetterAnnGen = new AttributeAllowedValuesSetterAnnGen();

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertThat(attributeAllowedValuesSetterAnnGen.isGenerateAnnotationFor(mock(XPolicyAttribute.class)), is(true));
        assertThat(attributeAllowedValuesSetterAnnGen.isGenerateAnnotationFor(mock(XProductAttribute.class)),
                is(false));
        assertThat(attributeAllowedValuesSetterAnnGen.isGenerateAnnotationFor(mock(XProductAssociation.class)),
                is(false));
    }

    @Test
    public void testCreateAnnotation() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("bar");

        JavaCodeFragment codeFragment = attributeAllowedValuesSetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(codeFragment.getSourcecode(),
                is(equalTo("@IpsAllowedValuesSetter(\"bar\")" + System.getProperty("line.separator"))));
    }

    private XProductAttribute xProductAttribute(String name) {
        IProductCmptTypeAttribute productCmptTypeAttribute = mock(IProductCmptTypeAttribute.class);
        when(productCmptTypeAttribute.getName()).thenReturn(name);
        return new XProductAttribute(productCmptTypeAttribute, modelContext, null);
    }

}
