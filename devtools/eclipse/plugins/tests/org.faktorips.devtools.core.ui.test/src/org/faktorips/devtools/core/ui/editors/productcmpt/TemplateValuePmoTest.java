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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateValuePmoTest {
    @Mock
    private IAttributeValue value;
    @Mock
    private IAttributeValue templateValue;
    @Mock
    private IProductCmpt container;
    @Mock
    private IProductCmpt templateContainer;

    private TemplateValuePmo<IPropertyValue> templateValuePmo;

    @Before
    public void setUp() {
        when(value.getTemplatedValueContainer()).thenReturn(container);
        when(container.getTemplate()).thenReturn("qualified.TemplateName");
        when(templateValue.getTemplatedValueContainer()).thenReturn(templateContainer);
        when(templateContainer.getProductCmpt()).thenReturn(templateContainer);
        when(templateContainer.getName()).thenReturn("TemplateName");

        templateValuePmo = spy(new TemplateValuePmo<IPropertyValue>(value, null));
        doReturn(TemplateValueUiStatus.INHERITED).when(templateValuePmo).getTemplateValueStatus();
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
