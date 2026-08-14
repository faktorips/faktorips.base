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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TemplateValuePmoTest {
    @Mock
    private IAttributeValue value;
    @Mock
    private IAttributeValue templateValue;
    @Mock
    private IProductCmpt container;
    @Mock
    private IProductCmpt templateContainer;
    @Mock
    private IIpsProject ipsProject;

    private TemplateValuePmo<IPropertyValue> templateValuePmo;

    @BeforeEach
    public void setUp() {
        when(value.getTemplatedValueContainer()).thenReturn(container);
        when(value.getIpsProject()).thenReturn(ipsProject);
        when(container.getTemplate()).thenReturn("qualified.TemplateName");
        when(templateValue.getTemplatedValueContainer()).thenReturn(templateContainer);
        when(templateContainer.getProductCmpt()).thenReturn(templateContainer);
        when(templateContainer.getName()).thenReturn("TemplateName");

        templateValuePmo = spy(new TemplateValuePmo<IPropertyValue>(value, null));
    }

    @Test
    public void testGetTemplateName_inherited() {
        when(value.findTemplateProperty(any(IIpsProject.class))).thenReturn(templateValue);

        assertThat(templateValuePmo.getTemplateName(), is("TemplateName"));
    }

    @Test
    public void testGetTemplateName_inherited_templateNotFound() {
        when(value.findTemplateProperty(any(IIpsProject.class))).thenReturn(null);

        assertThat(templateValuePmo.getTemplateName(), is("qualified.TemplateName"));
    }
    /*
     * No tests for getTemplateValue() due to static dependency to
     * ValueHolderToFormattedStringWrapper.
     */
}
