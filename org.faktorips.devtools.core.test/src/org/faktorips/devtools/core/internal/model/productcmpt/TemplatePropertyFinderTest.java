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
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
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
    private IPropertyValue propertyValue;
    @Mock
    private IPropertyValue parentValue;

    private TemplatePropertyFinder<IPropertyValue> finder;

    @Before
    public void setUp() {
        finder = new TemplatePropertyFinder<IPropertyValue>(propertyValue, IPropertyValue.class, ipsProject);
        when(propertyValue.getPropertyName()).thenReturn("someProperty");
    }

    @Test
    public void testVisit_ignoreOriginalPropertyValue() {
        when(propertyValue.getPropertyValueContainer()).thenReturn(container);

        boolean continueVisiting = finder.visit(container);

        assertTrue(continueVisiting);
        assertThat(finder.getPropertyValue(), is(nullValue()));
    }

    @Test
    public void testVisit_noPropertyValue() {
        when(propertyValue.getPropertyValueContainer()).thenReturn(container);

        boolean continueVisiting = finder.visit(parentContainer);

        assertTrue(continueVisiting);
        assertThat(finder.getPropertyValue(), is(nullValue()));
    }

    @Test
    public void testVisit_inheritedPropertyValue() {
        when(propertyValue.getPropertyValueContainer()).thenReturn(container);
        when(parentContainer.getPropertyValue(anyString(), eq(IPropertyValue.class))).thenReturn(parentValue);
        when(parentValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);

        boolean continueVisiting = finder.visit(parentContainer);

        assertTrue(continueVisiting);
        assertThat(finder.getPropertyValue(), is(nullValue()));
    }

    @Test
    public void testVisit_definedPropertyValue() {
        when(propertyValue.getPropertyValueContainer()).thenReturn(container);
        when(parentContainer.getPropertyValue(anyString(), eq(IPropertyValue.class))).thenReturn(parentValue);
        when(parentValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        boolean continueVisiting = finder.visit(parentContainer);

        assertFalse(continueVisiting);
        assertThat(finder.getPropertyValue(), is(parentValue));
    }

    @Test
    public void testVisit_undefinedPropertyValue() {
        when(propertyValue.getPropertyValueContainer()).thenReturn(container);
        when(parentContainer.getPropertyValue(anyString(), eq(IPropertyValue.class))).thenReturn(parentValue);
        when(parentValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);

        boolean continueVisiting = finder.visit(parentContainer);

        assertFalse(continueVisiting);
        assertThat(finder.getPropertyValue(), is(nullValue()));
    }
}
