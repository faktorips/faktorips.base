/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplatePropertyFinder.LinkFinder;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplatePropertyFinder.PropertyValueFinder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplatePropertyFinderTest {

    private static final String TARGET = "myTarget";

    private static final String ASSOCIATION = "myAssociation";

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

    @Mock
    private IProductCmptLink link;

    @Mock
    private IProductCmptLinkContainer linkContainer;

    private TemplatePropertyFinder<IPropertyValue, IPropertyValueContainer> finder;

    @Before
    public void setUp() {
        finder = new TemplatePropertyFinder<IPropertyValue, IPropertyValueContainer>(propertyValue,
                new PropertyValueFinder<IPropertyValue>("someProperty", IPropertyValue.class), ipsProject);
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

    @Test
    public void testLinkFinder_Found() {
        mockLinkAndContainer();
        LinkFinder linkFinder = new TemplatePropertyFinder.LinkFinder(ASSOCIATION, TARGET);

        assertThat(linkFinder.apply(linkContainer), is(link));
    }

    @Test
    public void testLinkFinder_InvalidAssociation() {
        mockLinkAndContainer();
        LinkFinder linkFinder = new TemplatePropertyFinder.LinkFinder("invalid", TARGET);

        assertThat(linkFinder.apply(linkContainer), is(nullValue()));
    }

    @Test
    public void testLinkFinder_InvalidTarget() {
        mockLinkAndContainer();
        LinkFinder linkFinder = new TemplatePropertyFinder.LinkFinder(ASSOCIATION, "invalid");

        assertThat(linkFinder.apply(linkContainer), is(nullValue()));
    }

    private void mockLinkAndContainer() {
        when(linkContainer.getLinksAsList(ASSOCIATION)).thenReturn(Arrays.asList(link));
        when(link.getTarget()).thenReturn(TARGET);
    }

}
