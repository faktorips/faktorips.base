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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

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

    private MockitoSession mockito;

    private TemplateValuePmo<IPropertyValue> templateValuePmo;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        lenient().when(value.getTemplatedValueContainer()).thenReturn(container);
        when(value.getIpsProject()).thenReturn(ipsProject);
        lenient().when(container.getTemplate()).thenReturn("qualified.TemplateName");
        lenient().when(templateValue.getTemplatedValueContainer()).thenReturn(templateContainer);
        lenient().when(templateContainer.getProductCmpt()).thenReturn(templateContainer);
        lenient().when(templateContainer.getName()).thenReturn("TemplateName");

        templateValuePmo = spy(new TemplateValuePmo<IPropertyValue>(value, null));
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
