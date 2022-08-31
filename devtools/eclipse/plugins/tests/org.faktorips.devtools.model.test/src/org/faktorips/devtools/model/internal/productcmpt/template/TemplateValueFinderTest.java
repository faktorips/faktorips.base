/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TemplateValueFinderTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ITemplatedValueContainer container;

    @Mock
    private ITemplatedValueContainer parentContainer;

    @Mock
    private ITemplatedValue value;

    @Mock
    private ITemplatedValueIdentifier identifier;

    @Mock
    private ITemplatedValue parentValue;

    private TemplateValueFinder<ITemplatedValue, ITemplatedValueContainer> finder;

    @Before
    public void setUp() {
        finder = new TemplateValueFinder<>(value, ITemplatedValue.class,
                identifier, ipsProject);
    }

    @Test
    public void testVisit_ignoreOriginalPropertyValue() {
        when(value.getTemplatedValueContainer()).thenReturn(container);

        boolean continueVisiting = finder.visit(container);

        assertTrue(continueVisiting);
        assertThat(finder.getTemplateValue(), is(nullValue()));
        assertThat(finder.isKnownInTemplate(), is(false));
    }

    @Test
    public void testVisit_noPropertyValue() {
        when(value.getTemplatedValueContainer()).thenReturn(container);

        boolean continueVisiting = finder.visit(parentContainer);

        assertTrue(continueVisiting);
        assertThat(finder.getTemplateValue(), is(nullValue()));
        assertThat(finder.isKnownInTemplate(), is(false));
    }

    @Test
    public void testVisit_inheritedPropertyValue() {
        when(value.getTemplatedValueContainer()).thenReturn(container);
        when(identifier.getValueFrom(parentContainer)).thenReturn(parentValue);
        when(parentValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);

        boolean continueVisiting = finder.visit(parentContainer);

        assertTrue(continueVisiting);
        assertThat(finder.getTemplateValue(), is(nullValue()));
        // because it is only known when it is found after continuing with the parent
        assertThat(finder.isKnownInTemplate(), is(false));
    }

    @Test
    public void testVisit_definedPropertyValue() {
        when(value.getTemplatedValueContainer()).thenReturn(container);
        when(identifier.getValueFrom(parentContainer)).thenReturn(parentValue);
        when(parentValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        boolean continueVisiting = finder.visit(parentContainer);

        assertFalse(continueVisiting);
        assertThat(finder.getTemplateValue(), is(parentValue));
        assertThat(finder.isKnownInTemplate(), is(true));
    }

    @Test
    public void testVisit_undefinedPropertyValue() {
        when(value.getTemplatedValueContainer()).thenReturn(container);
        when(identifier.getValueFrom(parentContainer)).thenReturn(parentValue);
        when(parentValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);

        boolean continueVisiting = finder.visit(parentContainer);

        assertFalse(continueVisiting);
        assertThat(finder.getTemplateValue(), is(nullValue()));
        assertThat(finder.isKnownInTemplate(), is(true));
    }

}
