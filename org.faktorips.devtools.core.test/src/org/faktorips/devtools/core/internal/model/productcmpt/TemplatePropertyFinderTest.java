/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue.TemplateStatus;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplatePropertyFinderTest {

    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IPropertyValueContainer container;
    @Mock
    private IPropertyValueContainer parentContainer;
    @Mock
    private IAttributeValue attrValue;
    @Mock
    private IAttributeValue parentValue;
    private TemplatePropertyFinder<IAttributeValue> templatePropertyFinder;

    @Before
    public void setUP() {
        templatePropertyFinder = new TemplatePropertyFinder<IAttributeValue>(attrValue, IAttributeValue.class,
                ipsProject);
        when(attrValue.getPropertyName()).thenReturn("someProperty");
    }

    @Test
    public void testVisit_ignoreOriginalPropertyValue() {
        when(attrValue.getPropertyValueContainer()).thenReturn(container);

        boolean continueVisiting = templatePropertyFinder.visit(container);

        assertTrue(continueVisiting);
        assertThat(templatePropertyFinder.getPropertyValue(), is(nullValue()));
    }

    @Test
    public void testVisit_noPropertyValue() {
        when(attrValue.getPropertyValueContainer()).thenReturn(container);

        boolean continueVisiting = templatePropertyFinder.visit(parentContainer);

        assertTrue(continueVisiting);
        assertThat(templatePropertyFinder.getPropertyValue(), is(nullValue()));
    }

    @Test
    public void testVisit_inheritedPropertyValue() {
        when(attrValue.getPropertyValueContainer()).thenReturn(container);
        when(parentContainer.getPropertyValue(anyString(), eq(IAttributeValue.class))).thenReturn(parentValue);
        when(parentValue.getTemplateStatus()).thenReturn(TemplateStatus.INHERITED);

        boolean continueVisiting = templatePropertyFinder.visit(parentContainer);

        assertTrue(continueVisiting);
        assertThat(templatePropertyFinder.getPropertyValue(), is(nullValue()));
    }

    @Test
    public void testVisit_definedPropertyValue() {
        when(attrValue.getPropertyValueContainer()).thenReturn(container);
        when(parentContainer.getPropertyValue(anyString(), eq(IAttributeValue.class))).thenReturn(parentValue);
        when(parentValue.getTemplateStatus()).thenReturn(TemplateStatus.DEFINED);

        boolean continueVisiting = templatePropertyFinder.visit(parentContainer);

        assertFalse(continueVisiting);
        assertThat(templatePropertyFinder.getPropertyValue(), is(parentValue));
    }

    @Test
    public void testVisit_undefinedPropertyValue() {
        when(attrValue.getPropertyValueContainer()).thenReturn(container);
        when(parentContainer.getPropertyValue(anyString(), eq(IAttributeValue.class))).thenReturn(parentValue);
        when(parentValue.getTemplateStatus()).thenReturn(TemplateStatus.UNDEFINED);

        boolean continueVisiting = templatePropertyFinder.visit(parentContainer);

        assertFalse(continueVisiting);
        assertThat(templatePropertyFinder.getPropertyValue(), is(nullValue()));
    }
}
